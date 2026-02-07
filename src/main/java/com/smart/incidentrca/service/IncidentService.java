package com.smart.incidentrca.service;

import com.smart.incidentrca.llm.LlmService;
import com.smart.incidentrca.model.Incident;
import com.smart.incidentrca.model.IncidentStatus;
import com.smart.incidentrca.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final LlmService llmService;

    @Autowired
    public IncidentService(
            IncidentRepository incidentRepository,
            LlmService llmService
    ) {
        this.incidentRepository = incidentRepository;
        this.llmService = llmService;
    }

    //Create incident with Rule-based + AI RCA
    public Incident createIncident(
            Incident incident,
            String environment,
            List<String> symptoms
    ) {
        incident.setStatus(IncidentStatus.OPEN);

        //Rule-based RCA (fallback)
        applyRuleBasedRca(incident);

        //AI-powered RCA (override if successful)
        try {
            String aiRca = llmService.generateRca(
                    incident.getDescription(),
                    incident.getSeverity(),
                    environment,
                    symptoms
            );
            incident.setRootCause(aiRca);
        } catch (Exception e) {
            // fallback RCA remains
        }

        return incidentRepository.save(incident);
    }

    //Rule-based RCA fallback
    private void applyRuleBasedRca(Incident incident) {
        if (incident.getDescription() == null) return;

        String desc = incident.getDescription().toLowerCase();

        if (desc.contains("cpu")) {
            incident.setRootCause("High CPU usage detected");
        } else if (desc.contains("database")) {
            incident.setRootCause("Database outage suspected");
        } else if (desc.contains("network")) {
            incident.setRootCause("Network connectivity issue suspected");
        } else {
            incident.setRootCause("Cause unclear - manual investigation required");
        }
    }

    //Read operations
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Incident getIncidentById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
    }

    //Status lifecycle handler
    public Incident updateStatus(Long id, IncidentStatus status) {
        Incident incident = getIncidentById(id);

        switch (status) {
            case IN_PROGRESS -> incident.setStatus(IncidentStatus.IN_PROGRESS);

            case RESOLVED -> {
                if (incident.getResolvedTime() == null) {
                    incident.setResolvedTime(LocalDateTime.now());
                }
                incident.setStatus(IncidentStatus.RESOLVED);
            }

            case CLOSED -> {
                if (incident.getResolvedTime() == null) {
                    throw new IllegalStateException("Resolve incident before closing");
                }
                incident.setStatus(IncidentStatus.CLOSED);
            }

            default -> throw new IllegalArgumentException("Invalid status");
        }

        return incidentRepository.save(incident);
    }
}
