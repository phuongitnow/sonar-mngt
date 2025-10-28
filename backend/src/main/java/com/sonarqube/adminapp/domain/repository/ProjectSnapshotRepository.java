package com.sonarqube.adminapp.domain.repository;

import com.sonarqube.adminapp.domain.entity.ProjectSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectSnapshotRepository extends JpaRepository<ProjectSnapshot, Long> {
    
    Optional<ProjectSnapshot> findByProjectKey(String projectKey);
    
    @Query("SELECT p FROM ProjectSnapshot p WHERE p.lastScanDate < :thresholdDate")
    List<ProjectSnapshot> findProjectsOlderThan(LocalDateTime thresholdDate);
    
    @Query("SELECT SUM(p.linesOfCode) FROM ProjectSnapshot p")
    Long getTotalLinesOfCode();
    
    List<ProjectSnapshot> findAllByOrderByCreatedAtDesc();
}

