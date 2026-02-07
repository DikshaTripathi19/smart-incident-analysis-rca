package com.smart.incidentrca.service.test;

import com.smart.incidentrca.llm.LlmService;
import com.smart.incidentrca.model.Incident;
import com.smart.incidentrca.model.IncidentStatus;
import com.smart.incidentrca.repository.IncidentRepository;
import com.smart.incidentrca.service.IncidentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncidentServiceTest {

    private IncidentRepository incidentRepository;
    private LlmService llmService;
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        llmService = mock(LlmService.class);
        incidentService = new IncidentService(incidentRepository, llmService);
    }

    @Test
    void testCreateIncident_withAiRca() throws Exception {
        Incident incident = new Incident();
        incident.setDescription("CPU spike on production server");
        incident.setSeverity("HIGH");

        when(llmService.generateRca(anyString(), anyString(), anyString(), anyList()))
                .thenReturn("AI Root Cause Detected");
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident saved = incidentService.createIncident(
                incident,
                "PRODUCTION",
                List.of("High CPU")
        );

        assertEquals(IncidentStatus.OPEN, saved.getStatus());
        assertEquals("AI Root Cause Detected", saved.getRootCause());
    }

    @Test
    void testCreateIncident_llmFails_ruleBasedRca() throws Exception {
        Incident incident = new Incident();
        incident.setDescription("Database timeout");
        incident.setSeverity("MEDIUM");

        when(llmService.generateRca(anyString(), anyString(), anyString(), anyList()))
                .thenThrow(new RuntimeException("LLM down"));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident saved = incidentService.createIncident(
                incident,
                "PRODUCTION",
                List.of("DB timeout")
        );

        assertEquals(IncidentStatus.OPEN, saved.getStatus());
        assertEquals("Database outage suspected", saved.getRootCause());
    }

    @Test
    void testGetAllIncidents() {
        List<Incident> incidents = List.of(new Incident(), new Incident());
        when(incidentRepository.findAll()).thenReturn(incidents);

        List<Incident> result = incidentService.getAllIncidents();
        assertEquals(2, result.size());
    }

    @Test
    void testGetIncidentById_found() {
        Incident incident = new Incident();
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Incident result = incidentService.getIncidentById(1L);
        assertNotNull(result);
    }

    @Test
    void testGetIncidentById_notFound() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> incidentService.getIncidentById(1L));
    }

    @Test
    void testUpdateStatus_inProgress() {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.OPEN);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident updated = incidentService.updateStatus(1L, IncidentStatus.IN_PROGRESS);
        assertEquals(IncidentStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void testUpdateStatus_resolved() {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.OPEN);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident updated = incidentService.updateStatus(1L, IncidentStatus.RESOLVED);
        assertEquals(IncidentStatus.RESOLVED, updated.getStatus());
        assertNotNull(updated.getResolvedTime());
    }

    @Test
    void testUpdateStatus_closed_withoutResolvedTime() {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        assertThrows(IllegalStateException.class, () -> incidentService.updateStatus(1L, IncidentStatus.CLOSED));
    }

    @Test
    void testUpdateStatus_closed_withResolvedTime() {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedTime(LocalDateTime.now());
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Incident updated = incidentService.updateStatus(1L, IncidentStatus.CLOSED);
        assertEquals(IncidentStatus.CLOSED, updated.getStatus());
    }
}
