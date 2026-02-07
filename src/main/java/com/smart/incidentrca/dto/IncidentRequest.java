package com.smart.incidentrca.dto;

import java.util.List;

public record IncidentRequest(
        String incidentId,
        String description,
        String severity,
        String environment,
        List<String> symptoms
) {}
