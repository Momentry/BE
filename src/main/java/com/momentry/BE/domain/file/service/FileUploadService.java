package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.dto.SaveFileDto;
import com.momentry.BE.domain.file.dto.UploadFileDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.exception.FileStorageException;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final AlbumPermissionService albumPermissionService;
    private final FileRepository fileRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final S3Util s3Util;

    @Transactional
    public List<FileResult> uploadFiles(Long uploaderId, Long albumId, List<MultipartFile> files) {
        // 유저 권한 체크
        albumPermissionService.checkPermission(uploaderId, albumId, MemberAlbumPermission.EDITOR);

        List<FileResult> fileResultList = new ArrayList<>();
        for (MultipartFile file : files) {
            // TODO: 라이브러리 사용해서 메타데이터 추출 후 파라미터로 넘겨주는 코드 추가하기
            UploadFileDto uploadFileDto = UploadFileDto.of(uploaderId, albumId, file, null, null);
            FileResult newFile = uploadFile(uploadFileDto);
            fileResultList.add(newFile);
        }

        return fileResultList;
    }

    @Transactional
    public FileResult uploadFile(UploadFileDto uploadFileDto) {
        // TODO: 파일 업로드 시 예외 처리
        // 3. 최대 업로드 가능한 크기 초과한 경우

        MultipartFile file = uploadFileDto.getFile();
        Long albumId = uploadFileDto.getAlbumId();

        // 파일 타입(Image, Video) 체크
        FileType fileType = FileType.fromContentType(file.getContentType());

        // S3에 저장할 고유한 파일명 생성 (UUID 활용)
        String fileId = UUID.randomUUID().toString();
        String extension = extractExtension(file.getOriginalFilename());

        // 파일 업로드 로직
        try {
            // 원본 파일 저장 경로 ( original/{albumId}/{fileId}.{extension} )
            String originalFilePath = "original/" + albumId + "/" + fileId + extension;

            // 파일 타입에 따른 업로드 로직 분기
            if (fileType == FileType.IMAGE) {
                // 이미지 업로드
                uploadImage(file, originalFilePath);
            } else if (fileType == FileType.VIDEO) {
                // 비디오 업로드
                uploadVideo(file, originalFilePath);
            }

            // DB에 파일 정보 저장
            File uploadedFile = saveFileInfo(
                    SaveFileDto.builder()
                            .uploaderId(uploadFileDto.getUploaderId())
                            .albumId(albumId)
                            .fileType(fileType)
                            .metadata(uploadFileDto.getMetadata())
                            .capturedAt(uploadFileDto.getCapturedAt())
                            .originalPath(originalFilePath)
                            .fileKey(fileId)
                        .build()
            );

            // 업로드 결과 반환
            return FileResult.of(uploadedFile);
        } catch (IOException e) {
            throw new FileStorageException();
        }
    }

    @Transactional
    public void uploadImage(
            MultipartFile imageFile,
            String originalFilePath) throws IOException {
        // 원본 파일 업로드
        s3Util.upload(
                originalFilePath,
                imageFile.getInputStream(),
                imageFile.getSize());

        // 1차/2차 파일 압축 -> Lambda에서 수행
    }

    @Transactional
    public void uploadVideo(
            MultipartFile videoFile,
            String originalFilePath) throws IOException {
        // 원본 파일 업로드
        s3Util.upload(
                originalFilePath,
                videoFile.getInputStream(),
                videoFile.getSize());

        // 썸네일 추출 + 압축 -> Lambda에서 수행
    }

    @Transactional
    public File saveFileInfo(
            SaveFileDto saveFileDto
    ) {
        // 유저, 앨범 프록시 객체 생성
        User uploader = userRepository.getReferenceById(saveFileDto.getUploaderId());
        Album album = albumRepository.getReferenceById(saveFileDto.getAlbumId());

        // 저장할 파일 정보 객체 생성
        File uploadedFile = File.builder()
                .uploader(uploader)
                .album(album)
                .originUrl(saveFileDto.getOriginalPath())
                .thumbUrl(saveFileDto.getThumbnailPath())
                .displayUrl(saveFileDto.getDisplayPath())
                .fileType(saveFileDto.getFileType())
                .metadata(saveFileDto.getMetadata())
                .fileKey(saveFileDto.getFileKey())
                .capturedAt(saveFileDto.getCapturedAt())
                .build();

        // DB에 저장
        fileRepository.save(uploadedFile);

        // 저장된 파일 정보 반환
        return uploadedFile;
    }



    // ========== 내부 private 메서드 ==========
    private String extractExtension(String fileName) {
        // 파일 이름이 null이면 확장자는 빈 문자열로 반환
        if (fileName == null)
            return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
