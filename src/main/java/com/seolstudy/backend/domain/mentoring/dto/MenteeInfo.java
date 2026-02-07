package com.seolstudy.backend.domain.mentoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MenteeInfo {

    private Long menteeId;
    private String menteeName;
    private String username;
}
