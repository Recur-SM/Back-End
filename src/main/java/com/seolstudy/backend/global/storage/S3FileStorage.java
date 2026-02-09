package com.seolstudy.backend.global.storage;

import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

    private static final String PLANNER_PREFIX = "uploads/planners/";
    private static final String TASK_ATTACHMENT_PREFIX = "uploads/task-attachments/";
    private static final String TASK_COMPLETION_PREFIX = "uploads/task-completions/";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${AWS_S3_BUCKET_NAME}")
    private String bucket;

    @Override
    public String uploadPlannerImage(MultipartFile image) {
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

        String key = PLANNER_PREFIX + fileName;
        putObject(key, contentType, image, ErrorStatus.PLANNER_IMAGE_UPLOAD_FAILED);
        return key;
    }

    @Override
    public String uploadTaskAttachment(MultipartFile file) {
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

        String key = TASK_ATTACHMENT_PREFIX + fileName;
        putObject(key, contentType, file, ErrorStatus.TASK_ATTACHMENT_UPLOAD_FAILED);
        return key;
    }

    @Override
    public String uploadTaskCompletionImage(MultipartFile image) {
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

        String key = TASK_COMPLETION_PREFIX + fileName;
        putObject(key, contentType, image, ErrorStatus.TASK_COMPLETION_IMAGE_UPLOAD_FAILED);
        return key;
    }

    @Override
    public void deleteObject(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("S3 객체 삭제 실패 - key: {}", key, e);
        }
    }

    @Override
    public String createPresignedGetUrl(String key, Duration duration) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        URL url = s3Presigner.presignGetObject(presignRequest).url();
        return url.toString();
    }

    private void putObject(String key, String contentType, MultipartFile file, ErrorStatus errorStatus) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            log.error("S3 업로드 실패 - key: {}", key, e);
            throw new GeneralException(errorStatus);
        }
    }
}
