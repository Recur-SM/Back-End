package com.seolstudy.backend.domain.task.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class TaskListResponse {

    private LocalDate plannerDate;
    private List<TaskResponse> tasks;
}