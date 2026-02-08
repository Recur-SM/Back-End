package com.seolstudy.backend.domain.task.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskMonthlyResponse {

    private Integer year;
    private Integer month;
    private List<TaskMonthlyDateResponse> dates;
    private TaskMonthlySummaryResponse summary;
}
