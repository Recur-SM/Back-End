package com.seolstudy.backend.domain.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class TaskListBySubjectResponse {

    private LocalDate plannerDate;
    private String subjectName;
    private String subjectCode;
    private List<TaskResponse> tasks;
}
