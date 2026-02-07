package com.seolstudy.backend.domain.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackUpdateRequest {

    private String detailContent;
}
