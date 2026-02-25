package com.momentry.BE.global.event.listener;

import com.momentry.BE.domain.album.service.AlbumInfoUpdateService;
import com.momentry.BE.domain.album.service.AlbumService;
import com.momentry.BE.domain.album.util.CoverImageS3KeyValidator;
import com.momentry.BE.domain.file.exception.InvalidFileTypeException;
import com.momentry.BE.domain.file.util.FileUtil;
import com.momentry.BE.global.event.dto.AlbumCoverUploadEvent;
import com.momentry.BE.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlbumCoverUploadEventListener {
    private final FileUtil fileUtil;
    private final S3Util s3Util;
    private final AlbumInfoUpdateService albumInfoUpdateService;
    private final CoverImageS3KeyValidator coverImageS3KeyValidator;

    @Async("s3UploadExecutor") // 별도의 스레드 풀 사용
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // DB 저장 완료 후 실행
    public void uploadAlbumCover(AlbumCoverUploadEvent event){
        // 유효한 타입인지 검사
        if (!fileUtil.isAllowedCoverImageType(event.getFile().getContentType())) {
            throw new InvalidFileTypeException();
        }

        // 앨범당 커버는 하나만 유지
        // S3 key일 때만 S3 객체 삭제 (URL 혹은 이상한 값은 스킵)
        String currentCover = event.getPrevAlbumCoverUrl();
        if (coverImageS3KeyValidator.isS3KeyFormat(currentCover)) {
            try {
                s3Util.delete(currentCover);
            } catch (Exception e) {
                log.warn("기존 커버 이미지 S3 삭제 실패 (albumId={}, key={})", event.getAlbumId(), currentCover, e);
            }
        }

        // 파일 키 생성
        String fileId = UUID.randomUUID().toString();
        String extension = fileUtil.getExtension(event.getFile().getContentType());
        String fileKey = event.getAlbumId() + "/" + coverImageS3KeyValidator.getCoverImageFolder() + fileId + extension;

        // S3 업로드
        try {
            s3Util.upload(fileKey, event.getFile().getInputStream(), event.getFile().getSize(), event.getFile().getContentType());
            albumInfoUpdateService.updateAlbumInfo(event.getAlbum(), null, fileKey);
            log.info("앨범 커버 이미지 S3 업로드 성공 (albumId={})", event.getAlbumId());
        } catch (IOException e) {
            log.warn("커버 이미지 업로드 실패, 기존 이미지 유지 (albumId={})", event.getAlbumId(), e);

            // TODO: FCM으로 앨범 커버 업로드 실패 사실 전달하기
        }
    }
}
