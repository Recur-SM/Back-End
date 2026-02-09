package com.seolstudy.backend.global.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalFileStorageAdapter implements FileStorage {

    private final LocalFileStorage localFileStorage;

    @Override
    public String uploadPlannerImage(MultipartFile image) {
        return localFileStorage.storePlannerImage(image);
    }

    @Override
    public String uploadTaskAttachment(MultipartFile file) {
        return localFileStorage.storeTaskAttachment(file);
    }

    @Override
    public String uploadTaskCompletionImage(MultipartFile image) {
        return localFileStorage.storeTaskCompletionImage(image);
    }

    @Override
    public void deleteObject(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (key.contains("task-attachments")) {
            localFileStorage.deleteTaskAttachment(key);
        } else if (key.contains("task-completions")) {
            localFileStorage.deleteTaskCompletionImage(key);
        }
    }

    @Override
    public String createPresignedGetUrl(String key, Duration duration) {
        return key;
    }
}
