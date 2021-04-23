package ru.example.autoconfigure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.example.autoconfigure.bean_post_processor.TimedBeanPostProcessor;
import ru.example.autoconfigure.metric.MetricStatProvider;
import ru.example.autoconfigure.metric.MetricStatProviderImpl;
import ru.example.autoconfigure.metric.StorageMethodInvocationMetric;

@Configuration
@EnableConfigurationProperties(MetricProperties.class)
@ConditionalOnProperty(prefix = "metrics", value = "enabled", havingValue = "true")
public class MetricConfig {

    @Bean
    public MetricProperties metricProperties() {
        return new MetricProperties();
    }

    @Bean
    public StorageMethodInvocationMetric storageMethodInvocationMetric() {
        return new StorageMethodInvocationMetric(metricProperties());
    }

    @Bean
    public MetricStatProvider metricStatProvider() {
        return new MetricStatProviderImpl(storageMethodInvocationMetric());
    }

    @Bean
    public TimedBeanPostProcessor timedBeanPostProcessor() {
        return new TimedBeanPostProcessor(storageMethodInvocationMetric());
    }
}
