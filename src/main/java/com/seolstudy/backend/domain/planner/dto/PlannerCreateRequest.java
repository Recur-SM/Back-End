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
public class PlannerCreateRequest {

    @NotNull(message = "멘티 ID는 필수입니다.")
    private Long menteeId;

    @NotBlank(message = "플래너 날짜는 필수입니다.")
    private String plannerDate; // yyyy-MM-dd

    @NotBlank(message = "플래너 내용은 필수입니다.")
    private String content;
}
