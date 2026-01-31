package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.util.S3Util;
import jakarta.persistence.EntityNotFoundException;
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
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    public FileResult uploadFile(Long uploaderId, Long albumId, MultipartFile file, String metadata, LocalDateTime createdAt){
        // TODO: 파일 업로드 시 예외 처리
        // 1. 앨범이 없는 경우
        // 2. 앨범에 업로드할 권한이 없는 경우 -> AOP로 분리
        // 3. 최대 업로드 가능한 크기 초과한 경우?

        // 파일 타입(Image, Video) 체크
        FileType fileType = FileType.fromContentType(file.getContentType());

        // S3에 저장할 고유한 파일명 생성 (UUID 활용)
        String fileId = UUID.randomUUID().toString();
        String extension = extractExtension(file.getOriginalFilename());

        // 파일 타입에 따른 업로드 로직 분기
        try{
            if(fileType == FileType.IMAGE){
                // 이미지 업로드
                uploadImage(albumId, fileId, extension, file);
            }else if(fileType == FileType.VIDEO){
                // 비디오 업로드
                uploadVideo(albumId, fileId, extension, file);
            }

            // DB에 파일 정보 저장
            File uploadedFile = saveFileInfo(uploaderId, albumId, fileType, metadata, createdAt);

            // 업로드 결과 반환
            return FileResult.of(uploadedFile);
        }catch(IOException e){
            throw new RuntimeException("파일 처리 중 오류 발생", e);
        }
    }

    public void uploadImage(Long albumId, String fileId, String extension, MultipartFile imageFile) throws IOException{
        String basePath = "albums/" + albumId + "/" + fileId + "/";
        // 원본 파일 업로드
        s3Util.upload(
                basePath + "original" + extension,
                imageFile.getInputStream(),
                imageFile.getSize()
        );

        // TODO: 1차/2차 파일 압축
    }

    public void uploadVideo(Long albumId, String fileId, String extension, MultipartFile videoFile) throws IOException{
        String basePath = "albums/" + albumId + "/" + fileId + "/";

        // 원본 파일 업로드
        s3Util.upload(
                basePath + "original" + extension,
                videoFile.getInputStream(),
                videoFile.getSize()
        );

        // TODO: 썸네일 추출 + 압축
    }

    public File saveFileInfo(Long uploaderId, Long albumId, FileType fileType, String metadata, LocalDateTime createdAt){
        // 유저, 앨범 프록시 객체 생성
        User uploader = userRepository.getReferenceById(uploaderId);
        Album album = albumRepository.getReferenceById(albumId);

        // 저장할 파일 정보 객체 생성
        File uploadedFile = File.builder()
                .uploader(uploader)
                .album(album)
                // TODO: null값 채우기
                .thumbUrl(null)
                .displayUrl(null)
                .fileType(fileType)
                .metadata(metadata)
                .createdAt(createdAt)
                .build();

        // DB에 저장
        fileRepository.save(uploadedFile);

        // 저장된 파일 정보 반환
        return uploadedFile;
    }

    public String extractExtension(String fileName){
        // 파일 이름이 null이면 확장자는 빈 문자열로 반환
        if(fileName == null) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}