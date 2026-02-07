package com.seolstudy.backend.global.storage;

import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorage {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String storePlannerImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_PLANNER_IMAGE);
        }

        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new GeneralException(ErrorStatus.INVALID_PLANNER_IMAGE_TYPE);
        }

        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String fileName = UUID.randomUUID().toString();
        if (StringUtils.hasText(extension)) {
            fileName += "." + extension;
        }

        Path plannerDir = Paths.get(uploadDir, "planners");
        try {
            Files.createDirectories(plannerDir);
            Path target = plannerDir.resolve(fileName);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("플래너 이미지 저장 실패 - uploadDir: {}", uploadDir, e);
            throw new GeneralException(ErrorStatus.PLANNER_IMAGE_UPLOAD_FAILED);
        }

        return "/uploads/planners/" + fileName;
    }

    public String storeTaskCompletionImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_COMPLETION_IMAGE);
        }

        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_COMPLETION_IMAGE_TYPE);
        }

        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String fileName = UUID.randomUUID().toString();
        if (StringUtils.hasText(extension)) {
            fileName += "." + extension;
        }

        Path completionDir = Paths.get(uploadDir, "task-completions");
        try {
            Files.createDirectories(completionDir);
            Path target = completionDir.resolve(fileName);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("과제 완료 이미지 저장 실패 - uploadDir: {}", uploadDir, e);
            throw new GeneralException(ErrorStatus.TASK_COMPLETION_IMAGE_UPLOAD_FAILED);
        }

        return "/uploads/task-completions/" + fileName;
    }

    public String storeTaskAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_ATTACHMENT);
        }

        String contentType = file.getContentType();
        boolean isPdf = StringUtils.hasText(contentType) && contentType.equalsIgnoreCase("application/pdf");
        boolean isImage = StringUtils.hasText(contentType) && contentType.startsWith("image/");
        if (!isPdf && !isImage) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_ATTACHMENT_TYPE);
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString();
        if (StringUtils.hasText(extension)) {
            fileName += "." + extension;
        } else if (isPdf) {
            fileName += ".pdf";
        } else {
            fileName += ".img";
        }

        Path attachmentDir = Paths.get(uploadDir, "task-attachments");
        try {
            Files.createDirectories(attachmentDir);
            Path target = attachmentDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("과제 첨부파일 저장 실패 - uploadDir: {}", uploadDir, e);
            throw new GeneralException(ErrorStatus.TASK_ATTACHMENT_UPLOAD_FAILED);
        }

        return "/uploads/task-attachments/" + fileName;
    }

    public void deleteTaskCompletionImage(String photoUrl) {
        if (!StringUtils.hasText(photoUrl)) {
            return;
        }

        Path fileName = Paths.get(photoUrl).getFileName();
        if (fileName == null) {
            return;
        }

        Path completionDir = Paths.get(uploadDir, "task-completions");
        Path target = completionDir.resolve(fileName.toString());
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("과제 완료 이미지 삭제 실패 - photoUrl: {}", photoUrl, e);
        }
    }

    public void deleteTaskAttachment(String attachmentUrl) {
        if (!StringUtils.hasText(attachmentUrl)) {
            return;
        }

        Path fileName = Paths.get(attachmentUrl).getFileName();
        if (fileName == null) {
            return;
        }

        Path attachmentDir = Paths.get(uploadDir, "task-attachments");
        Path target = attachmentDir.resolve(fileName.toString());
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("과제 첨부파일 삭제 실패 - attachmentUrl: {}", attachmentUrl, e);
        }
    }
}
