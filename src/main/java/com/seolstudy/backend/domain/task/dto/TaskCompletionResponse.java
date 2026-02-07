package com.seolstudy.backend.domain.task.dto;

import com.seolstudy.backend.domain.task.entity.TaskCompletion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TaskCompletionResponse {

    private Long completionId;
    private Long taskId;
    private String completionPhotoUrl;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    public static TaskCompletionResponse from(TaskCompletion completion) {
        return TaskCompletionResponse.builder()
                .completionId(completion.getId())
                .taskId(completion.getTask().getId())
                .completionPhotoUrl(completion.getCompletionPhotoUrl())
                .isCompleted(completion.getIsCompleted())
                .completedAt(completion.getCompletedAt())
                .createdAt(completion.getCreatedAt())
                .build();
    }
}
