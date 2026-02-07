package com.seolstudy.backend.domain.feedback.dto;

import com.seolstudy.backend.domain.feedback.entity.Feedback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FeedbackResponse {

    private Long feedbackId;
    private Long taskId;
    private String taskName;
    private Long menteeId;
    private String menteeName;
    private Long mentorId;
    private String mentorName;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private LocalDate feedbackDate;
    private String detailContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FeedbackResponse from(Feedback feedback, String taskName, String subjectName, String subjectCode) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .taskId(feedback.getTask().getId())
                .taskName(taskName)
                .menteeId(feedback.getMentee().getId())
                .menteeName(feedback.getMentee().getName())
                .mentorId(feedback.getMentor().getId())
                .mentorName(feedback.getMentor().getName())
                .subjectId(feedback.getSubjectId())
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .feedbackDate(feedback.getFeedbackDate())
                .detailContent(feedback.getDetailContent())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
