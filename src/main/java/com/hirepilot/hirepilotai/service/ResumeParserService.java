package com.hirepilot.hirepilotai.service;

import com.hirepilot.hirepilotai.dto.parser.ResumeParsedData;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class ResumeParserService {

    private static final Set<String> IGNORED_HEADINGS = Set.of("RESUME", "CURRICULUM VITAE", "CV");

    public ResumeParsedData parseResume(MultipartFile file) throws IOException {
        ResumeParsedData parsedData = new ResumeParsedData();
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);
            parsedData.setExtractedText(extractedText);
            parsedData.setEmail(extractEmail(extractedText));
            parsedData.setMobileNumbers(extractMobileNumbers(extractedText));
            parsedData.setName(extractName(extractedText));
        }
        return parsedData;
    }

    private String extractEmail(String text) {
        Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private List<String> extractMobileNumbers(String text) {
        Set<String> mobiles = new LinkedHashSet<>();
        Pattern pattern = Pattern.compile("\\+?[0-9][0-9()\\-\\s]{7,20}");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String mobile = matcher.group().trim();
            if (isValidPhoneCandidate(mobile)) {
                mobiles.add(mobile);
            }
        }
        return new ArrayList<>(mobiles);
    }

    private boolean isValidPhoneCandidate(String mobile) {
        String digits = mobile.replaceAll("\\D", "");
        return digits.length() >= 10;
    }

    private String extractName(String text) {
        String[] lines = text.split("\\R");
        for (String line : lines) {
            String candidate = line.trim();
            if (candidate.isEmpty()) {
                continue;
            }
            if (isSeparator(candidate)) {
                continue;
            }
            if (IGNORED_HEADINGS.contains(candidate.toUpperCase())) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    private boolean isSeparator(String line) {
        return line.matches("^[\\-=*_~.#|+]{3,}$");
    }

}
