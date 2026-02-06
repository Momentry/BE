package com.momentry.BE.domain.file.entity;

import com.momentry.BE.domain.file.exception.InvalidFileTypeException;
import lombok.Getter;

@Getter
public enum FileType {
    IMAGE, VIDEO;

    public static FileType fromContentType(String contentType){
        if(contentType == null) throw new InvalidFileTypeException();

        if(contentType.startsWith("image")) return IMAGE;
        else if(contentType.startsWith("video")) return VIDEO;

        // 어떤 타입에도 해당하지 않는다면 throw
        throw new InvalidFileTypeException();
    }
}
