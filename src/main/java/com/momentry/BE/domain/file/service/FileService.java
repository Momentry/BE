package com.momentry.BE.domain.file.service;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.file.dto.FileResult;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileLike;
import com.momentry.BE.domain.file.entity.FileType;
import com.momentry.BE.domain.file.exception.AlreadyLikedException;
import com.momentry.BE.domain.file.exception.FileNotFoundException;
import com.momentry.BE.domain.file.exception.FileStorageException;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {
    private final S3Util s3Util;
    private final AlbumPermissionService albumPermissionService;
    private final FileRepository fileRepository;
    private final FileLikeRepository fileLikeRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    @Transactional
    public FileResult uploadFile(Long uploaderId, Long albumId, MultipartFile file, String metadata, LocalDateTime createdAt){
        // TODO: 파일 업로드 시 예외 처리
        // 1. 앨범이 없는 경우
        // 2. 앨범에 업로드할 권한이 없는 경우
        albumPermissionService.checkPermission(uploaderId, albumId, MemberAlbumPermission.EDITOR);
        // 3. 최대 업로드 가능한 크기 초과한 경우



        // 파일 타입(Image, Video) 체크
        FileType fileType = FileType.fromContentType(file.getContentType());

        // S3에 저장할 고유한 파일명 생성 (UUID 활용)
        String fileId = UUID.randomUUID().toString();
        String extension = extractExtension(file.getOriginalFilename());

        // 파일 업로드 로직
        try{
            // 원본 파일 저장 경로 ( original/{albumId}/{fileId}.{extension} )
            String originalFilePath = "original/" + albumId + "/" + fileId + extension;

            // 파일 타입에 따른 업로드 로직 분기
            if(fileType == FileType.IMAGE){
                // 이미지 업로드
                uploadImage(file, originalFilePath);
            }else if(fileType == FileType.VIDEO){
                // 비디오 업로드
                uploadVideo(file, originalFilePath);
            }

            // DB에 파일 정보 저장
            File uploadedFile = saveFileInfo(
                    uploaderId,
                    albumId,
                    fileType,
                    metadata,
                    createdAt,
                    originalFilePath,
                    null,
                    null
            );

            // 업로드 결과 반환
            return FileResult.of(uploadedFile);
        }catch(IOException e){
            throw new FileStorageException();
        }
    }

    public void uploadImage(
            MultipartFile imageFile,
            String originalFilePath
    ) throws IOException{
        // 원본 파일 업로드
        s3Util.upload(
                originalFilePath,
                imageFile.getInputStream(),
                imageFile.getSize()
        );

        // 1차/2차 파일 압축 -> Lambda에서 수행
    }

    public void uploadVideo(
            MultipartFile videoFile,
            String originalFilePath
    ) throws IOException{
        // 원본 파일 업로드
        s3Util.upload(
                originalFilePath,
                videoFile.getInputStream(),
                videoFile.getSize()
        );

        // 썸네일 추출 + 압축 -> Lambda에서 수행
    }


    @Transactional
    public void deleteFile(Long userId, Long albumId, Long targetFileId){
        // 해당 앨범의 EDITOR 이상 권한이 있어야 삭제 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);

        File targetFile = fileRepository.findById(targetFileId)
                .orElseThrow(FileNotFoundException::new);
        s3Util.deleteAll(targetFile);
        fileRepository.delete(targetFile);
    }

    @Transactional
    public void likeFile(Long userId, Long albumId, Long targetFileId){
        // 해당 앨범의 VIEWER 이상 권한이 있어야 좋아요 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.VIEWER);

        // 중복 좋아요 체크 (이미 좋아요를 눌렀는지 확인)
        if (fileLikeRepository.existsByFileIdAndUserId(targetFileId, userId)) {
            throw new AlreadyLikedException(); // 중복 방지
        }

        FileLike fileLike = FileLike.builder()
                .file(fileRepository.getReferenceById(targetFileId))
                .user(userRepository.getReferenceById(userId))
                .build();

        // DB에 저장
        fileLikeRepository.save(fileLike);

        // 해당 파일의 좋아요 수 증가
        File file = fileRepository.findById(targetFileId)
                .orElseThrow(FileNotFoundException::new);
        file.incrementLikesCount();
    }

    @Transactional
    public void unlikeFile(Long userId, Long albumId, Long targetFileId){
        // 해당 앨범의 VIEWER 이상 권한이 있어야 좋아요 취소 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.VIEWER);

        // 좋아요 데이터 삭제
        fileLikeRepository.deleteByFileIdAndUserId(targetFileId, userId);

        // 해당 파일의 좋아요 수 감소
        File file = fileRepository.findById(targetFileId)
                .orElseThrow(FileNotFoundException::new);
        file.decrementLikesCount();
    }


    // ========== 내부 사용 메서드 ==========
    private File saveFileInfo(
            Long uploaderId,
            Long albumId,
            FileType fileType,
            String metadata,
            LocalDateTime createdAt,
            String originUrl,
            String thumbUrl,
            String displayUrl
    ){
        // 유저, 앨범 프록시 객체 생성
        User uploader = userRepository.getReferenceById(uploaderId);
        Album album = albumRepository.getReferenceById(albumId);

        // 저장할 파일 정보 객체 생성
        File uploadedFile = File.builder()
                .uploader(uploader)
                .album(album)
                .originUrl(originUrl)
                .thumbUrl(thumbUrl)
                .displayUrl(displayUrl)
                .fileType(fileType)
                .metadata(metadata)
                .createdAt(createdAt)
                .build();

        // DB에 저장
        fileRepository.save(uploadedFile);

        // 저장된 파일 정보 반환
        return uploadedFile;
    }

    private String extractExtension(String fileName){
        // 파일 이름이 null이면 확장자는 빈 문자열로 반환
        if(fileName == null) return "";
        return fileName.substring(fileName.lastIndexOf("."));
    }
}