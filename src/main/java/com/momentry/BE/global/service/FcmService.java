package com.momentry.BE.global.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.momentry.BE.global.exception.FcmTokenEmptyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * FCM 서버로 푸시 알림을 전송하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    /**
     * FCM 서버로 멀티캐스트 메시지를 전송하는 메서드.
     *
     * @param tokens  알림을 받을 디바이스 FCM 토큰 리스트
     * @param title   알림 타이틀
     * @param body    알림 내용
     * @param data    추가 데이터 (화면 이동 등에 사용)
     */
    public void sendMulticastMessage(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {
        try {
            MulticastMessage message = buildMulticastMessage(tokens, title, body, data);
            sendMessage(message);
        } catch (Exception e) {
            log.error("FCM 멀티캐스트 메시지 전송 실패", e);
        }
    }

    private MulticastMessage buildMulticastMessage(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("토큰 리스트가 비어 있습니다.");
            throw new FcmTokenEmptyException();
        }

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setSound("default")
                                .setChannelId("new_posts_channel")
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .putHeader("apns-priority", "10")
                        .build())
                .addAllTokens(tokens);

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        return builder.build();
    }

    private void sendMessage(MulticastMessage message) throws Exception {
        BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(message);

        log.info("FCM 전송 결과: 성공={}, 실패={}, 전체={}",
                batchResponse.getSuccessCount(),
                batchResponse.getFailureCount(),
                batchResponse.getResponses().size());

        if (batchResponse.getFailureCount() > 0) {
            List<SendResponse> responses = batchResponse.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse sr = responses.get(i);
                if (!sr.isSuccessful()) {
                    log.warn("FCM 전송 실패 [인덱스 {}]: {}",
                            i,
                            sr.getException() != null ? sr.getException().getMessage() : "unknown");
                }
            }
        }
    }
}


