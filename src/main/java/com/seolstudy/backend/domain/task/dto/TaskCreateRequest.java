package com.seolstudy.backend.domain.task.dto;

import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {

    @NotNull(message = "멘티 ID는 필수입니다.")
    private Long menteeId;

    @NotNull(message = "멘토 ID는 필수입니다.")
    private Long mentorId;

    @NotBlank(message = "과목 코드는 필수입니다.")
    private String subjectCode;

    @NotBlank(message = "과제 이름은 필수입니다.")
    @Size(max = 100, message = "과제 이름은 100자 이하여야 합니다.")
    private String taskName;

    @NotBlank(message = "과제 날짜는 필수입니다.")
    private String taskDate; // yyyy-MM-dd

    private String taskGoal;

    @NotNull(message = "과제 타입은 필수입니다.")
    private TaskType taskType;

    private LearningMaterialType learningMaterialType;

    private String pdfFileUrl;

    private String columnContent;

    private String comment;
}