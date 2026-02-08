package com.seolstudy.backend.domain.task.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskMonthlyDateResponse {

    private LocalDate date;
    private String dayOfWeek;
    private List<TaskMonthlyItemResponse> tasks;
    private Integer totalTasks;
    private Integer completedTasks;
}
