package com.momentry.BE.domain.file.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.momentry.BE.domain.album.entity.Album;
import com.momentry.BE.domain.album.entity.MemberAlbumPermission;
import com.momentry.BE.domain.album.repository.AlbumRepository;
import com.momentry.BE.domain.album.service.AlbumPermissionService;
import com.momentry.BE.domain.file.dto.DownloadUrlDto;
import com.momentry.BE.domain.file.dto.FileDownloadRequestDto;
import com.momentry.BE.domain.file.dto.FileDownloadResponseDto;
import com.momentry.BE.domain.file.dto.FileUploadRequestDto;
import com.momentry.BE.domain.file.dto.FileUploadResponseDto;
import com.momentry.BE.domain.file.dto.GetFileDetailResponseDto;
import com.momentry.BE.domain.file.dto.SaveFileDto;
import com.momentry.BE.domain.file.dto.UploadFileInfoDto;
import com.momentry.BE.domain.file.dto.UploadUrlDto;
import com.momentry.BE.domain.file.entity.File;
import com.momentry.BE.domain.file.entity.FileLike;
import com.momentry.BE.domain.file.exception.AlreadyLikedException;
import com.momentry.BE.domain.file.exception.FileNotFoundException;
import com.momentry.BE.domain.file.repository.FileLikeRepository;
import com.momentry.BE.domain.file.repository.FileRepository;
import com.momentry.BE.domain.file.repository.FileTagInfoRepository;
import com.momentry.BE.domain.file.util.FileUtil;
import com.momentry.BE.domain.user.entity.User;
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
    private final AlbumRepository albumRepository;
    private final FileRepository fileRepository;
    private final FileLikeRepository fileLikeRepository;
    private final UserRepository userRepository;
    private final FileTagInfoRepository fileTagInfoRepository;

    @Value("${cloudfront.url-prefix}")
    private String CLOUDFRONT_URL_PREFIX;

    @Value("${app.s3.upload-prefix:original/}")
    private String FILEKEY_PREFIX;

    @Transactional
    public FileUploadResponseDto getFileUploadUrls(Long uploaderId, Long albumId,
            FileUploadRequestDto getFileUploadUrlsRequestDtoList) {
        // 유저 권한 체크
        albumPermissionService.checkPermission(uploaderId, albumId, MemberAlbumPermission.EDITOR);

        List<UploadUrlDto> uploadUrlList = new ArrayList<>();
        for (UploadFileInfoDto fileInfo : getFileUploadUrlsRequestDtoList.getUploadFileInfoList()) {
            // 각 파일에 uuid 부여
            String fileId = UUID.randomUUID().toString();

            // 확장자 추출
            String extension = fileUtil.getExtension(fileInfo.getContentType());

            // fileKey 생성 ( original/{albumId}/{uuid}.{extension}
            String fileKey = FILEKEY_PREFIX + albumId + "/" + fileId + extension;

            // upload용 presigned url 생성
            String uploadUrl = s3Util.generatePresignedUploadUrl(uploaderId, fileKey, fileInfo.getContentType());

            uploadUrlList.add(
                    UploadUrlDto.builder()
                            .fileNo(fileInfo.getFileNo())
                            .uploadUrl(uploadUrl)
                            .build());
        }

        return FileUploadResponseDto.of(uploadUrlList);
    }

    @Transactional
    public FileDownloadResponseDto getFileDownloadUrls(Long downloaderId, Long albumId,
            FileDownloadRequestDto getFileDownloadUrlRequestDto) {
        // 유저 권한 체크
        albumPermissionService.checkPermission(downloaderId, albumId, MemberAlbumPermission.VIEWER);

        List<Long> fileIdList = getFileDownloadUrlRequestDto.getDownloadFileIdList();

        // 파일 조회
        List<File> fileList = fileRepository.findAllById(fileIdList);

        // 다운로드 URL 생성
        List<DownloadUrlDto> downloadUrlList = new ArrayList<>();
        for (File file : fileList) {
            String downloadUrl = s3Util.generatePresignedDownloadUrl(file.getOriginUrl(), file.getContentType());
            downloadUrlList.add(
                    DownloadUrlDto.builder()
                            .downloadUrl(downloadUrl)
                            .fileId(file.getId())
                            .contentType(file.getContentType())
                            .build());
        }

        return new FileDownloadResponseDto(downloadUrlList);
    }

    @Transactional
    public void deleteFiles(Long userId, Long albumId, List<Long> targetFileIds) {
        if (targetFileIds.isEmpty())
            return;

        // 해당 앨범의 EDITOR 이상 권한이 있어야 삭제 가능
        albumPermissionService.checkPermission(userId, albumId, MemberAlbumPermission.EDITOR);

        // 삭제 대상 일괄 조회
        List<File> targetFiles = fileRepository.findAllById(targetFileIds);

        // 파일 삭제
        for (File targetFile : targetFiles) {
            // TODO: 해당 파일이 진짜 그 앨범 소속이 맞는지 체크?

            // S3에서 삭제
            s3Util.deleteS3ObjectsForFile(targetFile);
        }
        // TODO: DDL 레벨에서 Cascade 옵션 주기 (일단은 수동으로 지우는걸로 대체 -> 테이블을 다시 만들어야해서!)
        // DB에서 파일 정보 일괄 삭제
        // 좋아요 정보 삭제
        fileLikeRepository.deleteAllByFileIdIn(targetFileIds);
        // 태그 정보 삭제
        fileTagInfoRepository.deleteAllByFileIdIn(targetFileIds);
        // 파일 정보 삭제
        fileRepository.deleteAllInBatch(targetFiles);
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

    @Transactional
    public void saveFileInfo(
            SaveFileDto saveFileDto) {
        // 유저, 앨범 프록시 객체 생성
        User uploader = userRepository.getReferenceById(saveFileDto.getUploaderId());
        Album album = albumRepository.getReferenceById(saveFileDto.getAlbumId());

        // 저장할 파일 정보 객체 생성
        File uploadedFile = File.builder()
                .uploader(uploader)
                .album(album)
                .originUrl(saveFileDto.getOriginalPath())
                .thumbUrl(CLOUDFRONT_URL_PREFIX + saveFileDto.getThumbnailPath())
                .displayUrl(CLOUDFRONT_URL_PREFIX + saveFileDto.getDisplayPath())
                .fileType(saveFileDto.getFileType())
                .metadata(saveFileDto.getMetadata())
                .contentType(saveFileDto.getContentType())
                .fileKey(saveFileDto.getFileKey())
                .capturedAt(saveFileDto.getCapturedAt())
                .build();

        // DB에 저장
        fileRepository.save(uploadedFile);
    }
}