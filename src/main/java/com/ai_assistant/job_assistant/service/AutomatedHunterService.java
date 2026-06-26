package com.ai_assistant.job_assistant.service;

import com.ai_assistant.job_assistant.entity.AppUser;
import com.ai_assistant.job_assistant.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutomatedHunterService {

    private final JobSearchService searchService;
    private final JobScraperService scraperService;
    private final JobAnalyst analyst;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository; 

    @Value("${spring.mail.username}") 
    private String senderEmail;

    // ⚡ 1. Set to trigger exactly at 9:00 AM daily (India Standard Time)
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata") 
    public void runMorningJobHunt() {
        System.out.println("⏰ 9:00 AM IST - Initiating Personalized Morning Job Hunts...");

        List<AppUser> verifiedUsers = userRepository.findAll().stream()
            .filter(u -> u.isVerified())
            .collect(Collectors.toList());

        if (verifiedUsers.isEmpty()) {
            System.out.println("⚠️ No verified users found in the database. Skipping.");
            return; 
        }

        // ⚡ 2. THE FLIP: Loop through every user and run a CUSTOM hunt for each one!
        for (AppUser user : verifiedUsers) {
            String userSkills = user.getSavedSkills();
            String userExp = user.getSavedExperience();

            // Skip users who haven't uploaded a resume to save their skills yet
            if (userSkills == null || userSkills.trim().isEmpty()) {
                System.out.println("⏭️ Skipping " + user.getEmail() + " (No resume data saved yet).");
                continue; 
            }

            System.out.println("🕵️ Hunting for " + user.getName() + " | Target: " + userSkills);
            
            // Search using THIS specific user's skills
            List<String> urls = searchService.findJobLinks(userSkills, userExp);
            
            if (urls.isEmpty()) {
                System.out.println("⚠️ No jobs found for " + user.getName() + " today.");
                continue;
            }

            // ⚡ 3. Personalize the Email Greeting
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>")
                       .append("<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 12px; border: 1px solid #e2e8f0;'>")
                       .append("<h2 style='color: #2563eb; border-bottom: 2px solid #e2e8f0; padding-bottom: 15px; margin-top: 0;'>🚀 Daily Match Report</h2>")
                       .append("<p style='color: #0f172a; font-size: 16px;'>Hello <strong>").append(user.getName()).append("</strong>,</p>")
                       .append("<p style='color: #64748b; line-height: 1.6;'>Llama 3 has finished scanning the web for roles matching your specific skill profile (<em>").append(userSkills).append("</em>).</p>");

            int matchCount = 0;

            // Analyze the scraped jobs
            for (String url : urls) {
                try {
                    System.out.println("   -> Analyzing: " + url);
                    String content = scraperService.fetchJobDescription(url);

                    if (content.length() > 2000) content = content.substring(0, 2000);

                    JobAnalysisResult result = analyst.analyzeJob(content);

                    // Only add jobs that have a decent match percentage
                    if (result.matchPercentage() >= 10) { 
                        matchCount++;
                        htmlContent.append("<div style='margin-top: 25px; padding: 20px; border-left: 4px solid #10b981; background: #f8fafc; border-radius: 0 8px 8px 0;'>")
                                   .append("<h3 style='margin: 0 0 10px 0; color: #0f172a;'>Match Score: <span style='color: #10b981;'>").append(result.matchPercentage()).append("%</span></h3>")
                                   .append("<p style='font-size: 14px; color: #475569; margin: 10px 0; line-height: 1.5;'>").append(result.briefSummary()).append("</p>")
                                   .append("<p style='font-size: 14px; color: #2563eb; background: #eff6ff; padding: 10px; border-radius: 6px; font-style: italic;'><strong>AI Insight:</strong> ").append(result.recommendation()).append("</p>")
                                   .append("<a href='").append(url).append("' style='display: inline-block; margin-top: 10px; padding: 10px 20px; color: white; background-color: #2563eb; text-decoration: none; border-radius: 6px; font-weight: 600;'>View Application</a>")
                                   .append("</div>");
                    }
                } catch (Exception e) { 
                    System.err.println("❌ Background analysis failed: " + e.getMessage());
                }
            }

            htmlContent.append("<p style='font-size: 12px; color: #94a3b8; text-align: center; margin-top: 40px; border-top: 1px solid #e2e8f0; padding-top: 20px;'>Automated by SmartHunter AI</p>")
                       .append("</div></body></html>");

            // ⚡ 4. Send the personalized email ONLY to this specific user
            if (matchCount > 0) {
                sendHtmlEmail(user.getEmail(), "Your AI Job Picks for Today", htmlContent.toString());
            } else {
                System.out.println("⚠️ No high-match jobs found for " + user.getName() + " today.");
            }
        }
    }

    // ⚡ PUBLIC METHOD: Used by your Controller for instant resume uploads
    public void sendInstantJobAlert(String toEmail, List<JobAnalysisResult> matchedJobs) {
        try {
            // Grab the user to greet them by name!
            AppUser user = userRepository.findByEmail(toEmail).orElse(null);
            String name = (user != null && user.getName() != null) ? user.getName() : "Agent";

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<html><body style='font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;'>")
                       .append("<div style='max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 12px; border: 1px solid #e2e8f0;'>")
                       .append("<h2 style='color: #2563eb; border-bottom: 2px solid #e2e8f0; padding-bottom: 15px; margin-top: 0;'>SmartHunter Analysis Complete</h2>")
                       .append("<p style='color: #0f172a; font-size: 16px;'>Hello <strong>").append(name).append("</strong>,</p>")
                       .append("<p style='color: #64748b;'>We successfully processed your resume and found these immediate opportunities:</p>");

            for (JobAnalysisResult job : matchedJobs) {
                htmlContent.append("<div style='margin-top: 25px; padding: 20px; border-left: 4px solid #10b981; background: #f8fafc; border-radius: 0 8px 8px 0;'>")
                           .append("<h3 style='margin: 0 0 10px 0; color: #0f172a;'>Match Score: <span style='color: #10b981;'>").append(job.matchPercentage()).append("%</span></h3>")
                           .append("<p style='font-size: 14px; color: #475569; margin: 10px 0; line-height: 1.5;'>").append(job.briefSummary()).append("</p>")
                           .append("<p style='font-size: 14px; color: #2563eb; background: #eff6ff; padding: 10px; border-radius: 6px; font-style: italic;'><strong>AI Insight:</strong> ").append(job.recommendation()).append("</p>")
                           .append("<a href='").append(job.jobUrl()).append("' style='display: inline-block; margin-top: 10px; padding: 10px 20px; background-color: #2563eb; color: white; text-decoration: none; border-radius: 6px; font-weight: 600;'>View Application</a>")
                           .append("</div>");
            }

            htmlContent.append("<p style='font-size: 12px; color: #94a3b8; text-align: center; margin-top: 40px; border-top: 1px solid #e2e8f0; padding-top: 20px;'>Automated by SmartHunter AI</p>")
                       .append("</div></body></html>");

            sendHtmlEmail(toEmail, "🚀 SmartHunter: " + matchedJobs.size() + " New High-Match Jobs Found!", htmlContent.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send instant alert: " + e.getMessage());
        }
    }

    // Your private mail sender method
    private void sendHtmlEmail(String targetEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderEmail);
            helper.setTo(targetEmail);  
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            System.out.println("✉️ Professional HTML Job Report sent to: " + targetEmail);
        } catch (Exception e) {
            System.err.println("❌ Email failed to send to " + targetEmail + ": " + e.getMessage());
        }
    }
}