package com.seolstudy.backend.domain.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class FeedbackListResponse {

    private Long menteeId;
    private String menteeName;
    private List<FeedbackResponse> feedbacks;
}