package com.ai_assistant.job_assistant.controller;

import com.ai_assistant.job_assistant.entity.SavedJob;
import com.ai_assistant.job_assistant.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobRepository savedJobRepository;

    @PostMapping("/save")
    public String saveJob(@RequestBody SavedJob job) {
        // Prevent duplicate saves
        if (savedJobRepository.existsByUserEmailAndJobUrl(job.getUserEmail(), job.getJobUrl())) {
            return "⚠️ You already saved this job!";
        }
        
        savedJobRepository.save(job);
        return "✅ Job successfully saved to your vault!";
    }

    // We will use this later to build your "My Saved Jobs" page!
    @GetMapping("/{email}")
    public List<SavedJob> getSavedJobs(@PathVariable String email) {
        return savedJobRepository.findByUserEmail(email);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteJob(@PathVariable Long id) {
        if (savedJobRepository.existsById(id)) {
            savedJobRepository.deleteById(id);
            return "Job removed from vault.";
        }
        return "Error: Job not found.";
    }
}