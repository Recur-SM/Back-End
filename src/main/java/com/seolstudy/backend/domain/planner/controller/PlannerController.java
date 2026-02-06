package com.seolstudy.backend.domain.planner.controller;

import com.seolstudy.backend.domain.planner.dto.PlannerCommentRequest;
import com.seolstudy.backend.domain.planner.dto.PlannerCommentResponse;
import com.seolstudy.backend.domain.planner.dto.PlannerCreateRequest;
import com.seolstudy.backend.domain.planner.dto.PlannerCreateResponse;
import com.seolstudy.backend.domain.planner.service.PlannerService;
import com.seolstudy.backend.global.payload.CommonResponse;
import com.seolstudy.backend.global.payload.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

@Tag(name = "플래너", description = "일자별 플래너 API")
@RestController
@RequestMapping("/api/planners")
@RequiredArgsConstructor
@Validated
public class PlannerController {

    private final PlannerService plannerService;

    @Operation(summary = "일자별 플래너 등록", description = "멘티가 일자별 플래너를 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MENTEE')")
    public CommonResponse<PlannerCreateResponse> createPlanner(
            @NotNull @RequestParam("menteeId") Long menteeId,
            @NotBlank @RequestParam("plannerDate") String plannerDate,
            @NotBlank @RequestParam("content") String content,
            @RequestPart("image") MultipartFile image
    ) {
        PlannerCreateRequest request = PlannerCreateRequest.builder()
                .menteeId(menteeId)
                .plannerDate(plannerDate)
                .content(content)
                .build();
        PlannerCreateResponse response = plannerService.createPlanner(request, image);
        return CommonResponse.of(SuccessStatus.CREATED, response);
    }

    @Operation(summary = "일자별 플래너 코멘트 등록", description = "멘토가 일자별 플래너에 코멘트를 등록합니다.")
    @PostMapping("/comment")
    @PreAuthorize("hasRole('MENTOR')")
    public CommonResponse<PlannerCommentResponse> addMentorComment(
            @Valid @RequestBody PlannerCommentRequest request
    ) {
        PlannerCommentResponse response = plannerService.addMentorComment(request);
        return CommonResponse.onSuccess(response);
    }

}
