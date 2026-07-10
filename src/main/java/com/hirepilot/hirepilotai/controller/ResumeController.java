package com.hirepilot.hirepilotai.controller;

import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<String> uploadResume(@RequestParam("resumeTitle") String resumeTitle,
                                               @RequestParam("file") MultipartFile file,
                                               Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        resumeService.uploadResume(resumeTitle, file, user);
        return ResponseEntity.ok("Resume uploaded successfully.");
    }
}