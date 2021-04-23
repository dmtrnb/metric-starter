package ru.example.autoconfigure.metric;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@NoArgsConstructor
public class MethodMetricStat {

    private String methodName;
    private Integer invocationsCount;
    private Integer minTime;
    private Integer averageTime;
    private Integer maxTime;
}
