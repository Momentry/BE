package com.momentry.BE.global.util;

import static org.springframework.util.StringUtils.hasText;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.exception.InvalidFileSizeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
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

    @Value("${app.s3.upload-prefix:original/}")
    private String originalKeyPrefix;

    public void upload(String key, InputStream is, long contentLength) {
        upload(key, is, contentLength, null);
    }

    // S3에 업로드. contentType을 주면 브라우저에서 "열기" 시 다운로드가 아닌 미리보기로 열리도록 설정함.
    public void upload(String key, InputStream is, long contentLength, String contentType) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key);
        if (contentType != null && !contentType.isBlank()) {
            builder.contentType(contentType)
                    .contentDisposition("inline");
        }
        PutObjectRequest putObjectRequest = builder.build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, contentLength));
    }

    // 파일 하나에 연결된 S3 객체(origin, thumbnail, display) 일괄 삭제
    public void deleteS3ObjectsForFile(File file) {
        List<String> keys = new ArrayList<>();
        if (hasText(file.getOriginUrl()))
            keys.add(file.getOriginUrl());
        if (hasText(file.getThumbUrl()))
            keys.add(file.getThumbUrl());
        if (hasText(file.getDisplayUrl()))
            keys.add(file.getDisplayUrl());
        if (keys.isEmpty()) // 삭제할 키가 없으면 스킵
            return;

        try {
            List<ObjectIdentifier> identifiers = keys.stream()
                    .map(k -> ObjectIdentifier.builder().key(k).build())
                    .toList();
            s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(identifiers).build())
                    .build());
        } catch (Exception e) {
            // 예외를 던지는 대신 로그를 남겨서 전체 프로세스가 멈추지 않게 함
            log.error("S3 객체 삭제 실패 (fileId={}): {}", file.getId(), e.getMessage());
        }
    }

    // 단일 S3 객체 삭제
    public void delete(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    /**
     * 앨범에 속한 모든 S3 객체를 albumId 기준 prefix로 일괄 삭제
     * - bucket/albumId/... → thumbnail, display, cover-image
     * - bucket/original/albumId/... → original
     *
     * @param albumId 앨범 ID
     */
    public void deleteAllByAlbumPrefix(Long albumId) {
        String albumPrefix = albumId + "/";
        deleteAllByPrefix(albumPrefix);
        deleteAllByPrefix(originalKeyPrefix + albumPrefix);
    }

    // 주어진 prefix로 시작하는 모든 S3 객체를 조회 후 배치 삭제
    private void deleteAllByPrefix(String prefix) {
        String nextToken = null; // 다음 페이지(1000개 단위) 토큰
        do {
            // prefix로 시작하는 객체 목록 요청
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .continuationToken(nextToken)
                    .build();

            // 응답 받은 객체 목록 (S3가 알아서 1000개 단위로 잘라서 줌)
            ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
            // 빈 리스트가 응답으로 오면 (해당 prefix로 시작하는 객체가 없는 경우) 종료
            if (response.contents().isEmpty()) {
                break;
            }
            // 키만 추출 (삭제 API는 키만 받음)
            List<ObjectIdentifier> identifiers = response.contents().stream()
                    .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                    .toList();
            try {
                s3Client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(identifiers).build())
                        .build());
            } catch (Exception e) {
                log.error("S3 prefix 삭제 실패 (prefix={}): {}", prefix, e.getMessage());
            }
            // 다음 페이지 토큰 갱신
            nextToken = response.nextContinuationToken();
        } while (nextToken != null && !nextToken.isBlank());
    }

    public String generatePresignedUrl(String fileKey) {
        if (!hasText(fileKey))
            return null;

        // 접근할 파일 정의
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .build();

        // Presigned URL 발급 옵션 설정
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // 10분간 유효
                .getObjectRequest(getObjectRequest)
                .build();

        // URL 발급
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public String generatePresignedUploadUrl(Long uploaderId, String fileKey, String contentType, Long contentLength) {
        if (!hasText(fileKey)) return null;
        if (contentLength == null || contentLength <= 0L) {
            throw new InvalidFileSizeException();
        }

        // 업로드될 파일의 위치와 설정 정의
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .metadata(Map.of(
                        "uploaderid", String.valueOf(uploaderId),
                        "expectedcontentlength", String.valueOf(contentLength)
                ))
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

    public String generatePresignedDownloadUrl(String originUrl, String contentType) {
        if (!hasText(originUrl))
            return null;

        // 파일 조회 설정 정의
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(originUrl)
                .responseContentDisposition("attachment;")
                .responseContentType(contentType)
                .build();

        // Presigned URL 발급 옵션 설정
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        // URL 발급
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }
}
