package com.smart.incidentrca.controller;

import com.smart.incidentrca.dto.IncidentRequest;
import com.smart.incidentrca.dto.IncidentResponse;
import com.smart.incidentrca.model.Incident;
import com.smart.incidentrca.model.IncidentStatus;
import com.smart.incidentrca.service.IncidentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Incident APIs", description = "Incident management and RCA operations")
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    //CREATE incident (LLM + rule-based RCA)
    @Operation(summary = "Create a new incident with AI-powered RCA")
    @PostMapping
    public IncidentResponse createIncident(@RequestBody IncidentRequest request) {

        Incident incident = new Incident();
        incident.setIncidentId(request.incidentId());
        incident.setDescription(request.description());
        incident.setSeverity(request.severity());

        Incident saved = incidentService.createIncident(
                incident,
                request.environment(),
                request.symptoms()
        );

        return mapToResponse(saved);
    }

    //GET all incidents
    @Operation(summary = "Fetch all incidents")
    @GetMapping
    public List<IncidentResponse> getAllIncidents() {
        return incidentService.getAllIncidents()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //GET incident by ID
    @Operation(summary = "Fetch incident by ID")
    @GetMapping("/{id}")
    public IncidentResponse getIncidentById(@PathVariable Long id) {
        Incident incident = incidentService.getIncidentById(id);
        return mapToResponse(incident);
    }

    //UPDATE incident status
    @Operation(summary = "Update incident status")
    @PatchMapping("/{id}/status")
    public IncidentResponse updateStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status) {

        Incident incident = incidentService.updateStatus(id, status);
        return mapToResponse(incident);
    }

    //Entity → DTO mapper
    private IncidentResponse mapToResponse(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getIncidentId(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getRootCause(),
                incident.getStatus(),
                incident.getCreatedTime(),
                incident.getResolvedTime()
        );
    }
}
