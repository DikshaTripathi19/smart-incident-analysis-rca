package com.smart.incidentrca.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.incidentrca.controller.IncidentController;
import com.smart.incidentrca.dto.IncidentRequest;
import com.smart.incidentrca.model.Incident;
import com.smart.incidentrca.model.IncidentStatus;
import com.smart.incidentrca.service.IncidentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncidentService incidentService;

    private Incident incident;

    @BeforeEach
    void setup() {
        incident = new Incident();
        incident.setId(1L);
        incident.setIncidentId("INC-102");
        incident.setDescription("CPU spike and DB timeout");
        incident.setSeverity("HIGH");
        incident.setRootCause("Infrastructure issue");
        incident.setStatus(IncidentStatus.OPEN);
        incident.setCreatedTime(LocalDateTime.now());
        incident.setResolvedTime(null);
    }

    @Test
    void testCreateIncident() throws Exception {
        IncidentRequest request = new IncidentRequest(
                "INC-102",
                "CPU spike and DB timeout",
                "HIGH",
                "PRODUCTION",
                List.of("High CPU usage", "DB connection timeout")
        );

        Mockito.when(incidentService.createIncident(any(Incident.class), anyString(), anyList()))
                .thenReturn(incident);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("INC-102"))
                .andExpect(jsonPath("$.description").value("CPU spike and DB timeout"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testGetAllIncidents() throws Exception {
        Mockito.when(incidentService.getAllIncidents())
                .thenReturn(List.of(incident));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].incidentId").value("INC-102"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    @Test
    void testGetIncidentById() throws Exception {
        Mockito.when(incidentService.getIncidentById(1L))
                .thenReturn(incident);

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").value("INC-102"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        Incident updatedIncident = new Incident();
        updatedIncident.setId(1L);
        updatedIncident.setIncidentId("INC-102");
        updatedIncident.setStatus(IncidentStatus.RESOLVED);

        Mockito.when(incidentService.updateStatus(eq(1L), eq(IncidentStatus.RESOLVED)))
                .thenReturn(updatedIncident);

        mockMvc.perform(patch("/api/incidents/1/status")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
    }
}
