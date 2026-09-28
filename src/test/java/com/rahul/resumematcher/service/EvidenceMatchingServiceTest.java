package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceMatchingServiceTest {

    private final EvidenceMatchingService service = new EvidenceMatchingService(
            new SkillMatcherService(), new ResponsibilityMatcherService());
    private final ResumeSectionParser parser = new ResumeSectionParser();

    @Test
    void classifiesSkillEvidenceBySectionAndPreservesMultipleSources() {
        EvidenceMatchResult result = service.match(parser.parse("""
                SKILLS
                Java, Spring Boot, PostgreSQL
                PROJECTS
                Built a Spring Boot application using PostgreSQL.
                """), "Java, Spring Boot, PostgreSQL, Docker");

        Map<String, SkillEvidence> evidence = result.getSkillEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getCanonicalSkill(), Function.identity()));
        assertEquals(List.of(ResumeSectionType.SKILLS), evidence.get("java").getEvidenceSections());
        assertEquals(List.of(ResumeSectionType.SKILLS, ResumeSectionType.PROJECTS),
                evidence.get("spring boot").getEvidenceSections());
        assertEquals(List.of(EvidenceStrength.SKILL_LISTING, EvidenceStrength.PROJECT_EVIDENCE),
                evidence.get("postgresql").getEvidenceStrengths());
        assertTrue(evidence.get("spring boot").getEvidenceTexts().get(1).contains("Spring Boot"));
        assertTrue(evidence.get("docker").getEvidenceSections().isEmpty());
        assertEquals(List.of(EvidenceStrength.NO_EVIDENCE), evidence.get("docker").getEvidenceStrengths());
    }

    @Test
    void classifiesResponsibilityEvidenceInExperienceAndProjects() {
        EvidenceMatchResult result = service.match(parser.parse("""
                EXPERIENCE
                Developed REST APIs using Spring Boot.
                PROJECTS
                Built a microservices-based inventory application.
                """), "Develop REST APIs using Spring Boot. Build microservices.");

        Map<String, ResponsibilityEvidence> evidence = result.getResponsibilityEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getResponsibility(), Function.identity()));
        assertEquals(List.of(ResumeSectionType.EXPERIENCE),
                evidence.get("develop rest apis using spring boot").getEvidenceSections());
        assertEquals(List.of(EvidenceStrength.DIRECT_EXPERIENCE),
                evidence.get("develop rest apis using spring boot").getEvidenceStrengths());
        assertEquals(List.of(ResumeSectionType.PROJECTS),
                evidence.get("build microservices").getEvidenceSections());
    }

    @Test
    void skillOnlyEvidenceDoesNotBecomeResponsibilityEvidence() {
        EvidenceMatchResult result = service.match(parser.parse("""
                SKILLS
                Spring Boot, REST
                """), "Develop REST APIs using Spring Boot.");

        ResponsibilityEvidence evidence = result.getResponsibilityEvidence().get(0);
        assertTrue(evidence.getEvidenceSections().isEmpty());
        assertEquals(List.of(EvidenceStrength.NO_EVIDENCE), evidence.getEvidenceStrengths());
    }

    @Test
    void classifiesEducationCertificationAndSummarySkillEvidence() {
        EvidenceMatchResult result = service.match(parser.parse("""
                SUMMARY
                Java developer.
                EDUCATION
                Coursework: Java Programming.
                CERTIFICATIONS
                Spring Boot certification.
                """), "Java, Spring Boot");

        Map<String, SkillEvidence> evidence = result.getSkillEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getCanonicalSkill(), Function.identity()));
        assertEquals(List.of(EvidenceStrength.SUMMARY_EVIDENCE, EvidenceStrength.EDUCATION_EVIDENCE),
                evidence.get("java").getEvidenceStrengths());
        assertEquals(List.of(EvidenceStrength.CERTIFICATION_EVIDENCE),
                evidence.get("spring boot").getEvidenceStrengths());
    }

    @Test
    void handlesCaseInsensitiveMatchingAndMissingResponsibility() {
        EvidenceMatchResult result = service.match(parser.parse("""
                projects
                BUILT REST APIS USING SPRING BOOT.
                """), "develop rest apis using spring boot. write unit tests.");

        Map<String, ResponsibilityEvidence> evidence = result.getResponsibilityEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getResponsibility(), Function.identity()));
        assertEquals(List.of(ResumeSectionType.PROJECTS),
                evidence.get("develop rest apis using spring boot").getEvidenceSections());
        assertEquals(List.of(EvidenceStrength.NO_EVIDENCE),
                evidence.get("write unit tests").getEvidenceStrengths());
    }

    @Test
    void supportsRealisticJavaDeveloperResumeWithoutInferringDocker() {
        EvidenceMatchResult result = service.match(parser.parse("""
                SUMMARY:
                Java developer interested in application development.
                SKILLS:
                Java, Spring Boot, REST, PostgreSQL
                EXPERIENCE:
                Python Programming Intern
                Developed automation scripts and tested applications.
                PROJECTS:
                Resume Matcher
                Built REST APIs using Spring Boot and PostgreSQL.
                Created JUnit tests for service classes.
                EDUCATION:
                B.Sc. Computer Science
                """), """
                Junior Java Developer.
                Required: Java, Spring Boot, REST, PostgreSQL and Docker.
                Responsibilities:
                Develop REST APIs using Spring Boot.
                Build and maintain backend applications.
                Write unit tests.
                """);

        Map<String, SkillEvidence> skills = result.getSkillEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getCanonicalSkill(), Function.identity()));
        assertEquals(List.of(ResumeSectionType.SKILLS, ResumeSectionType.PROJECTS),
                skills.get("spring boot").getEvidenceSections());
        assertTrue(skills.get("docker").getEvidenceSections().isEmpty());

        Map<String, ResponsibilityEvidence> responsibilities = result.getResponsibilityEvidence().stream()
                .collect(Collectors.toMap(evidenceItem -> evidenceItem.getResponsibility(), Function.identity()));
        assertEquals(List.of(ResumeSectionType.PROJECTS),
                responsibilities.get("develop rest apis using spring boot").getEvidenceSections());
        assertEquals(List.of(ResumeSectionType.PROJECTS),
                responsibilities.get("write unit tests").getEvidenceSections());
    }
}