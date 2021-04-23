package ru.example.autoconfigure.metric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MetricStatProviderImpl implements MetricStatProvider {

    private final StorageMethodInvocationMetric storage;

    @Autowired
    public MetricStatProviderImpl(StorageMethodInvocationMetric storage) {
        this.storage = storage;
    }

    @Override
    public List<MethodMetricStat> getTotalStat() {
        List<MethodMetricStat> list = new ArrayList<>();
        for (String name: storage.getUniqueMethodsName()) {
            list.add(getTotalStatByMethod(name));
        }
        return list;
    }

    @Override
    public List<MethodMetricStat> getTotalStatForPeriod(LocalDateTime from, LocalDateTime to) {
        List<MethodMetricStat> list = new ArrayList<>();
        for (String name: storage.getUniqueMethodsName()) {
            list.add(getTotalStatByMethodForPeriod(name, from, to));
        }
        return list;
    }

    @Override
    public MethodMetricStat getTotalStatByMethod(String method) {
        return calculate(storage.getByMethodName(method), method);
    }

    @Override
    public MethodMetricStat getTotalStatByMethodForPeriod(String method, LocalDateTime from, LocalDateTime to) {
        return calculate(storage.getByMethodNameForPeriod(method, from, to), method);
    }

    private MethodMetricStat calculate(List<MethodInvocationMetric> list, String method) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        int sum = 0;
        for (MethodInvocationMetric m: list) {
            int totalTime = m.getTotalTime();
            if (totalTime > max) {
                max = totalTime;
            }
            if (totalTime < min) {
                min = totalTime;
            }
            sum += totalTime;
        }

        int size = list.size();
        return MethodMetricStat.builder()
                .methodName(method)
                .invocationsCount(size)
                .averageTime(size > 1 ? sum / size : sum)
                .minTime(min)
                .maxTime(max)
                .build();
    }
}
