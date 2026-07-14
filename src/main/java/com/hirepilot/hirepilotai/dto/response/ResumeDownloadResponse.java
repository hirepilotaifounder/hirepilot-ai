package com.hirepilot.hirepilotai.dto.response;

import org.springframework.core.io.Resource;

public class ResumeDownloadResponse {

    private Resource resource;
    private String originalFileName;

    public ResumeDownloadResponse() {
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}