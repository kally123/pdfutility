package com.pdfutility.common.event;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base event structure following Event-Driven Architecture guidelines.
 * Events are immutable and include all required metadata for tracing.
 */
@Value
@Builder
public class BaseEvent<T> {
    String eventId;
    String eventType;
    LocalDateTime eventTime;
    String source;
    String subject;
    String dataVersion;
    T data;
    EventMetadata metadata;

    public static <T> BaseEvent<T> create(String eventType, String source, String subject, T data) {
        return BaseEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .source(source)
                .subject(subject)
                .dataVersion("1.0")
                .data(data)
                .metadata(EventMetadata.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    public static <T> BaseEvent<T> create(String eventType, String source, String subject, T data, String correlationId) {
        return BaseEvent.<T>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .source(source)
                .subject(subject)
                .dataVersion("1.0")
                .data(data)
                .metadata(EventMetadata.builder()
                        .correlationId(correlationId)
                        .build())
                .build();
    }

    @Value
    @Builder
    public static class EventMetadata {
        String correlationId;
        String causationId;
        String userId;
        Map<String, String> additionalProperties;
    }
}
