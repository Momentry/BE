package com.momentry.BE.global.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import com.momentry.BE.global.exception.CursorDecodeFailException;

public class CursorUtil {
    
    public static String[] decodeCursorParts(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|", 2);
            if (parts.length != 2) {
                throw new CursorDecodeFailException();
            }
            return parts;
        } catch (IllegalArgumentException e) {
            throw new CursorDecodeFailException();
        }
    }

    public static String encodeCursor(LocalDateTime createdAt, Long id) {
        String payload = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeUserCursor(String username, String email, Long id) {
        String encodedUsername = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(username.getBytes(StandardCharsets.UTF_8));
        String encodedEmail = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(email.getBytes(StandardCharsets.UTF_8));
        return encodedUsername + "." + encodedEmail + "." + id;
    }

    public static String[] decodeUserCursorParts(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String[] parts = cursor.split("\\.", 3);
            if (parts.length != 3) {
                throw new CursorDecodeFailException();
            }
            String username = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String email = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return new String[] {username, email, parts[2]};
        } catch (IllegalArgumentException e) {
            throw new CursorDecodeFailException();
        }
    }
}
