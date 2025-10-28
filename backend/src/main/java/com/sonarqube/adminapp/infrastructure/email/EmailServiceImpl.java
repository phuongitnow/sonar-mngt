package com.sonarqube.adminapp.infrastructure.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public void sendCleanupNotification(int projectsDeleted, long linesOfCodeDeleted, 
                                       long linesOfCodeRemaining, String recipientEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("SonarQube Cleanup Notification");
            
            String emailBody = String.format(
                "SonarQube Cleanup Process Completed\n\n" +
                "Projects Deleted: %d\n" +
                "Lines of Code Deleted: %,d\n" +
                "Remaining Lines of Code: %,d\n\n" +
                "This is an automated notification from the SonarQube Admin Application.",
                projectsDeleted, linesOfCodeDeleted, linesOfCodeRemaining
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Cleanup notification email sent to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send notification email", e);
        }
    }
}

