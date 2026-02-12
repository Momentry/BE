package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.file.dto.SaveFileDto;
import com.momentry.BE.global.event.dto.FileUploadEvent;
import org.springframework.cglib.core.Local;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.file.dto.MediaProcessingResultDto;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaResultListener {

    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @SqsListener(value = "${aws.sqs.queue-name}", factory = "defaultSqsListenerContainerFactory") // application.yml에 등록된 SQS 큐 이름
    @Transactional
    public void receiveMessage(MediaProcessingResultDto message) {
        log.info("SQS 메시지 수신: fileKey={}, status={}, capturedAt={}, fullMessage={}", message.getFileKey(), message.getStatus(), message.getCapturedAt(), message);

        if ("SUCCESS".equals(message.getStatus())) {
            boolean success = true;

            try{
                // 파일 정보 저장 메서드 호출
                fileService.saveFileInfo(
                        SaveFileDto.of(message)
                );
            } catch (DataIntegrityViolationException e){
                // 데이터 무결성에 의해 실패 -> 재시도 하지 않고 해당 메시지는 폐기
                log.error("파일 정보 DB 저장 도중 에러 발생: {}", e.toString());

                success = false;
            } finally {
                // fcm 전송 이벤트
                FileUploadEvent event = new FileUploadEvent(message.getUploaderId(), message.getAlbumId(), success);
                eventPublisher.publishEvent(event);
            }
        } else {
            log.error("미디어 처리 실패 메시지 수신: fileId={}", message.getFileKey());
        }
    }
}