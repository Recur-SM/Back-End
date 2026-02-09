package com.seolstudy.backend.domain.planner.dto;

import com.seolstudy.backend.domain.planner.entity.Planner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class PlannerCreateResponse {

    private Long plannerId;
    private Long menteeId;
    private LocalDate plannerDate;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static PlannerCreateResponse from(Planner planner, String imageUrl) {
        return PlannerCreateResponse.builder()
                .plannerId(planner.getId())
                .menteeId(planner.getMenteeId())
                .plannerDate(planner.getPlannerDate())
                .content(planner.getContent())
                .imageUrl(imageUrl)
                .createdAt(planner.getCreatedAt())
                .build();
    }
}
