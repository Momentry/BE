package com.momentry.BE.domain.file.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.momentry.BE.domain.file.dto.*;
import com.momentry.BE.domain.file.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileLike;
import com.momentry.BE.domain.file.exception.AlreadyLikedException;
import com.momentry.BE.domain.file.exception.FileNotFoundException;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.domain.user.repository.UserRepository;
import com.momentry.BE.global.util.S3Util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {
    private final S3Util s3Util;
    private final FileUtil fileUtil;
    private final AlbumPermissionService albumPermissionService;
    private final FileRepository fileRepository;
    private final FileLikeRepository fileLikeRepository;
    private final UserRepository userRepository;
    private final FileTagInfoRepository fileTagInfoRepository;

    @Value("${cloudfront.url-prefix}")
    private String CLOUDFRONT_URL_PREFIX;

    @Transactional
    public FileUploadResponseDto getFileUploadUrls(Long uploaderId, Long albumId, FileUploadRequestDto getFileUploadUrlsRequestDtoList){
        // 유저 권한 체크
        albumPermissionService.checkPermission(uploaderId, albumId, MemberAlbumPermission.EDITOR);

        List<UploadUrlResponseDto> uploadUrlList = new ArrayList<>();
        for(UploadFileInfoDto fileInfo : getFileUploadUrlsRequestDtoList.getUploadFileInfoList()){
            // 각 파일에 uuid 부여
            String fileId = UUID.randomUUID().toString();

            // 확장자 추출
            String extension = fileUtil.getExtension(fileInfo.getContentType());

            // fileKey 생성 ( original/{albumId}/{uuid}.{extension}
            String fileKey = "original/" + albumId + "/" + fileId + extension;

            // upload용 presigned url 생성
            String uploadUrl = s3Util.generatePresignedUploadUrl(uploaderId, fileKey, fileInfo.getContentType());

            uploadUrlList.add(
                    UploadUrlResponseDto.builder()
                            .fileNo(fileInfo.getFileNo())
                            .uploadUrl(uploadUrl)
                            .build()
            );
        }

        return FileUploadResponseDto.of(uploadUrlList);
    }

    @Transactional
    public void deleteFiles(Long userId, Long albumId, List<Long> targetFileIds) {
        // 해당 앨범의 EDITOR 이상 권한이 있어야 삭제 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);

        // 삭제 대상 일괄 조회
        List<File> targetFiles = fileRepository.findAllById(targetFileIds);

        // 파일 삭제
        for (File targetFile : targetFiles) {
            // TODO: 해당 파일이 진짜 그 앨범 소속이 맞는지 체크?

            // S3에서 삭제
            s3Util.deleteAll(targetFile);
        }
        // DB에서 파일 정보 일괄 삭제
        fileRepository.deleteAllInBatch(targetFiles);
    }

    @Transactional
    public void deleteFile(Long userId, Long albumId, Long targetFileId) {
        File targetFile = fileRepository.findById(targetFileId)
                .orElseThrow(FileNotFoundException::new);
        s3Util.deleteAll(targetFile);
        fileRepository.delete(targetFile);
    }

    @Transactional
    public void likeFile(Long userId, Long albumId, Long targetFileId) {
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
    public void unlikeFile(Long userId, Long albumId, Long targetFileId) {
        // 해당 앨범의 VIEWER 이상 권한이 있어야 좋아요 취소 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.VIEWER);

        // 좋아요 데이터 삭제
        fileLikeRepository.deleteByFileIdAndUserId(targetFileId, userId);

        // 해당 파일의 좋아요 수 감소
        File file = fileRepository.findById(targetFileId)
                .orElseThrow(FileNotFoundException::new);
        file.decrementLikesCount();
    }

    @Transactional
    public GetFileDetailResponseDto getFileDetail(Long userId, Long albumId, Long targetFileId) {
        // 해당 앨범의 VIEWER 이상 권한이 있어야 상세 정보 조회 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.VIEWER);

        // 파일 조회
        File targetFile = fileRepository.findByIdWithUploader(targetFileId)
                .orElseThrow(FileNotFoundException::new);

        // 좋아요 여부 확인
        Boolean isLiked = fileLikeRepository.existsByFileIdAndUserId(targetFileId, userId);

        // 태그 목록 조회
        List<Long> tags = fileTagInfoRepository.findTagIdsByFileId(targetFileId);

        // 업로더의 프로필 이미지 접근용 url 제공
        String uploaderProfileImageUrl = targetFile.getUploader().getProfileImageUrl();

        // DTO로 변환 후 반환
        return GetFileDetailResponseDto.of(targetFile, tags, isLiked, uploaderProfileImageUrl);
    }

    // SQS 메시지 수신 시 해당 파일의 thumbnail, display path를 업데이트
    @Transactional
    public void updateThumbDisplayPathOfFile(String fileKey, String thumbnailPath, String displayPath, String metadata, String createdAt) {
        File file = fileRepository.findByFileKey(fileKey)
                .orElseThrow(FileNotFoundException::new);

        // 리사이징된 경로 업데이트
        file.updatePostProcessingResults(
                CLOUDFRONT_URL_PREFIX + thumbnailPath,
                CLOUDFRONT_URL_PREFIX + displayPath,
                metadata,
                createdAt==null ? LocalDateTime.now() : LocalDateTime.parse(createdAt)
        );

        log.info("파일 정보 업데이트 완료: ID={}", file.getId());
    }
}