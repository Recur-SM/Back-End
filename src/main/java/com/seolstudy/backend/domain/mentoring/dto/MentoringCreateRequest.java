package com.seolstudy.backend.domain.mentoring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringCreateRequest {

    @NotNull(message = "멘티 ID는 필수입니다.")
    private Long menteeId;
}
