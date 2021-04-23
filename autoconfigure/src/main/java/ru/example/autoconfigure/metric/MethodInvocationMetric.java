package ru.example.autoconfigure.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
@AllArgsConstructor
public class MethodInvocationMetric {

    private final String method;
    private final LocalDateTime invocationTime;
    private final Integer totalTime;
}
