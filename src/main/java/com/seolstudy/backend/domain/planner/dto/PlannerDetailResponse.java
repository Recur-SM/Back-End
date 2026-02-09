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
public class PlannerDetailResponse {

    private Long plannerId;
    private Long menteeId;
    private Long mentorId;
    private LocalDate plannerDate;
    private String content;
    private String imageUrl;
    private String mentorComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlannerDetailResponse from(Planner planner) {
        return PlannerDetailResponse.builder()
                .plannerId(planner.getId())
                .menteeId(planner.getMenteeId())
                .mentorId(planner.getMentorId())
                .plannerDate(planner.getPlannerDate())
                .content(planner.getContent())
                .imageUrl(planner.getImageUrl())
                .mentorComment(planner.getMentorComment())
                .createdAt(planner.getCreatedAt())
                .updatedAt(planner.getUpdatedAt())
                .build();
    }
}
