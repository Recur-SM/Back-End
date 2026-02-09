package com.seolstudy.backend.domain.task.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskDetailResponse {

    private Long taskId;
    private LocalDate taskDate;
    private String taskName;
    private String taskGoal;
    private TaskType taskType;
    private LearningMaterialType learningMaterialType;
    private String pdfFileUrl;
    private String columnContent;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskDetailResponse from(Task task, String pdfFileUrl) {
        return TaskDetailResponse.builder()
                .taskId(task.getId())
                .taskDate(task.getTaskDate())
                .taskName(task.getTaskName())
                .taskGoal(task.getTaskGoal())
                .taskType(task.getTaskType())
                .learningMaterialType(task.getLearningMaterialType())
                .pdfFileUrl(pdfFileUrl)
                .columnContent(task.getColumnContent())
                .isFixed(task.getIsFixed())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
