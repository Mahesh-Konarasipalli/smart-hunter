package com.ai_assistant.job_assistant.controller;

import com.ai_assistant.job_assistant.entity.AppUser;
import com.ai_assistant.job_assistant.entity.JobAnalysis;
import com.ai_assistant.job_assistant.repository.JobAnalysisRepository;
import com.ai_assistant.job_assistant.repository.UserRepository;
import com.ai_assistant.job_assistant.service.JobAnalyst;
import com.ai_assistant.job_assistant.service.JobAnalysisResult;
import com.ai_assistant.job_assistant.service.JobSearchService;
import com.ai_assistant.job_assistant.service.ResumeAnalyzer;
import com.ai_assistant.job_assistant.service.ResumeSkills;
import com.ai_assistant.job_assistant.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import com.ai_assistant.job_assistant.entity.SavedJob;
import com.ai_assistant.job_assistant.repository.SavedJobRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class JobAssistantController {

    private final JobScraperService scraperService;
    private final JobAnalyst jobAnalyst;
    private final JobAnalysisRepository repository;
    private final JobSearchService jobSearchService;
    private final UserRepository userRepository;
    private final SavedJobRepository savedJobRepository;

    private final ResumeAnalyzer resumeAnalyzer;

    @PostMapping("/api/hunt-from-resume")
    public List<JobAnalysisResult> huntFromResume(@RequestParam("file") MultipartFile file, @RequestParam("email") String userEmail) {
        System.out.println("=====================================");
        System.out.println("📄 RESUME UPLOAD DETECTED. Reading PDF...");

        String resumeText = "";
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            resumeText = stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF file.");
        }

        if (resumeText.length() > 3000) {
            resumeText = resumeText.substring(0, 3000);
        }

        System.out.println("🧠 Asking Groq AI to analyze candidate profile...");
        
        ResumeSkills skills = resumeAnalyzer.extractSkills(resumeText);
        String searchKeywords = skills.searchKeywords();
        String experienceLevel = skills.experienceLevel(); 
        
        // ⚡ THE FIX: The Java Scissors!
        // If Llama 3 hallucinates and gives us a massive list separated by commas, chop it down.
        String[] skillArray = searchKeywords.split(",");
        if (skillArray.length > 3) {
            searchKeywords = skillArray[0].trim() + " " + skillArray[1].trim() + " " + skillArray[2].trim();
        } else {
            searchKeywords = searchKeywords.replace(",", ""); // Just clean up commas
        }

        // ⚡ NEW: Save the candidate's personal skills to their database profile!
        AppUser user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            user.setSavedSkills(searchKeywords);
            user.setSavedExperience(experienceLevel);
            userRepository.save(user);
            System.out.println("💾 Saved specific skills to profile for automated morning hunts.");
        }

        // If it hallucinated 'junior', force it back to 'fresher' for the Indian job market
        if (experienceLevel.toLowerCase().contains("junior") || experienceLevel.toLowerCase().contains("entry")) {
            experienceLevel = "fresher";
        }
        
        System.out.println("🎯 Sanitized Skills: " + searchKeywords);
        System.out.println("🎯 Sanitized Experience: " + experienceLevel);
        
        return autoHunt(searchKeywords, experienceLevel); 
    }

   @GetMapping("/api/auto-hunt")
    public List<JobAnalysisResult> autoHunt(@RequestParam String skill, @RequestParam(defaultValue = "fresher OR entry level") String experience) {
        System.out.println("=====================================");
        System.out.println("🚀 AUTO-HUNT TRIGGERED FOR: " + skill);
        
        // Pass BOTH variables to the search service
        List<String> urls = jobSearchService.findJobLinks(skill, experience);
        List<JobAnalysisResult> filteredJobs = new ArrayList<>();

        if (urls.isEmpty()) {
            System.out.println("⚠️ No URLs found to analyze.");
            return filteredJobs;
        }

        int jobCounter = 1;
        for (String url : urls) {
            try {
                System.out.println("⏳ [" + jobCounter + "/" + urls.size() + "] Scraping: " + url);
                
                String content = scraperService.fetchJobDescription(url);

                // ⚡ THE FIX: Prevent Llama 3 from choking on huge websites!
                // If the text is longer than 4000 characters, chop it down.
                if (content.length() > 2000) {
                    content = content.substring(0, 2000);
                }

                System.out.println("   -> 🧠 Read " + content.length() + " chars. Handing to Groq AI...");
                
                JobAnalysisResult analysis = jobAnalyst.analyzeJob(content);

                JobAnalysisResult analysisWithUrl = new JobAnalysisResult(
                    analysis.matchPercentage(),
                    analysis.briefSummary(),
                    analysis.missingSkills(),
                    analysis.recommendation(),
                    url 
                );

                System.out.println("   -> 🎯 Match Score: " + analysisWithUrl.matchPercentage() + "%");

                if (analysisWithUrl.matchPercentage() >= 10) {
                    filteredJobs.add(analysisWithUrl);
                    
                    // Optional Database save
                    JobAnalysis dbRecord = new JobAnalysis();
                    dbRecord.setJobUrl(url);
                    dbRecord.setMatchPercentage(analysisWithUrl.matchPercentage());
                    dbRecord.setBriefSummary(analysisWithUrl.briefSummary());
                    dbRecord.setMissingSkills(analysisWithUrl.missingSkills());
                    dbRecord.setRecommendation(analysisWithUrl.recommendation());
                    repository.save(dbRecord);
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to process " + url + " | Error: " + e.getMessage());
            }
            jobCounter++;
        }
        
        System.out.println("✅ Hunt Complete. Returning " + filteredJobs.size() + " jobs to UI.");
        System.out.println("=====================================");
        return filteredJobs;
    }
    // ⚡ THE NEW COVER LETTER API
    @PostMapping("/api/generate-cover-letter")
    public Map<String, String> generateCoverLetter(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String jobSummary = payload.get("jobSummary");
        
        // 1. Grab the user's saved data from the database
        AppUser user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        String name = user.getName();
        String skills = user.getSavedSkills();
        
        System.out.println("📝 Generating Cover Letter for " + name + "...");
        
        // 2. Ask Llama 3 to write the letter!
        String coverLetter = jobAnalyst.generateCoverLetter(name, skills, jobSummary);
        
        // 3. Send it back to the frontend
        return java.util.Map.of("coverLetter", coverLetter);
    }
    // ⚡ THE NEW KANBAN UPDATE API
    @PostMapping("/api/saved-jobs/update-status")
    public String updateJobStatus(@RequestBody java.util.Map<String, String> payload) {
        try {
            Long jobId = Long.parseLong(payload.get("jobId"));
            String newStatus = payload.get("status");

            // Assuming you have a savedJobRepository injected in your controller!
            // If you use a service instead, call your service to update it.
            java.util.Optional<SavedJob> jobOptional = savedJobRepository.findById(jobId);
            
            if (jobOptional.isPresent()) {
                SavedJob job = jobOptional.get();
                job.setStatus(newStatus);
                savedJobRepository.save(job);
                return "Status updated to " + newStatus;
            }
            return "Error: Job not found.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}