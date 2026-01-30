package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.file.dto.FileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Test
    @DisplayName("실제 S3 버킷에 이미지 업로드 테스트")
    void uploadImageFileToS3Test() throws IOException {
        // 1. 테스트용 가짜 파일 생성 (MockMultipartFile)
        // 실제 로컬에 있는 이미지 파일을 읽어서 테스트에 사용합니다.
        String fileName = "test-image";
        String contentType = "image/jpeg";
        String filePath = "src/test/resources/test.jpg";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                fileName + ".jpg",
                contentType,
                new FileInputStream(filePath)
        );

        // 2. 메타데이터 생성
        String metadata = "{테스트 메타데이터}";

        // 3. 서비스 호출 (실제 S3 업로드 로직 실행)
        Long testAlbumId = 1L;
        Long testUserId = 1L;
        FileResult result = fileService.uploadFile(testUserId, testAlbumId, mockFile, metadata, LocalDateTime.now());

        // 4. 검증
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("실제 S3 버킷에 비디오 업로드 테스트")
    void uploadVideoFileToS3Test() throws IOException {
        // 1. 테스트용 가짜 파일 생성 (MockMultipartFile)
        // 실제 로컬에 있는 이미지 파일을 읽어서 테스트에 사용합니다.
        String fileName = "test-video";
        String contentType = "video/quicktime";
        String filePath = "src/test/resources/test.mov";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                fileName + ".mov",
                contentType,
                new FileInputStream(filePath)
        );

        // 2. 메타데이터 생성
        String metadata = "{테스트 메타데이터}";

        // 3. 서비스 호출 (실제 S3 업로드 로직 실행)
        Long testAlbumId = 1L;
        Long testUserId = 1L;
        FileResult result = fileService.uploadFile(testUserId, testAlbumId, mockFile, metadata, LocalDateTime.now());

        // 4. 검증
        assertThat(result).isNotNull();
    }
}