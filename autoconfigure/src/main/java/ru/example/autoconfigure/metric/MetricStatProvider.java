package ru.example.autoconfigure.metric;

import java.time.LocalDateTime;
import java.util.List;

public interface MetricStatProvider {

    List<MethodMetricStat> getTotalStat();

    List<MethodMetricStat> getTotalStatForPeriod(LocalDateTime from, LocalDateTime to);

    MethodMetricStat getTotalStatByMethod(String method);

    MethodMetricStat getTotalStatByMethodForPeriod(String method, LocalDateTime from, LocalDateTime to);
}
