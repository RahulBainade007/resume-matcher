package com.rahul.resumematcher.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ResumeSectionParser {

    private static final Map<ResumeSectionType, List<String>> SECTION_HEADERS = createSectionHeaders();
    private static final Pattern BULLET_PREFIX = Pattern.compile("^(?:[•●▪◦*-]|\\d+[.)])\\s*");

    public ParsedResume parse(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return new ParsedResume(List.of());
        }

        List<ResumeSection> sections = new ArrayList<>();
        List<String> contentLines = new ArrayList<>();
        ResumeSectionType currentType = null;
        String currentTitle = null;

        for (String line : resumeText.split("\\R", -1)) {
            ResumeSectionType headerType = detectHeader(line);
            if (headerType != null) {
                addSection(sections, currentType, currentTitle, contentLines);
                currentType = headerType;
                currentTitle = cleanHeader(line);
                contentLines = new ArrayList<>();
            } else {
                contentLines.add(cleanContentLine(line));
            }
        }

        addSection(sections, currentType, currentTitle, contentLines);
        return new ParsedResume(sections);
    }

    public List<ResumeSection> getSections(String resumeText) {
        return parse(resumeText).getSections();
    }

    public List<ResumeSection> getSectionsByType(String resumeText, ResumeSectionType sectionType) {
        return parse(resumeText).getSectionsByType(sectionType);
    }

    private void addSection(
            List<ResumeSection> sections,
            ResumeSectionType sectionType,
            String sectionTitle,
            List<String> contentLines) {

        String content = contentLines.stream()
            .map(line -> line.trim())
                .filter(line -> !line.isEmpty())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");

        if (sectionType == null) {
            if (!content.isEmpty()) {
                sections.add(new ResumeSection(ResumeSectionType.OTHER, "OTHER", content));
            }
            return;
        }

        sections.add(new ResumeSection(sectionType, sectionTitle, content));
    }

    private ResumeSectionType detectHeader(String line) {
        String normalizedLine = normalizeHeader(line);
        if (normalizedLine.isEmpty()) {
            return null;
        }

        for (Map.Entry<ResumeSectionType, List<String>> entry : SECTION_HEADERS.entrySet()) {
            if (entry.getValue().contains(normalizedLine)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String cleanHeader(String line) {
        String header = line == null ? "" : line.trim();
        header = BULLET_PREFIX.matcher(header).replaceFirst("");
        return header.replaceFirst("\\s*[:\\-]+\\s*$", "").trim();
    }

    private String normalizeHeader(String line) {
        String header = cleanHeader(line)
                .toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return header;
    }

    private String cleanContentLine(String line) {
        if (line == null) {
            return "";
        }
        return BULLET_PREFIX.matcher(line.trim()).replaceFirst("").trim();
    }

    private static Map<ResumeSectionType, List<String>> createSectionHeaders() {
        Map<ResumeSectionType, List<String>> headers = new EnumMap<>(ResumeSectionType.class);
        headers.put(ResumeSectionType.SUMMARY, aliases(
                "summary", "profile", "professional summary", "about me"));
        headers.put(ResumeSectionType.SKILLS, aliases(
                "skills", "technical skills", "technologies", "technical expertise", "core skills"));
        headers.put(ResumeSectionType.EXPERIENCE, aliases(
                "experience", "work experience", "professional experience", "employment", "work history"));
        headers.put(ResumeSectionType.PROJECTS, aliases(
                "projects", "project experience", "personal projects", "academic projects"));
        headers.put(ResumeSectionType.EDUCATION, aliases(
                "education", "academic background", "educational qualification"));
        headers.put(ResumeSectionType.CERTIFICATIONS, aliases(
                "certifications", "certificates", "licenses certifications"));
        headers.put(ResumeSectionType.ACHIEVEMENTS, aliases(
                "achievements", "awards"));
        return headers;
    }

    private static List<String> aliases(String... values) {
        return List.of(values);
    }
}