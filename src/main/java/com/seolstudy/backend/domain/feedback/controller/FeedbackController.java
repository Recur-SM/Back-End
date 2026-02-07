package com.seolstudy.backend.domain.feedback.controller;

import com.seolstudy.backend.domain.feedback.dto.*;
import com.seolstudy.backend.domain.feedback.service.FeedbackService;
import com.seolstudy.backend.global.payload.CommonResponse;
import com.seolstudy.backend.global.payload.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "피드백", description = "피드백 관련 API")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "피드백 생성", description = "멘토가 과제에 대한 피드백을 작성합니다")
    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<FeedbackResponse> createFeedback(
            @Valid @RequestBody FeedbackCreateRequest request) {
        FeedbackResponse response = feedbackService.createFeedback(request);
        return CommonResponse.of(SuccessStatus.CREATED, response);
    }

    @Operation(summary = "피드백 상세 조회", description = "피드백 상세 정보를 조회합니다")
    @GetMapping("/{feedbackId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'MENTEE')")
    public CommonResponse<FeedbackResponse> getFeedback(@PathVariable Long feedbackId) {
        FeedbackResponse response = feedbackService.getFeedback(feedbackId);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "멘티의 피드백 목록 조회", description = "특정 멘티의 모든 피드백을 조회합니다")
    @GetMapping("/mentee/{menteeId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'MENTEE')")
    public CommonResponse<FeedbackListResponse> getFeedbacksByMentee(@PathVariable Long menteeId) {
        FeedbackListResponse response = feedbackService.getFeedbacksByMentee(menteeId);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "피드백 작성 여부 확인", description = "특정 과제에 피드백이 작성되었는지 확인합니다")
    @GetMapping("/check/{taskId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'MENTEE')")
    public CommonResponse<FeedbackCheckResponse> checkFeedback(@PathVariable Long taskId) {
        FeedbackCheckResponse response = feedbackService.checkFeedback(taskId);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "피드백 수정", description = "피드백 내용을 수정합니다")
    @PutMapping("/{feedbackId}")
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<FeedbackResponse> updateFeedback(
            @PathVariable Long feedbackId,
            @Valid @RequestBody FeedbackUpdateRequest request) {
        FeedbackResponse response = feedbackService.updateFeedback(feedbackId, request);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "피드백 삭제", description = "피드백을 삭제합니다")
    @DeleteMapping("/{feedbackId}")
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<String> deleteFeedback(@PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return CommonResponse.of(SuccessStatus.NO_CONTENT, "피드백이 삭제되었습니다.");
    }
}