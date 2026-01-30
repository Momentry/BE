package com.momentry.BE.domain.file.entity;

import lombok.Getter;

@Getter
public enum FileType {
    IMAGE, VIDEO;

    public static FileType fromContentType(String contentType){
        if(contentType == null) throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");

        if(contentType.startsWith("image")) return IMAGE;
        else if(contentType.startsWith("video")) return VIDEO;

        // 어떤 타입에도 해당하지 않는다면 throw
        // TODO: 커스텀 예외 클래스 만들어서 대체하기
        throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
    }
}
