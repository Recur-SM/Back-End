package com.seolstudy.backend.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedBackCreateRequest {

    @NotNull(message = "과제 ID는 필수입니다.")
    private Long taskId;

    @NotNull(message = "멘티 ID는 필수입니다.")
    private Long menteeId;

    @NotNull(message = "멘토 ID는 필수입니다.")
    private Long mentorId;

    @NotNull(message = "과목 ID는 필수입니다.")
    private Long subjectId;

    @NotBlank(message = "피드백 날짜는 필수입니다.")
    private String feedbackDate; // yyyy-MM-dd

    private String detailContent;
}
