package com.hirepilot.hirepilotai.service;

import com.hirepilot.hirepilotai.entity.Resume;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.exception.ResumeNotFoundException;
import com.hirepilot.hirepilotai.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.hirepilot.hirepilotai.dto.response.ResumeResponse;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public void uploadResume(String resumeTitle, MultipartFile file, User user) throws IOException {
        validateFile(file);
        String storedFileName = generateUniqueFileName();
        Path uploadPath = createUploadDirectory();
        Path destination = uploadPath.resolve(storedFileName);
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            buildResume(resumeTitle, file, storedFileName, destination, user);
            logger.info("Resume uploaded successfully for user: {}", user.getEmail());
        } catch (Exception e) {
            Files.deleteIfExists(destination);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Please select a resume to upload.");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new RuntimeException("Only PDF files are allowed.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Resume size exceeds the 5 MB limit.");
        }
    }

    private String generateUniqueFileName() {
        return UUID.randomUUID() + ".pdf";
    }

    private Path createUploadDirectory() throws IOException {
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private void buildResume(String resumeTitle, MultipartFile file, String storedFileName, Path destination, User user) {
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setResumeTitle(resumeTitle);
        resume.setOriginalFileName(file.getOriginalFilename());
        resume.setStoredFileName(storedFileName);
        resume.setFilePath(destination.toString());
        resume.setFileSize(file.getSize());
        resume.setContentType(file.getContentType());
        resume.setUploadedAt(LocalDateTime.now());
        resume.setActive(false);
        resumeRepository.save(resume);
    }

    public List<ResumeResponse> getMyResumes(User user) {
        List<Resume> resumes = resumeRepository.findByUserOrderByActiveDescUploadedAtDesc(user);
        return resumes.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ResumeResponse mapToResponse(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setResumeTitle(resume.getResumeTitle());
        response.setActive(resume.isActive());
        response.setUploadedAt(resume.getUploadedAt());
        return response;
    }

    @Transactional
    public void activateResume(Long resumeId, User user) {
        resumeRepository.deactivateAllByUser(user);
        Resume resume = resumeRepository
                .findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found."));

        resume.setActive(true);
        resumeRepository.save(resume);
    }

}