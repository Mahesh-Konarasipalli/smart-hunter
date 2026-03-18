package com.ai_assistant.job_assistant.service;

import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;

@Service
public class JobScraperService {

    public String fetchJobDescription(String url) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080));

            Page page = context.newPage();
            
            System.out.println("   ⏳ Loading page and waiting for redirects...");
            page.navigate(url);

            // 1. ⚡ THE FIX: Wait until the network is completely quiet (this bypasses the 411-character redirect pages!)
            try {
                page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(10000));
            } catch (Exception e) {
                // If it times out waiting for network idle, we just continue and try to scrape anyway
            }
            
            // 2. Extra human-like wait for dynamic React/Angular text to render
            page.waitForTimeout(3000);

            String content = "";
            
            // 3. ⚡ TARGETED SCRAPING: Grab ONLY the job description box, ignoring headers/footers
            try {
                if (url.contains("indeed.com")) {
                    content = page.locator("#jobDescriptionText").innerText();
                } else if (url.contains("linkedin.com")) {
                    content = page.locator(".show-more-less-html__markup, .description__text").first().innerText();
                } else if (url.contains("naukri.com")) {
                    content = page.locator(".job-desc").innerText();
                } else {
                    content = page.locator("body").innerText(); // Fallback
                }
            } catch (Exception e) {
                // If the specific box isn't found, grab everything
                content = page.locator("body").innerText();
            }

            browser.close();
            
            System.out.println("   -> 📄 Successfully ripped " + content.length() + " chars of actual job text.");
            return content;
            
        } catch (Exception e) {
            System.err.println("❌ Scraper completely failed on this link: " + e.getMessage());
            return "";
        }
    }
}