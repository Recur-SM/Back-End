package com.seolstudy.backend.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TaskAttachmentResponse {

    private Long taskId;
    private String attachmentUrl;

    public static TaskAttachmentResponse of(Long taskId, String attachmentUrl) {
        return TaskAttachmentResponse.builder()
                .taskId(taskId)
                .attachmentUrl(attachmentUrl)
                .build();
    }
}
