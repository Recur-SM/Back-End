package com.seolstudy.backend.domain.task.dto;

import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long taskId;
    private String subjectName;
    private String subjectCode;
    private String taskName;
    private LocalDate taskDate;
    private String taskGoal;
    private TaskType taskType;
    private LearningMaterialType learningMaterialType;
    private String pdfFileUrl;
    private String columnContent;
    private String comment;
    private Boolean isCompleted;
    private Integer studyTime;
    private String completionPhotoUrl;
    private LocalDateTime completedAt;
    private Boolean hasFeedback;

    public static TaskResponse from(Task task, String subjectName, String subjectCode, Boolean hasFeedback) {
        return TaskResponse.builder()
                .taskId(task.getId())
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .taskName(task.getTaskName())
                .taskDate(task.getTaskDate())
                .taskGoal(task.getTaskGoal())
                .taskType(task.getTaskType())
                .learningMaterialType(task.getLearningMaterialType())
                .pdfFileUrl(task.getPdfFileUrl())
                .columnContent(task.getColumnContent())
                .comment(task.getComment())
                .isCompleted(false)
                .studyTime(null)
                .completionPhotoUrl(null)
                .completedAt(null)
                .hasFeedback(hasFeedback)
                .build();
    }
}
