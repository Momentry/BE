package com.momentry.BE.domain.album.util;

import com.momentry.BE.global.config.CloudFrontProperties;
import com.momentry.BE.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 앨범 커버 이미지 표시
 * DB에는 null 또는 S3 key만 저장하고, 조회 시 여기서 최종 URL로 변환한다.
 * - 기본 커버 이미지 (null): CloudFront public URL (쿠키 없음)
 * - 업로드된 커버 이미지: CloudFront url-prefix 경로 (로그인 시 발급 쿠키로 접근)
 */
@Component
@RequiredArgsConstructor
public class CoverImageResolver {

    // CloudFront public 미사용 시 fallback (외부 URL)
    private static final String FALLBACK_DEFAULT_COVER_URL = "https://images.unsplash.com/photo-1511497584788-876760111969?w=800";

    private final S3Util s3Util;
    private final CloudFrontProperties cloudFrontProperties;
    private final CoverImageS3KeyValidator coverImageS3KeyValidator;

    public String resolve(String coverImageUrl) {
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            return resolveDefaultCoverUrl();
        }
        if (coverImageS3KeyValidator.isS3KeyFormat(coverImageUrl)) {
            return resolveUploadedCoverUrl(coverImageUrl);
        }
        // S3 키가 아닌 값(URL, 이상한 문자열)은 클라이언트에 노출하지 않고 기본 커버 반환 (보안)
        return resolveDefaultCoverUrl();
    }

    // 기본 커버 이미지 가져오기 (CloudFront public URL)
    private String resolveDefaultCoverUrl() {
        if (StringUtils.hasText(cloudFrontProperties.getPublicUrlPrefix())
                && StringUtils.hasText(cloudFrontProperties.getDefaultCoverFilename())) {
            return cloudFrontProperties.getPublicUrlPrefix() + cloudFrontProperties.getDefaultCoverFilename();
        }
        return FALLBACK_DEFAULT_COVER_URL;
    }

    // 업로드된 커버 이미지 가져오기 (S3 key): CloudFront 경유(쿠키 검증)
    private String resolveUploadedCoverUrl(String s3Key) {
        if (cloudFrontProperties.isEnabled() && StringUtils.hasText(cloudFrontProperties.getUrlPrefix())) {
            return cloudFrontProperties.getUrlPrefix() + s3Key;
        }
        return s3Util.generatePresignedUrl(s3Key); // cloudfront 사용 불가 시 S3 presigned URL 발급
    }
}
