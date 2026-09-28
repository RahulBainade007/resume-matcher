package com.rahul.resumematcher.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ResponsibilityMatcherServiceTest {

    private final ResponsibilityMatcherService service = new ResponsibilityMatcherService();

    @Test
    void normalizesDevelopDesignAndOtherVerbForms() {
        assertEquals(List.of("develop rest apis", "design microservices"),
                service.extractResponsibilities("Develop REST APIs. Designed microservices."));
        assertEquals(List.of("develop rest apis", "design microservices"),
                service.extractResponsibilities("Developing REST APIs. Designing microservices."));
    }

    @Test
    void matchesApiSpringBootMicroservicePostgresqlAndJunitResponsibilities() {
        ResponsibilityMatcherService.ResponsibilityMatchResult result = service.match(
                "Developed REST APIs using Spring Boot and Hibernate. Designed microservices for an inventory management application. Used PostgreSQL for persistence. Created JUnit tests for service classes.",
                "Develop REST APIs using Spring Boot. Design and maintain microservices. Work with PostgreSQL databases. Write unit tests using JUnit.");

        assertEquals(100.0, result.getResponsibilityScore());
        assertEquals(4, result.getMatchedResponsibilities().size());
        assertTrue(result.getMatchedResponsibilities().contains("develop rest apis using spring boot"));
    }

    @Test
    void detectsMissingResponsibilityAndCalculatesEightyPercent() {
        ResponsibilityMatcherService.ResponsibilityMatchResult result = service.match(
                "Developed REST APIs using Spring Boot and Hibernate. Designed microservices for an inventory management application. Used PostgreSQL for persistence. Created JUnit tests for service classes.",
                "Develop REST APIs using Spring Boot. Design and maintain microservices. Work with PostgreSQL databases. Write unit tests using JUnit. Troubleshoot production issues.");

        assertEquals(80.0, result.getResponsibilityScore());
        assertEquals(List.of("troubleshoot production issues"), result.getMissingResponsibilities());
    }

    @Test
    void ignoresGenericStatements() {
        ResponsibilityMatcherService.ResponsibilityMatchResult result = service.match(
                "Strong communication and teamwork.",
                "Good communication. Work with team. Work in environment. Support company goals.");

        assertEquals(0.0, result.getResponsibilityScore());
        assertTrue(result.getMatchedResponsibilities().isEmpty());
        assertTrue(result.getMissingResponsibilities().isEmpty());
    }

    @Test
    void skillOnlyEvidenceDoesNotMatchResponsibility() {
        ResponsibilityMatcherService.ResponsibilityMatchResult result = service.match(
                "Knowledge of Spring Boot and REST APIs.",
                "Develop REST APIs using Spring Boot.");

        assertEquals(0.0, result.getResponsibilityScore());
        assertEquals(List.of("develop rest apis using spring boot"), result.getMissingResponsibilities());
    }

    @Test
    void handlesEmptyResumeAndJobDescription() {
        ResponsibilityMatcherService.ResponsibilityMatchResult emptyJob = service.match("Developed APIs.", "");
        ResponsibilityMatcherService.ResponsibilityMatchResult emptyResume = service.match("", "Develop APIs.");

        assertEquals(0.0, emptyJob.getResponsibilityScore());
        assertEquals(0.0, emptyResume.getResponsibilityScore());
        assertTrue(emptyResume.getMatchedResponsibilities().isEmpty());
        assertEquals(List.of("develop apis"), emptyResume.getMissingResponsibilities());
    }

    @Test
    void supportsCompleteAndPartialMatches() {
        ResponsibilityMatcherService.ResponsibilityMatchResult complete = service.match(
                "Built applications and tested services.",
                "Build applications. Test services.");
        ResponsibilityMatcherService.ResponsibilityMatchResult partial = service.match(
                "Built applications.",
                "Build applications. Test services.");

        assertEquals(100.0, complete.getResponsibilityScore());
        assertEquals(50.0, partial.getResponsibilityScore());
        assertEquals(List.of("test services"), partial.getMissingResponsibilities());
    }
}