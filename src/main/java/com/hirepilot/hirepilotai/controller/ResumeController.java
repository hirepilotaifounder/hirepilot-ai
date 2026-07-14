package com.hirepilot.hirepilotai.controller;

import com.hirepilot.hirepilotai.dto.response.ResumeDownloadResponse;
import com.hirepilot.hirepilotai.dto.response.ResumeResponse;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<String> uploadResume(@RequestParam("resumeTitle") String resumeTitle, @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        resumeService.uploadResume(resumeTitle, file, user);
        return ResponseEntity.ok("Resume uploaded successfully.");
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getMyResumes(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(resumeService.getMyResumes(user));
    }

    @PatchMapping("/{resumeId}/activate")
    public ResponseEntity<String> activateResume(@PathVariable Long resumeId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        resumeService.activateResume(resumeId, user);
        return ResponseEntity.ok("Resume activated successfully.");
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<String> deleteResume(@PathVariable Long resumeId, Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        resumeService.deleteResume(resumeId, user);
        return ResponseEntity.ok("Resume deleted successfully.");
    }

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long resumeId, Authentication authentication) throws IOException {
        User user = (User) authentication.getPrincipal();
        ResumeDownloadResponse response = resumeService.downloadResume(resumeId, user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(response.getResource().contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getOriginalFileName() + "\"")
                .body(response.getResource());
    }
}