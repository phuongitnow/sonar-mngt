package com.sonarqube.adminapp.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cleanup_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanupHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private LocalDateTime executedAt;
    
    @Column
    private Integer projectsDeleted;
    
    @Column
    private Long linesOfCodeDeleted;
    
    @Column
    private Long linesOfCodeRemaining;
    
    @Column
    private String status;
    
    @Column(length = 5000)
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}

