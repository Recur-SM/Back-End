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
public class PlannerCommentResponse {

    private Long plannerId;
    private Long menteeId;
    private Long mentorId;
    private LocalDate plannerDate;
    private String mentorComment;
    private LocalDateTime updatedAt;

    public static PlannerCommentResponse from(Planner planner) {
        return PlannerCommentResponse.builder()
                .plannerId(planner.getId())
                .menteeId(planner.getMenteeId())
                .mentorId(planner.getMentorId())
                .plannerDate(planner.getPlannerDate())
                .mentorComment(planner.getMentorComment())
                .updatedAt(planner.getUpdatedAt())
                .build();
    }
}
