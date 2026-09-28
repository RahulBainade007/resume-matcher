package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceScoringServiceTest {

    private final EvidenceScoringService service = new EvidenceScoringService(
            new EvidenceMatchingService(new SkillMatcherService(), new ResponsibilityMatcherService()));
    private final ResumeSectionParser parser = new ResumeSectionParser();

    @Test
    void calculatesEightyPercentTechnicalSkillScore() {
        EvidenceScoreResult result = score("SKILLS\nJava, Spring Boot, REST, PostgreSQL", "Java, Spring Boot, REST, PostgreSQL, Docker");
        assertEquals(80.0, result.getTechnicalSkillScore());
        assertEquals(4, result.getMatchedSkills());
    }

    @Test
    void calculatesEightyPercentResponsibilityScore() {
        EvidenceScoreResult result = score("EXPERIENCE\nDeveloped REST APIs. Built applications. Wrote unit tests. Deployed applications.",
            "Develop REST APIs. Build applications. Write unit tests. Deploy applications. Troubleshoot issues.");
        assertEquals(80.0, result.getResponsibilityScore());
    }

    @Test
    void experienceScoreCountsOnlyDirectExperience() {
        EvidenceScoreResult result = score("EXPERIENCE\nDeveloped REST APIs. Wrote unit tests.\nPROJECTS\nBuilt applications.",
                "Develop REST APIs. Write unit tests. Build applications. Deploy applications.");
        assertEquals(50.0, result.getExperienceEvidenceScore());
        assertEquals(25.0, result.getProjectEvidenceScore());
    }

    @Test
    void skillsDoNotCountAsExperienceOrResponsibilityEvidence() {
        EvidenceScoreResult result = score("SKILLS\nSpring Boot, REST", "Develop REST APIs using Spring Boot.");
        assertEquals(0.0, result.getExperienceEvidenceScore());
        assertEquals(0.0, result.getResponsibilityScore());
    }

    @Test
    void educationAndCertificationScoresAreApplicableOnlyWhenRequired() {
        EvidenceScoreResult education = score("EDUCATION\nB.Sc. Computer Science", "Bachelor's degree in Computer Science");
        EvidenceScoreResult noEducation = score("EDUCATION\nB.Sc. Computer Science", "Experience with Docker");
        EvidenceScoreResult certification = score("CERTIFICATIONS\nAWS Certified Developer", "AWS certification preferred");
        EvidenceScoreResult practicalAws = score("CERTIFICATIONS\nAWS certification", "Experience with AWS");

        assertTrue(education.hasEducationRequirement());
        assertEquals(100.0, education.getEducationEvidenceScore());
        assertFalse(noEducation.hasEducationRequirement());
        assertEquals(0.0, noEducation.getEducationEvidenceScore());
        assertTrue(certification.hasCertificationRequirement());
        assertEquals(100.0, certification.getCertificationEvidenceScore());
        assertFalse(practicalAws.hasCertificationRequirement());
        assertEquals(0.0, practicalAws.getCertificationEvidenceScore());
    }

    @Test
    void missingEvidenceAndEmptyInputsScoreZero() {
        EvidenceScoreResult missing = score("SKILLS\nJava", "Java, Docker");
        EvidenceScoreResult empty = score("", "");

        assertEquals(50.0, missing.getTechnicalSkillScore());
        assertEquals(0.0, missing.getResponsibilityScore());
        assertEquals(0.0, empty.getTechnicalSkillScore());
        assertEquals(0.0, empty.getResponsibilityScore());
        assertFalse(empty.hasEducationRequirement());
        assertFalse(empty.hasCertificationRequirement());
    }

    @Test
    void preservesProvidedSemanticScoreWithoutRecalculatingIt() {
        EvidenceScoreResult result = service.score(
                new EvidenceMatchResult(java.util.List.of(), java.util.List.of()), 73.4);
        assertEquals(73.4, result.getSemanticScore());
    }

    @Test
    void projectEvidenceDoesNotCountAsDirectExperience() {
        EvidenceScoreResult result = score("PROJECTS\nBuilt REST APIs and deployed applications.",
                "Build REST APIs. Deploy applications.");

        assertEquals(0.0, result.getExperienceEvidenceScore());
        assertEquals(100.0, result.getProjectEvidenceScore());
    }

    @Test
    void preservesMultipleEvidenceSourcesWithoutChangingDiagnosticWeights() {
        EvidenceScoreResult result = score("SKILLS\nJava\nEXPERIENCE\nDeveloped Java services.\nPROJECTS\nBuilt a Java application.",
                "Java. Develop services.");

        assertEquals(100.0, result.getTechnicalSkillScore());
        assertEquals(100.0, result.getExperienceEvidenceScore());
        assertEquals(0.0, result.getProjectEvidenceScore());
    }

    @Test
    void doesNotInventEducationOrCertificationRequirements() {
        EvidenceScoreResult result = score("EDUCATION\nB.Sc. Computer Science\nCERTIFICATIONS\nAWS Certified Developer",
                "Experience with Java and AWS.");

        assertFalse(result.hasEducationRequirement());
        assertFalse(result.hasCertificationRequirement());
        assertEquals(0.0, result.getEducationEvidenceScore());
        assertEquals(0.0, result.getCertificationEvidenceScore());
    }

    @Test
    void scoresRealisticJavaDeveloperEvidenceIndependently() {
        EvidenceScoreResult result = score("""
                SUMMARY:
                Java developer interested in application development.
                SKILLS:
                Java, Spring Boot, REST, PostgreSQL
                EXPERIENCE:
                Python Programming Intern
                Developed automation scripts.
                Tested applications.
                PROJECTS:
                Resume Matcher
                Built REST APIs using Spring Boot and PostgreSQL.
                Created JUnit tests for service classes.
                EDUCATION:
                B.Sc. Computer Science
                CERTIFICATIONS:
                Cybersecurity certification
                """, """
                Junior Java Developer.
                Requirements: Java, Spring Boot, REST APIs, PostgreSQL, Docker.
                Responsibilities:
                Develop REST APIs using Spring Boot.
                Build backend applications.
                Write unit tests using JUnit.
                Work with PostgreSQL databases.
                """);

        assertEquals(80.0, result.getTechnicalSkillScore());
        assertEquals(50.0, result.getExperienceEvidenceScore());
        assertTrue(result.getProjectEvidenceScore() > 0.0);
        assertEquals(0.0, result.getCertificationEvidenceScore());
    }

    private EvidenceScoreResult score(String resumeText, String jobDescription) {
        return service.score(parser.parse(resumeText), jobDescription, null);
    }
}