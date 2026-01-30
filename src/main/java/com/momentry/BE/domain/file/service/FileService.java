package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final S3Util s3Util;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public FileResult uploadFile(Long albumId, MultipartFile file, String metadata, LocalDateTime createdAt){
        // TODO: 파일 업로드 시 예외 처리
        // 1. 앨범이 없는 경우
        // 2. 앨범에 업로드할 권한이 없는 경우
        // 3. 최대 업로드 가능한 크기 초과한 경우?

        // TODO: 1차/2차 파일 압축

        // TODO: DB에 파일 정보 저장

        // 파일 업로드
        // S3에 저장할 고유한 파일명 생성 (UUID 활용)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String s3Key = "albums/" + albumId + "/original/" + UUID.randomUUID() + extension;

        try (InputStream is = file.getInputStream()) {
            s3Util.upload(bucketName, s3Key, is, file.getSize());

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
            log.info("new uploaded file URL: {}", fileUrl);

            // 테스트용 결과 반환 (추후 DB 저장 로직 추가 필요)
            return new FileResult();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류 발생", e);
        }
    }
}