package com.smart.incidentrca.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, unique = true)
    private String incidentId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String severity; // LOW, MEDIUM, HIGH

    @Column(name = "root_cause")
    private String rootCause;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "last_updated_time", nullable = false)
    private LocalDateTime lastUpdatedTime;

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.lastUpdatedTime = now;

        if (this.status == null) {
            this.status = IncidentStatus.OPEN;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdatedTime = LocalDateTime.now();
    }
}
