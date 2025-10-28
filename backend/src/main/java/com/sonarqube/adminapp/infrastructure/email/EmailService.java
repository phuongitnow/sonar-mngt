package com.sonarqube.adminapp.infrastructure.email;

public interface EmailService {
    void sendCleanupNotification(int projectsDeleted, long linesOfCodeDeleted, 
                                long linesOfCodeRemaining, String recipientEmail);
}

