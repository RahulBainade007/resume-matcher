package com.rahul.resumematcher.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeSectionParserTest {

    private final ResumeSectionParser parser = new ResumeSectionParser();

    @Test
    void parsesStandardSectionsInOriginalOrder() {
        ParsedResume resume = parser.parse("""
                SUMMARY
                Java developer with backend experience.

                SKILLS
                Java, Spring Boot

                EXPERIENCE
                Developed APIs.

                PROJECTS
                Resume Matcher

                EDUCATION
                B.Sc. Computer Science

                CERTIFICATIONS
                AWS Cloud Practitioner
                """);

        assertEquals(List.of(ResumeSectionType.SUMMARY, ResumeSectionType.SKILLS,
                        ResumeSectionType.EXPERIENCE, ResumeSectionType.PROJECTS,
                        ResumeSectionType.EDUCATION, ResumeSectionType.CERTIFICATIONS),
                resume.getSections().stream().map(section -> section.getSectionType()).toList());
        assertEquals("Java developer with backend experience.", resume.getSections().get(0).getContent());
    }

    @Test
    void recognizesHeaderAliases() {
        ParsedResume resume = parser.parse("""
                PROFESSIONAL SUMMARY
                Summary text
                TECHNICAL SKILLS
                Java
                WORK EXPERIENCE
                Work text
                PROFESSIONAL EXPERIENCE
                More work
                ACADEMIC PROJECTS
                Project text
                ACADEMIC BACKGROUND
                Education text
                LICENSES & CERTIFICATIONS
                Certificate text
                AWARDS
                Award text
                """);

        assertEquals(List.of(ResumeSectionType.SUMMARY, ResumeSectionType.SKILLS,
                        ResumeSectionType.EXPERIENCE, ResumeSectionType.EXPERIENCE,
                        ResumeSectionType.PROJECTS, ResumeSectionType.EDUCATION,
                        ResumeSectionType.CERTIFICATIONS, ResumeSectionType.ACHIEVEMENTS),
                resume.getSections().stream().map(section -> section.getSectionType()).toList());
    }

    @Test
    void recognizesCaseAndPunctuationVariations() {
        ParsedResume resume = parser.parse("""
                technical skills:
                Java
                Work Experience -
                Built services
                projects
                App
                """);

        assertEquals(ResumeSectionType.SKILLS, resume.getSections().get(0).getSectionType());
        assertEquals(ResumeSectionType.EXPERIENCE, resume.getSections().get(1).getSectionType());
        assertEquals(ResumeSectionType.PROJECTS, resume.getSections().get(2).getSectionType());
    }

    @Test
    void removesBulletAndNumberPrefixesFromContent() {
        ParsedResume resume = parser.parse("""
                TECHNICAL SKILLS
                • Java
                - Spring Boot
                1. PostgreSQL
                """);

        assertEquals("Java\nSpring Boot\nPostgreSQL", resume.getSections().get(0).getContent());
    }

    @Test
    void preservesUnknownContentAsOther() {
        ParsedResume resume = parser.parse("""
                Java Developer
                English and Hindi

                SKILLS
                Java
                """);

        assertEquals(ResumeSectionType.OTHER, resume.getSections().get(0).getSectionType());
        assertTrue(resume.getSections().get(0).getContent().contains("Java Developer"));
        assertTrue(resume.getSections().get(0).getContent().contains("English and Hindi"));
    }

    @Test
    void preservesUnrecognizedResumeWithoutDroppingText() {
        ParsedResume resume = parser.parse("Java Developer\nBuilt backend services.\nEnglish");

        assertEquals(1, resume.getSections().size());
        assertEquals(ResumeSectionType.OTHER, resume.getSections().get(0).getSectionType());
        assertTrue(resume.getSections().get(0).getContent().contains("Built backend services."));
    }

    @Test
    void supportsEmptyResume() {
        assertTrue(parser.parse("").getSections().isEmpty());
        assertTrue(parser.parse(null).getSections().isEmpty());
    }

    @Test
    void supportsMultipleSectionsOfSameTypeAndTypeFiltering() {
        ParsedResume resume = parser.parse("""
                PROJECTS
                Resume Matcher
                PROJECT EXPERIENCE
                Inventory App
                EDUCATION
                B.Sc.
                """);

        assertEquals(2, resume.getSectionsByType(ResumeSectionType.PROJECTS).size());
        assertEquals(1, resume.getSectionsByType(ResumeSectionType.EDUCATION).size());
        assertEquals("Resume Matcher", resume.getSections().get(0).getContent());
        assertEquals("Inventory App", resume.getSections().get(1).getContent());
    }

    @Test
    void parsesRealisticJavaDeveloperResume() {
        ParsedResume resume = parser.parse("""
                PROFESSIONAL SUMMARY
                Java developer interested in backend application development.

                TECHNICAL SKILLS:
                Java, Spring Boot, Hibernate, PostgreSQL, Docker

                EXPERIENCE
                Python Programming Intern
                Developed automation scripts.

                PROJECTS
                Resume Matcher
                Built a Spring Boot resume matching application.

                EDUCATION
                B.Sc. Computer Science

                CERTIFICATIONS
                Cybersecurity Certification
                """);

        assertEquals("Java developer interested in backend application development.",
                resume.getSectionsByType(ResumeSectionType.SUMMARY).get(0).getContent());
        assertTrue(resume.getSectionsByType(ResumeSectionType.SKILLS).get(0).getContent().contains("Spring Boot"));
        assertTrue(resume.getSectionsByType(ResumeSectionType.EXPERIENCE).get(0).getContent().contains("Developed automation scripts."));
        assertTrue(resume.getSectionsByType(ResumeSectionType.PROJECTS).get(0).getContent().contains("Resume Matcher"));
    }
}