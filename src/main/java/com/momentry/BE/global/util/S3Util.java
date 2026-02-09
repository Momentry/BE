package com.momentry.BE.global.util;

import com.momentry.BE.domain.file.entity.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3Util {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public void upload(String key, InputStream is, long contentLength) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, contentLength));
    }

    public void deleteAll(File targetFile){
        try{
            if(hasText(targetFile.getOriginUrl())) delete(targetFile.getOriginUrl());
            if(hasText(targetFile.getThumbUrl())) delete(targetFile.getThumbUrl());
            if(hasText(targetFile.getDisplayUrl())) delete(targetFile.getDisplayUrl());
        }catch (Exception e){
            // 예외를 던지는 대신 로그를 남겨서 전체 프로세스가 멈추지 않게 함
            log.error("S3 물리 파일 삭제 실패 (File ID: {}): {}", targetFile.getId(), e.getMessage());
        }
    }

    public void delete(String key){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public String generatePresignedUrl(String fileKey){
        if(!hasText(fileKey)) return null;

        // 접근할 파일 정의
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        // Presigned URL 발급 옵션 설정
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // 10분간 유효
                .getObjectRequest(getObjectRequest)
                .build();

        // URL 발급
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public String generatePresignedUploadUrl(Long uploaderId, String fileKey, String contentType) {
        if (!hasText(fileKey)) return null;

        // 업로드될 파일의 위치와 설정 정의
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType)
                .metadata(Map.of("uploaderid", String.valueOf(uploaderId)))
                .build();

        // Presigned URL 발급 옵션 설정
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 10분간 유효
                .putObjectRequest(putObjectRequest)
                .build();

        // URL 발급
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public String generatePresignedDownloadUrl(Long downloaderId, String fileKey, String contentType){

        return null;
    }
}
