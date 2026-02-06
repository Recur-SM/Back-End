package com.seolstudy.backend.domain.planner.dto;

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
public class PlannerCommentRequest {

    @NotNull(message = "플래너 ID는 필수입니다.")
    private Long plannerId;

    @NotNull(message = "멘티 ID는 필수입니다.")
    private Long menteeId;

    @NotNull(message = "멘토 ID는 필수입니다.")
    private Long mentorId;

    @NotBlank(message = "멘토 코멘트는 필수입니다.")
    private String mentorComment;
}
