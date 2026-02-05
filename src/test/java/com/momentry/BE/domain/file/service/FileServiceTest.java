package com.momentry.BE.domain.file.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.dto.UploadFileDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.global.util.S3Util;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class FileServiceTest {

    @Autowired
    private FileService fileService;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private EntityManager em; // 삭제 검증을 위해 추가
    @Autowired
    private S3Util s3Util;

    // @Test
    // @Transactional(propagation = Propagation.NOT_SUPPORTED) // 트랜잭션 미반영
    // @DisplayName("파일 업로드 및 DB 삭제 검증 테스트 (이미지/비디오 공통)")
    // void uploadAndDeleteVerificationTest() throws IOException {
    // // [준비] 1번 유저(Manager), 1번 앨범 사용
    // Long userId = 1L;
    // Long albumId = 1L;
    // String metadata = "{\"desc\": \"삭제 검증 테스트\"}";
    //
    // MockMultipartFile imageFile = createMockFile("test.jpg", "image/jpeg",
    // "src/test/resources/test.jpg");
    // MockMultipartFile videoFile = createMockFile("test.mov", "video/quicktime",
    // "src/test/resources/test.mov");
    //
    // // [실행 & 검증] 이미지 프로세스
    // processUploadAndDelete(userId, albumId, imageFile, metadata);
    //
    // // [실행 & 검증] 비디오 프로세스
    // processUploadAndDelete(userId, albumId, videoFile, metadata);
    // }

    private void processUploadAndDelete(Long userId, Long albumId, MockMultipartFile file, String metadata)
            throws IOException {
        // 1. 업로드
        FileResult result = fileUploadService.uploadFile(
                UploadFileDto.of(userId, albumId, file, metadata, null));

        Long fileId = result.getId();
        assertThat(fileRepository.existsById(fileId)).isTrue();

        // displayUrl, thumbnailUrl이 반영되기까지 대기
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .until(() -> {
                    // em.clear() 대신 findById를 새로 호출하여 최신 DB 데이터 확인
                    File fileInfo = fileRepository.findById(fileId).orElse(null);
                    return fileInfo != null && fileInfo.getThumbUrl() != null;
                });

        // 2. 삭제
        fileService.deleteFile(userId, albumId, fileId);

        // 3. DB 반영 강제 수행 및 영속성 컨텍스트 초기화 (삭제 확인을 위한 핵심)
        // em.flush();
        // em.clear();

        // 4. 삭제 검증 (DB에 데이터가 없어야 함)
        assertThat(fileRepository.existsById(fileId)).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 트랜잭션 미반영
    @DisplayName("이미지 파일 업로드 단일 테스트")
    void uploadImageOnlyTest() throws IOException {
        // [준비]
        Long userId = 1L;
        Long albumId = 1L;
        MockMultipartFile imageFile = createMockFile("test.jpg", "image/jpeg", "src/test/resources/test.jpg");
        String metadata = "{\"description\": \"이미지 업로드 단일 테스트\"}";

        // [실행]
        FileResult result = fileUploadService.uploadFile(
                UploadFileDto.of(userId, albumId, imageFile, metadata, null));

        // [검증]
        assertThat(result).isNotNull();
        assertThat(fileRepository.existsById(result.getId())).isTrue();

        // 롤백 확인을 위해 로그 출력 (실제 DB 반영 안 됨)
        System.out.println("업로드된 이미지 ID: " + result.getId());

        // PresignedURL 테스트
        System.out.println("Presigned URL : " + s3Util.generatePresignedUrl(result.getUrl()));
    }

    // @Test
    // @DisplayName("비디오 파일 업로드 단일 테스트")
    // void uploadVideoOnlyTest() throws IOException {
    // // [준비]
    // Long userId = 1L;
    // Long albumId = 1L;
    // MockMultipartFile videoFile = createMockFile("test.mov", "video/quicktime",
    // "src/test/resources/test.mov");
    // String metadata = "{\"description\": \"비디오 업로드 단일 테스트\"}";
    //
    // // [실행]
    // FileResult result = fileUploadService.uploadFile(userId, albumId, videoFile,
    // metadata, LocalDateTime.now());
    //
    // // [검증]
    // assertThat(result).isNotNull();
    // assertThat(fileRepository.existsById(result.getId())).isTrue();
    //
    // System.out.println("업로드된 비디오 ID: " + result.getId());
    // }

    private MockMultipartFile createMockFile(String name, String contentType, String path) throws IOException {
        return new MockMultipartFile("file", name, contentType, new FileInputStream(path));
    }
}