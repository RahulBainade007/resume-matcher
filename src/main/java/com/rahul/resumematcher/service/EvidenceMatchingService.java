package com.rahul.resumematcher.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class EvidenceMatchingService {

    private final SkillMatcherService skillMatcherService;
    private final ResponsibilityMatcherService responsibilityMatcherService;

    public EvidenceMatchingService(
            SkillMatcherService skillMatcherService,
            ResponsibilityMatcherService responsibilityMatcherService) {
        this.skillMatcherService = skillMatcherService;
        this.responsibilityMatcherService = responsibilityMatcherService;
    }

    public EvidenceMatchResult match(ParsedResume resume, String jobDescription) {
        if (resume == null) {
            return new EvidenceMatchResult(List.of(), List.of());
        }

        List<SkillEvidence> skillEvidence = buildSkillEvidence(resume, jobDescription);
        List<ResponsibilityEvidence> responsibilityEvidence = buildResponsibilityEvidence(resume, jobDescription);
        return new EvidenceMatchResult(skillEvidence, responsibilityEvidence);
    }

    private List<SkillEvidence> buildSkillEvidence(ParsedResume resume, String jobDescription) {
        List<SkillEvidence> evidence = new ArrayList<>();
        for (String skill : skillMatcherService.extractSkills(jobDescription)) {
            List<ResumeSectionType> sections = new ArrayList<>();
            List<String> texts = new ArrayList<>();
            List<EvidenceStrength> strengths = new ArrayList<>();

            for (ResumeSection section : resume.getSections()) {
                if (skillMatcherService.extractSkills(section.getContent()).contains(skill)) {
                    sections.add(section.getSectionType());
                    texts.add(section.getContent());
                    strengths.add(strengthFor(section.getSectionType()));
                }
            }

            if (sections.isEmpty()) {
                strengths.add(EvidenceStrength.NO_EVIDENCE);
            }
            evidence.add(new SkillEvidence(skill, sections, texts, strengths));
        }
        return evidence;
    }

    private List<ResponsibilityEvidence> buildResponsibilityEvidence(
            ParsedResume resume,
            String jobDescription) {
        List<ResponsibilityEvidence> evidence = new ArrayList<>();
        for (String responsibility : responsibilityMatcherService.extractResponsibilities(jobDescription)) {
            List<ResumeSectionType> sections = new ArrayList<>();
            List<String> texts = new ArrayList<>();
            List<EvidenceStrength> strengths = new ArrayList<>();

            for (ResumeSection section : resume.getSections()) {
                if (section.getSectionType() == ResumeSectionType.SKILLS
                        || section.getSectionType() == ResumeSectionType.EDUCATION
                        || section.getSectionType() == ResumeSectionType.CERTIFICATIONS) {
                    continue;
                }

                ResponsibilityMatcherService.ResponsibilityMatchResult sectionResult =
                        responsibilityMatcherService.match(section.getContent(), responsibility);
                if (sectionResult.getMatchedResponsibilities().stream()
                        .anyMatch(matched -> sameResponsibility(matched, responsibility))) {
                    sections.add(section.getSectionType());
                    texts.add(section.getContent());
                    strengths.add(strengthFor(section.getSectionType()));
                }
            }

            if (sections.isEmpty()) {
                strengths.add(EvidenceStrength.NO_EVIDENCE);
            }
            evidence.add(new ResponsibilityEvidence(responsibility, sections, texts, strengths));
        }
        return evidence;
    }

    private boolean sameResponsibility(String matched, String requested) {
        return matched.toLowerCase(Locale.ROOT).equals(requested.toLowerCase(Locale.ROOT));
    }

    private EvidenceStrength strengthFor(ResumeSectionType sectionType) {
        return switch (sectionType) {
            case EXPERIENCE, WORK_EXPERIENCE -> EvidenceStrength.DIRECT_EXPERIENCE;
            case PROJECTS -> EvidenceStrength.PROJECT_EVIDENCE;
            case SKILLS -> EvidenceStrength.SKILL_LISTING;
            case EDUCATION -> EvidenceStrength.EDUCATION_EVIDENCE;
            case CERTIFICATIONS -> EvidenceStrength.CERTIFICATION_EVIDENCE;
            case SUMMARY -> EvidenceStrength.SUMMARY_EVIDENCE;
            default -> EvidenceStrength.OTHER_EVIDENCE;
        };
    }
}