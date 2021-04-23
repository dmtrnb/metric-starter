package ru.example.autoconfigure.metric;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.example.autoconfigure.config.MetricProperties;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class StorageMethodInvocationMetric {

    private final MetricProperties properties;

    @Autowired
    public StorageMethodInvocationMetric(MetricProperties metricProperties) {
        this.properties = metricProperties;
    }

    private final ConcurrentMap<String, List<MethodInvocationMetric>> storage = new ConcurrentHashMap<>();

    public void add(MethodInvocationMetric metric) {
        storage.compute(metric.getMethod(), (k, v) -> {
            if (v == null) {
                v = new LinkedList<>();
            } else if (v.size() == properties.getLimit()) {
                v.remove(0);
            }
            v.add(metric);
            return v;
        });
    }

    public List<MethodInvocationMetric> getByMethodName(String name) {
        return storage.getOrDefault(name, new LinkedList<>()).stream()
                .filter((i) -> i.getMethod().equals(name))
                .collect(Collectors.toList());
    }

    public List<MethodInvocationMetric> getByMethodNameForPeriod(String name, LocalDateTime from, LocalDateTime to) {
        Predicate<MethodInvocationMetric> predicate = (item) -> {
            LocalDateTime time = item.getInvocationTime();
            return item.getMethod().equals(name) && (time.equals(from) || time.isAfter(from)) && time.isBefore(to);
        };
        return storage.getOrDefault(name, new LinkedList<>()).stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public Set<String> getUniqueMethodsName() {
        return storage.keySet();
    }
}
