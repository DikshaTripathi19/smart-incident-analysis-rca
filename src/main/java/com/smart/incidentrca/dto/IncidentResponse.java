package com.smart.incidentrca.dto;

import java.time.LocalDateTime;

import com.smart.incidentrca.model.IncidentStatus;

public record IncidentResponse(
        Long id,
        String incidentId,
        String description,
        String severity,
        String rootCause,
        IncidentStatus status,
        LocalDateTime createdTime,
        LocalDateTime resolvedTime
) {}
