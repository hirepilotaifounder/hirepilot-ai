package com.hirepilot.hirepilotai.service;

import com.hirepilot.hirepilotai.dto.parser.ResumeParsedData;
import com.hirepilot.hirepilotai.dto.response.ResumeDownloadResponse;
import com.hirepilot.hirepilotai.entity.Resume;
import com.hirepilot.hirepilotai.entity.User;
import com.hirepilot.hirepilotai.exception.ActiveResumeDeletionException;
import com.hirepilot.hirepilotai.exception.ResumeNotFoundException;
import com.hirepilot.hirepilotai.repository.ResumeRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
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
    private ResumeParserService resumeParserService;

    public ResumeService(ResumeRepository resumeRepository, ResumeParserService resumeParserService) {
        this.resumeRepository = resumeRepository;
        this.resumeParserService = resumeParserService;
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
        ResumeParsedData parsedData = resumeParserService.parseResume(file);
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
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                                        .orElseThrow(() -> new ResumeNotFoundException("Resume not found."));

        resume.setActive(true);
        resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId, User user) throws IOException {
        Resume resume = resumeRepository
                .findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found.")); // Step 1 - Find resume

        if (resume.isActive()) {
            throw new ActiveResumeDeletionException("Please activate another resume before deleting the active resume."); // Step 2 - Check if active
        }

        Files.deleteIfExists(Paths.get(resume.getFilePath())); // Step 3 - Delete PDF

        resumeRepository.delete(resume); // Step 4 - Delete DB record

        logger.info("Resume '{}' deleted successfully for user '{}'", resume.getResumeTitle(), user.getEmail()); // Step 5 - Log success
    }

    public ResumeDownloadResponse downloadResume(Long resumeId, User user) throws MalformedURLException {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                                        .orElseThrow(() -> new ResumeNotFoundException("Resume not found."));

        Path path = Paths.get(resume.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResumeNotFoundException("Resume file not found.");
        }
        ResumeDownloadResponse response = new ResumeDownloadResponse();
        response.setResource(resource);
        response.setOriginalFileName(resume.getOriginalFileName());
        return response;
    }

}