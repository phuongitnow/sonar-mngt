package com.sonarqube.adminapp.domain.repository;

import com.sonarqube.adminapp.domain.entity.CleanupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleanupHistoryRepository extends JpaRepository<CleanupHistory, Long> {
    
    List<CleanupHistory> findTop10ByOrderByExecutedAtDesc();
}

