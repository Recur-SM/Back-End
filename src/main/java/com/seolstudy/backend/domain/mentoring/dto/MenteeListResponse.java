package com.seolstudy.backend.domain.mentoring.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MenteeListResponse {

    private Long mentorId;
    private String mentorName;
    private List<MenteeInfo> mentees;
}
