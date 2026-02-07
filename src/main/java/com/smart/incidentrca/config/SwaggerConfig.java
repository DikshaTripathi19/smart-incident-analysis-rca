package com.smart.incidentrca.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Smart Incident RCA API",
                version = "1.0",
                description = "Incident Management System with Smart Root Cause Analysis"
        )
)
public class SwaggerConfig {
}
