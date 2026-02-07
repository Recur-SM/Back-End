package com.seolstudy.backend.domain.mentoring.controller;

import com.seolstudy.backend.domain.mentoring.dto.MenteeListResponse;
import com.seolstudy.backend.domain.mentoring.dto.MentorResponse;
import com.seolstudy.backend.domain.mentoring.dto.MentoringCreateRequest;
import com.seolstudy.backend.domain.mentoring.dto.MentoringResponse;
import com.seolstudy.backend.domain.mentoring.service.MentoringService;
import com.seolstudy.backend.global.payload.CommonResponse;
import com.seolstudy.backend.global.payload.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "멘토링", description = "멘토-멘티 관계 관리 API")
@RestController
@RequestMapping("/api/mentoring")
@RequiredArgsConstructor
public class MentoringController {

    private final MentoringService mentoringService;

    @Operation(summary = "멘티 등록", description = "멘토가 새로운 멘티를 등록합니다")
    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<MentoringResponse> createMentoring(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MentoringCreateRequest request) {
        MentoringResponse response = mentoringService.createMentoring(userDetails.getUsername(), request);
        return CommonResponse.of(SuccessStatus.CREATED, response);
    }

    @Operation(summary = "멘티 등록 해제", description = "멘토가 멘티 등록을 해제합니다")
    @DeleteMapping("/{menteeId}")
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<String> deactivateMentoring(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long menteeId) {
        mentoringService.deactivateMentoring(userDetails.getUsername(), menteeId);
        return CommonResponse.of(SuccessStatus.NO_CONTENT, "멘티 등록이 해제되었습니다.");
    }

    @Operation(summary = "담당 멘티 목록 조회", description = "멘토가 담당하는 멘티 목록을 조회합니다")
    @GetMapping("/mentees")
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<MenteeListResponse> getMentees(
            @AuthenticationPrincipal UserDetails userDetails) {
        MenteeListResponse response = mentoringService.getMentees(userDetails.getUsername());
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "담당 멘토 조회", description = "멘티의 담당 멘토를 조회합니다")
    @GetMapping("/mentor")
    @PreAuthorize("hasRole('MENTEE')")
    public CommonResponse<MentorResponse> getMentor(
            @AuthenticationPrincipal UserDetails userDetails) {
        MentorResponse response = mentoringService.getMentor(userDetails.getUsername());
        return CommonResponse.onSuccess(response);
    }
}