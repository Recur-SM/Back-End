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
public class TaskCreateResponse {

    private Long taskId;
    private Long menteeId;
    private Long mentorId;
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
    private Boolean isFixed;
    private LocalDateTime createdAt;

    public static TaskCreateResponse from(Task task, String subjectName, String subjectCode) {
        return TaskCreateResponse.builder()
                .taskId(task.getId())
                .menteeId(task.getMentee().getId())
                .mentorId(task.getMentor().getId())
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
                .isFixed(task.getIsFixed())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
