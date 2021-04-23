package ru.example.autoconfigure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;

@Data
@Validated
@ConfigurationProperties(prefix = "metrics")
public class MetricProperties {

    private boolean enabled = false;
    @Positive int limit = 10000;
}
