package com.seolstudy.backend.domain.task.controller;

import com.seolstudy.backend.domain.task.dto.TaskCompletionResponse;
import com.seolstudy.backend.domain.task.dto.TaskCreateRequest;
import com.seolstudy.backend.domain.task.dto.TaskCreateResponse;
import com.seolstudy.backend.domain.task.dto.TaskListBySubjectResponse;
import com.seolstudy.backend.domain.task.dto.TaskListResponse;
import com.seolstudy.backend.domain.task.service.TaskService;
import com.seolstudy.backend.global.payload.CommonResponse;
import com.seolstudy.backend.global.payload.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@Tag(name = "오늘 할일", description = "오늘 할일 관련 API")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "오늘 할일 전체 조회", description = "오늘 할일 전체를 조회합니다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('MENTEE', 'MENTOR')")
    public CommonResponse<TaskListResponse> getTasks(
            @RequestParam("mentee_id") Long menteeId,
            @RequestParam(value = "date", required = false) String date
    ) {
        TaskListResponse response = taskService.getTasks(menteeId, date);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "특정 과목 할일 조회", description = "특정 과목 할일을 조회합니다.")

    @GetMapping("/subject")
    @PreAuthorize("hasAnyRole('MENTEE', 'MENTOR')")
    public CommonResponse<TaskListBySubjectResponse> getTasksBySubject(
            @RequestParam("mentee_id") Long menteeId,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam("subject_code") String subjectCode
    ) {
        TaskListBySubjectResponse response = taskService.getTasksBySubject(menteeId, date, subjectCode);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "오늘 할일 추가", description = "오늘 할일을 추가합니다.")

    @PostMapping
    @PreAuthorize("hasAnyRole('MENTEE', 'MENTOR')")
    public CommonResponse<TaskCreateResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request
    ) {
        TaskCreateResponse response = taskService.createTask(request);
        return CommonResponse.of(SuccessStatus.CREATED, response);
    }

    @Operation(summary = "과제 제출", description = "완료 인증 사진을 제출합니다.")
    @PostMapping(value = "/{task_id}/submit", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('MENTEE')")
    public CommonResponse<TaskCompletionResponse> submitTask(
            @PathVariable("task_id") Long taskId,
            @RequestPart(value = "completion_photo", required = false) MultipartFile completionPhoto
    ) {
        TaskCompletionResponse response = taskService.submitTask(taskId, completionPhoto);
        return CommonResponse.of(SuccessStatus.CREATED, response);
    }
}