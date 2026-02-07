package com.seolstudy.backend.domain.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FeedbackCheckResponse {

    private Long taskId;
    private Boolean hasFeedback;

    public static FeedbackCheckResponse of(Long taskId, Boolean hasFeedback) {
        return FeedbackCheckResponse.builder()
                .taskId(taskId)
                .hasFeedback(hasFeedback)
                .build();
    }
}