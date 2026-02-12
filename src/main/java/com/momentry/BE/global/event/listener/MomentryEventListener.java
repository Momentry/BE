package com.momentry.BE.global.event.listener;

import com.momentry.BE.domain.user.entity.User;
import com.momentry.BE.domain.user.service.sub.UserService;
import com.momentry.BE.global.event.dto.AlbumCreateEvent;
import com.momentry.BE.global.event.dto.AlbumInviteEvent;
import com.momentry.BE.global.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MomentryEventListener {

    // TODO : 추후 multi device 환경에서의 개선 필요
    private final UserService userService;
    private final FcmService fcmService;

    // 앨범 생성한 생성자에게 fcm 전송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumCreated(AlbumCreateEvent event) {
        User user = userService.getUser(event.getUserId());

        log.info("앨범 생성 완료 : " + event.getAlbumName());

        List<String> userTokens = List.of(user.getFcmToken());
        String title = "앨범 생성";
        String body = "앨범이 생성되었습니다.";

        Map<String, String> data = new HashMap<>();
        data.put("albumId", event.getAlbumId().toString());
        data.put("albumName", event.getAlbumName());
        data.put("type", "ALBUM_CREATE");

        fcmService.sendMulticastMessage(userTokens, title, body, data);
    }

    // 초대 당한 멤버들에게 fcm 전송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlbumInvited(AlbumInviteEvent event) {
        List<User> users = userService.getUsers(event.getInvitedUserIds());

        log.info("초대 완료 : " + event.getAlbumName());

        List<String> userTokens = users.stream().map(User::getFcmToken).toList();
        String title = "앨범 초대";
        String body = "앨범에 초대되었습니다.";

        Map<String, String> data = new HashMap<>();
        data.put("albumId", event.getAlbumId().toString());
        data.put("albumName", event.getAlbumName());
        data.put("type", "ALBUM_INVITE");

        fcmService.sendMulticastMessage(userTokens, title, body, data);
    }
}
