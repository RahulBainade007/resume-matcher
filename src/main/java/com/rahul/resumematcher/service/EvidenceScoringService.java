package com.rahul.resumematcher.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class EvidenceScoringService {

    private static final Pattern EDUCATION_REQUIREMENT = Pattern.compile(
            "\\b(bachelor|bachelors|bachelor's|b\\.s\\.?|master|masters|master's|m\\.s\\.?|ph\\.?d\\.?|degree|diploma|education|academic)\\b");
    private static final Pattern CERTIFICATION_REQUIREMENT = Pattern.compile(
            "\\b(certification|certifications|certificate|certificates|certified|license|licenses|credential|credentials)\\b");

    private final EvidenceMatchingService evidenceMatchingService;

    public EvidenceScoringService(EvidenceMatchingService evidenceMatchingService) {
        this.evidenceMatchingService = evidenceMatchingService;
    }

    public EvidenceScoreResult score(
            ParsedResume resume,
            String jobDescription,
            Double semanticScore) {
        EvidenceMatchResult evidence = evidenceMatchingService.match(resume, jobDescription);
        String normalizedJobDescription = jobDescription == null
                ? ""
                : jobDescription.toLowerCase(Locale.ROOT);
        boolean hasEducationRequirement = EDUCATION_REQUIREMENT.matcher(normalizedJobDescription).find();
        boolean hasCertificationRequirement = CERTIFICATION_REQUIREMENT.matcher(normalizedJobDescription).find();

        return score(
                evidence,
                semanticScore,
                hasEducationRequirement,
                hasCertificationRequirement,
                educationSupported(resume, normalizedJobDescription, hasEducationRequirement),
                certificationSupported(resume, normalizedJobDescription, hasCertificationRequirement));
    }

    public EvidenceScoreResult score(EvidenceMatchResult evidence, Double semanticScore) {
        return score(evidence, semanticScore, false, false, false, false);
    }

    private EvidenceScoreResult score(
            EvidenceMatchResult evidence,
            Double semanticScore,
            boolean hasEducationRequirement,
            boolean hasCertificationRequirement,
            boolean educationSupported,
            boolean certificationSupported) {
        int totalSkills = evidence.getSkillEvidence().size();
        int matchedSkills = (int) evidence.getSkillEvidence().stream()
                .filter(item -> !item.getEvidenceSections().isEmpty())
                .count();
        int totalResponsibilities = evidence.getResponsibilityEvidence().size();
        int matchedResponsibilities = (int) evidence.getResponsibilityEvidence().stream()
                .filter(item -> !item.getEvidenceSections().isEmpty())
                .count();
        int experienceSupported = countByStrength(
                evidence, EvidenceStrength.DIRECT_EXPERIENCE);
        int projectSupported = countByStrength(
                evidence, EvidenceStrength.PROJECT_EVIDENCE);

        return new EvidenceScoreResult(
                percentage(matchedSkills, totalSkills),
                percentage(matchedResponsibilities, totalResponsibilities),
                percentage(experienceSupported, totalResponsibilities),
                percentage(projectSupported, totalResponsibilities),
                hasEducationRequirement ? percentage(educationSupported ? 1 : 0, 1) : 0.0,
                hasCertificationRequirement ? percentage(certificationSupported ? 1 : 0, 1) : 0.0,
                semanticScore,
                totalSkills,
                matchedSkills,
                totalResponsibilities,
                matchedResponsibilities,
                experienceSupported,
                projectSupported,
                hasEducationRequirement,
                hasCertificationRequirement);
    }

    private int countByStrength(EvidenceMatchResult evidence, EvidenceStrength strength) {
        return (int) evidence.getResponsibilityEvidence().stream()
                .filter(item -> item.getEvidenceStrengths().contains(strength))
                .count();
    }

    private boolean educationSupported(
            ParsedResume resume,
            String jobDescription,
            boolean hasRequirement) {
        if (!hasRequirement || resume == null) {
            return false;
        }
        Set<String> requirementTokens = meaningfulTokens(jobDescription);
        return resume.getSectionsByType(ResumeSectionType.EDUCATION).stream()
                .anyMatch(section -> requirementTokens.isEmpty()
                        || !Collections.disjoint(meaningfulTokens(section.getContent()), requirementTokens));
    }

    private boolean certificationSupported(
            ParsedResume resume,
            String jobDescription,
            boolean hasRequirement) {
        if (!hasRequirement || resume == null) {
            return false;
        }
        Set<String> requirementTokens = meaningfulTokens(jobDescription);
        return resume.getSectionsByType(ResumeSectionType.CERTIFICATIONS).stream()
                .anyMatch(section -> requirementTokens.isEmpty()
                        || !Collections.disjoint(meaningfulTokens(section.getContent()), requirementTokens));
    }

    private Set<String> meaningfulTokens(String text) {
        Set<String> tokens = new HashSet<>();
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (token.length() > 2 && !Set.of("the", "and", "for", "with", "preferred", "required").contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private double percentage(int numerator, int denominator) {
        return denominator == 0
                ? 0.0
                : Math.round((numerator * 1000.0) / denominator) / 10.0;
    }
}