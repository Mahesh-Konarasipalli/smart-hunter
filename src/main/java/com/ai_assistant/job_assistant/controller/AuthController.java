package com.ai_assistant.job_assistant.controller;

import com.ai_assistant.job_assistant.entity.AppUser;
import com.ai_assistant.job_assistant.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    // 1. Update Registration to accept "name"
    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        String name = payload.getOrDefault("name", "Agent"); // Capture the name!

        if (userRepository.findByEmail(email).isPresent()) {
            return "Error: Email already exists!";
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPassword(password); // Note: In production, use BCrypt!
        user.setName(name);
        
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtpCode(otp);
        userRepository.save(user);

        System.out.println("🚨 HACKER BYPASS - OTP for " + user.getEmail() + " is: " + otpCode);
        sendOtpEmail(email, otp);
        return "Registration successful. Check email for OTP.";
    }

    // NEW: Verify Initial Registration
   @PostMapping("/verify-registration")
    public Map<String, String> verifyRegistration(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        
        System.out.println("--- VERIFICATION ATTEMPT ---");
        System.out.println("Email received: '" + email + "'");
        System.out.println("OTP received: '" + otp + "'");

        AppUser user = userRepository.findByEmail(email).orElse(null);
        Map<String, String> response = new java.util.HashMap<>();

        if (user == null) {
            System.out.println("❌ ERROR: User not found in database.");
            response.put("error", "User not found.");
            return response;
        }

        System.out.println("User found! Database OTP is: '" + user.getOtpCode() + "'");

        if (otp != null && otp.equals(user.getOtpCode())) {
            // Execute the update query and get the number of rows changed
            int rowsUpdated = userRepository.verifyUserEmail(email);
            System.out.println("✅ MATCH! Rows updated in MySQL: " + rowsUpdated);
            
            response.put("success", "Email verified successfully! You can now log in.");
            return response;
        }
        
        System.out.println("❌ ERROR: OTP did not match.");
        response.put("error", "Error: Invalid OTP.");
        return response;
    }

    // 2. Update Login to return Name + Email
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        
        AppUser user = userRepository.findByEmail(email).orElse(null);
        Map<String, String> response = new java.util.HashMap<>();
        
        if (user != null && user.getPassword().equals(password)) {
            if (!user.isVerified()) {
                response.put("error", "Error: Please verify your email first.");
                return response;
            }
            response.put("success", "true");
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return response;
        }
        response.put("error", "Error: Invalid credentials.");
        return response;
    }

    // 3. NEW: Request Profile Update OTP
    @PostMapping("/request-update-otp")
    public String requestUpdateOtp(@RequestBody Map<String, String> payload) {
        String currentEmail = payload.get("currentEmail");
        AppUser user = userRepository.findByEmail(currentEmail).orElseThrow();
        
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtpCode(otp);
        userRepository.save(user);
        
        // Use your existing email service to send the OTP
        sendOtpEmail(currentEmail, otp); 
        
        return "OTP sent to current email for authorization.";
    }

    // 4. NEW: Verify and Apply Profile Update
    @PostMapping("/verify-update")
    public Map<String, String> verifyUpdate(@RequestBody Map<String, String> payload) {
        String currentEmail = payload.get("currentEmail");
        String otp = payload.get("otp");
        String newName = payload.get("newName");
        String newEmail = payload.get("newEmail");

        AppUser user = userRepository.findByEmail(currentEmail).orElse(null);
        Map<String, String> response = new java.util.HashMap<>();

        if (user != null && user.getOtpCode().equals(otp)) {
            user.setName(newName);
            user.setEmail(newEmail);
            user.setOtpCode(null); // Clear OTP after use
            userRepository.save(user);
            
            response.put("success", "Profile updated successfully!");
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            return response;
        }
        response.put("error", "Error: Invalid OTP.");
        return response;
    }

    private void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Job Assistant Verification Code");
        message.setText("Your OTP code is: " + otp + "\nThis code is required to verify your account or update your profile.");
        mailSender.send(message);
        System.out.println("✉️ OTP sent to: " + email);
    }
}