package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.file.dto.MediaProcessingResultDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.repository.FileRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaResultListener {

    private final FileRepository fileRepository;

    @SqsListener(value = "${aws.sqs.queue-name}", factory = "defaultSqsListenerContainerFactory") // application.yml에 정의된 큐 이름
    @Transactional
    public void receiveMessage(MediaProcessingResultDto message) {
        log.info("SQS 메시지 수신: fileId={}, status={}", message.getFileKey(), message.getStatus());

        if ("SUCCESS".equals(message.getStatus())) {
            File file = fileRepository.findByFileKey(message.getFileKey())
                    .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

            // 리사이징된 경로 업데이트
            file.updatePostProcessingResults(
                    message.getThumbnailPath(),
                    message.getDisplayPath()
            );

            log.info("파일 정보 업데이트 완료: ID={}", file.getId());
        } else {
            log.error("미디어 처리 실패 메시지 수신: fileId={}", message.getFileKey());
        }
    }
}