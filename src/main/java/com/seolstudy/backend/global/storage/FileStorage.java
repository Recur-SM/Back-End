package com.seolstudy.backend.global.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface FileStorage {

    String uploadPlannerImage(MultipartFile image);

    String uploadTaskAttachment(MultipartFile file);

    String uploadTaskCompletionImage(MultipartFile image);

    void deleteObject(String key);

    String createPresignedGetUrl(String key, Duration duration);
}
