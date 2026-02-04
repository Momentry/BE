package com.momentry.BE.domain.file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.file.dto.MediaProcessingResultDto;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaResultListener {

    private final FileService fileService;

    @SqsListener(value = "${aws.sqs.queue-name}", factory = "defaultSqsListenerContainerFactory") // application.yml에
                                                                                                  // 정의된 큐 이름
    @Transactional
    public void receiveMessage(MediaProcessingResultDto message) {
        log.info("SQS 메시지 수신: fileKey={}, status={}, createdAt={}, fullMessage={}", message.getFileKey(), message.getStatus(), message.getCreatedAt(), message);

        if ("SUCCESS".equals(message.getStatus())) {
            // 파일 path 업데이트 메서드 호출
            fileService.updateThumbDisplayPathOfFile(
                    message.getFileKey(),
                    message.getThumbnailPath(),
                    message.getDisplayPath(),
                    message.getMetadata(),
                    message.getCreatedAt()
            );
        } else {
            log.error("미디어 처리 실패 메시지 수신: fileId={}", message.getFileKey());
        }
    }
}