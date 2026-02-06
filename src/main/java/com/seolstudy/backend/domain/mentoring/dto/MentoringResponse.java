package com.seolstudy.backend.domain.mentoring.dto;

import com.seolstudy.backend.domain.mentoring.entity.Mentoring;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringResponse {

    private Long mentoringId;
    private Long mentorId;
    private String mentorName;
    private Long menteeId;
    private String menteeName;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static MentoringResponse from(Mentoring mentoring) {
        return MentoringResponse.builder()
                .mentoringId(mentoring.getId())
                .mentorId(mentoring.getMentor().getId())
                .mentorName(mentoring.getMentor().getName())
                .menteeId(mentoring.getMentee().getId())
                .menteeName(mentoring.getMentee().getName())
                .isActive(mentoring.getIsActive())
                .createdAt(mentoring.getCreatedAt())
                .build();
    }

}
