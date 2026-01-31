package com.momentry.BE.global.util;

import com.momentry.BE.domain.file.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class S3Util {
    private final S3Client s3Client;

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
            throw new RuntimeException("S3 파일 삭제 실패", e);
        }
    }

    public void delete(String key){
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
}
