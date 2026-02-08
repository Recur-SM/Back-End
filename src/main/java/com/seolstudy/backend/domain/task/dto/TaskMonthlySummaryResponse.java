package com.seolstudy.backend.domain.task.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskMonthlySummaryResponse {

    private Integer totalDays;
    private Integer daysWithTasks;
    private Integer totalTasks;
    private Integer completedTasks;
    private Double completionRate;
}
