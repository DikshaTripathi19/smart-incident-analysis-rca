package com.smart.incidentrca.llm.test;

import com.smart.incidentrca.llm.LlmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LlmServiceTest {

    private LlmService llmService;

    @BeforeEach
    void setUp() {
        llmService = spy(new LlmService());
        ReflectionTestUtils.setField(llmService, "apiKey", "test-key");
        ReflectionTestUtils.setField(llmService, "rcaPrompt",
                "Incident Description: %s\nSeverity: %s\nEnvironment: %s\nSymptoms: %s");
    }

    @Test
    void testIsEnabled() {
        assertTrue(llmService.isEnabled());
        ReflectionTestUtils.setField(llmService, "apiKey", "");
        assertFalse(llmService.isEnabled());
    }

    @Test
    void testGenerateRca() throws Exception {
        doReturn("Root cause is CPU spike")
                .when(llmService).generateRca(
                        anyString(), anyString(), anyString(), anyList()
                );

        String result = llmService.generateRca(
                "CPU spike on server",
                "HIGH",
                "PROD",
                List.of("High CPU", "Timeout")
        );

        assertNotNull(result);
        assertTrue(result.contains("CPU spike"));
    }
}
