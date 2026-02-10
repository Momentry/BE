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
    private FileRepository fileRepository;
    @Autowired
    private EntityManager em; // 삭제 검증을 위해 추가
    @Autowired
    private S3Util s3Util;

    private MockMultipartFile createMockFile(String name, String contentType, String path) throws IOException {
        return new MockMultipartFile("file", name, contentType, new FileInputStream(path));
    }
}