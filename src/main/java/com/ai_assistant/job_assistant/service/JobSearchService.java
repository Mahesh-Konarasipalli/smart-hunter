package com.ai_assistant.job_assistant.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobSearchService {

    public List<String> findJobLinks(String keyword, String experience) {
        System.out.println("🕵️ Agent going DIRECTLY to LinkedIn, Naukri, Indeed, and WorkIndia for: " + keyword + " | Exp: " + experience);
        
        List<String> allRawLinks = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            // Added viewport size to make the headless browser look more like a real laptop
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080));
            
            Page page = context.newPage();

            // 1. Format queries for different site structures
            String safeQuery = URLEncoder.encode(keyword + " " + experience, StandardCharsets.UTF_8.name());
            String dashQuery = (keyword + " " + experience).replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase(); // formats to "java-spring-boot-fresher"

            // 2. The Master List of Indian Job Boards
            String[] searchUrls = {
                "https://www.linkedin.com/jobs/search/?keywords=" + safeQuery + "&location=India",
                "https://in.indeed.com/jobs?q=" + safeQuery + "&l=India",
                "https://www.naukri.com/" + dashQuery + "-jobs",
                "https://www.workindia.in/search/?query=" + safeQuery
            };

            // 3. Loop through each platform
            for (String targetUrl : searchUrls) {
                try {
                    System.out.println("   🌐 Scanning: " + targetUrl);
                    page.navigate(targetUrl, new Page.NavigateOptions().setTimeout(10000));
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
                    page.waitForTimeout(2000); // Give React/Angular apps time to inject the job cards

                    Locator linksLocator = page.locator("a");
                    for (int i = 0; i < linksLocator.count(); i++) {
                        String href = linksLocator.nth(i).getAttribute("href");
                        if (href != null) {
                            // Convert relative paths (like /viewjob?jk=123) to full URLs
                            if (href.startsWith("/")) {
                                if (targetUrl.contains("indeed.com")) href = "https://in.indeed.com" + href;
                                else if (targetUrl.contains("naukri.com")) href = "https://www.naukri.com" + href;
                                else if (targetUrl.contains("workindia.in")) href = "https://www.workindia.in" + href;
                                else if (targetUrl.contains("linkedin.com")) href = "https://www.linkedin.com" + href;
                            }
                            allRawLinks.add(href);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("   ⚠️ Timed out or blocked by security on this site. Moving to next...");
                }
            }

            // 4. Filter the massive list of links down to JUST the actual job postings
            List<String> cleanLinks = allRawLinks.stream()
                    .filter(href -> 
                        href.contains("linkedin.com/jobs/view/") || 
                        href.contains("naukri.com/job-listings") || 
                        href.contains("indeed.com/viewjob") || 
                        href.contains("indeed.com/rc/clk") || 
                        href.contains("workindia.in/jobs/")
                    )
                    // Remove basic tracking parameters to avoid duplicates
                    .map(href -> href.split("&trk=")[0]) 
                    .distinct()
                    .limit(4) // Let's pull the top 4 best matches across all platforms
                    .collect(Collectors.toList());

            if (cleanLinks.isEmpty()) {
                System.out.println("⚠️ No jobs found across the platforms. Bot protections might be temporarily active.");
            } else {
                System.out.println("✅ Found " + cleanLinks.size() + " direct job links from multiple platforms!");
                for (String link : cleanLinks) {
                    System.out.println("   🔗 " + link);
                }
            }
            
            browser.close();
            return cleanLinks;
        } catch (Exception e) {
            System.err.println("❌ Master Scraper failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}