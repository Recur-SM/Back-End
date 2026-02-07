package com.seolstudy.backend.domain.mentoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MentorResponse {

    private Long menteeId;
    private String menteeName;
    private Long mentorId;
    private String mentorName;
    private String mentorUsername;
}
