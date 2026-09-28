package com.rahul.resumematcher.service;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SkillMatcherServiceTest {

    private final SkillMatcherService skillMatcherService = new SkillMatcherService();

    @Test
    void normalizesAliasesAndScoresOnlyTechnicalSkills() {
        SkillMatcherService.MatchResult result = skillMatcherService.match(
                "Java, Spring Boot, REST API, PostgreSQL",
                "Core Java, Spring Boot, REST APIs, PostgreSQL DB, Docker, good communication, teamwork");

        assertEquals(80.0, result.score());
        assertEquals(Set.of("java", "spring boot", "rest", "postgresql"),
                Set.copyOf(result.matchedSkills()));
        assertEquals(Set.of("docker"), Set.copyOf(result.missingSkills()));
    }

    @Test
    void ignoresGenericVocabularyAndShortAmbiguousTerms() {
        SkillMatcherService.MatchResult result = skillMatcherService.match(
                "Experienced developer with strong communication and teamwork",
                "Good communication, analytical ability, teamwork, problem solving, C, Go, R, AI");

        assertEquals(0.0, result.score());
        assertTrue(result.matchedSkills().isEmpty());
        assertTrue(result.missingSkills().isEmpty());
    }

    @Test
    void recognizesCommonAliasForms() {
        assertEquals(Set.of("java", "rest", "postgresql", "mongodb", "github", "ci/cd", "oop"),
                skillMatcherService.extractSkills(
                        "Java 17, RESTful APIs, Postgres, Mongo DBMS, Git Hub, CI CD pipeline, Object-Oriented Programming"));
    }
}