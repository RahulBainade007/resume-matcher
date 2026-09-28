package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class SkillMatcherService {

    private static final Map<String, List<String>> SKILL_ALIASES = createSkillAliases();
    private static final List<SkillPattern> SKILL_PATTERNS = createSkillPatterns();

    public MatchResult match(String resumeText, String jobDescription) {
        Set<String> requiredSkills = extractSkills(jobDescription);
        Set<String> resumeSkills = extractSkills(resumeText);

        Set<String> matchedSkills = new LinkedHashSet<>(requiredSkills);
        matchedSkills.retainAll(resumeSkills);

        Set<String> missingSkills = new LinkedHashSet<>(requiredSkills);
        missingSkills.removeAll(resumeSkills);

        double score = requiredSkills.isEmpty()
                ? 0.0
                : Math.round((matchedSkills.size() * 1000.0) / requiredSkills.size()) / 10.0;

        return new MatchResult(
                score,
                new ArrayList<>(matchedSkills),
                new ArrayList<>(missingSkills));
    }

    public Set<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return new LinkedHashSet<>();
        }

        String normalizedText = text.toLowerCase(Locale.ROOT);
        Set<String> skills = new LinkedHashSet<>();
        List<int[]> matchedRanges = new ArrayList<>();
        for (SkillPattern skillPattern : SKILL_PATTERNS) {
            var matcher = skillPattern.pattern().matcher(normalizedText);
            while (matcher.find()) {
                if (matchedRanges.stream().noneMatch(range -> overlaps(range, matcher.start(), matcher.end()))) {
                    matchedRanges.add(new int[]{matcher.start(), matcher.end()});
                    skills.add(skillPattern.canonicalName());
                }
            }
        }
        return skills;
    }

    private boolean overlaps(int[] range, int start, int end) {
        return start < range[1] && end > range[0];
    }

    private static Map<String, List<String>> createSkillAliases() {
        Map<String, List<String>> aliases = new LinkedHashMap<>();
        add(aliases, "java", "java", "core java", "java se", "java 8", "java 11", "java 17", "java 21", "java 25");
        add(aliases, "spring framework", "spring", "spring framework");
        add(aliases, "spring boot", "spring boot");
        add(aliases, "spring security", "spring security");
        add(aliases, "spring data jpa", "spring data jpa", "spring data");
        add(aliases, "hibernate", "hibernate");
        add(aliases, "jpa", "jpa");
        add(aliases, "rest", "rest api", "rest apis", "restful api", "restful apis", "rest");
        add(aliases, "microservices", "microservices", "microservices architecture");
        add(aliases, "sql", "sql");
        add(aliases, "mysql", "mysql");
        add(aliases, "postgresql", "postgresql", "postgresql db", "postgres");
        add(aliases, "mongodb", "mongodb", "mongo db", "mongo dbms");
        add(aliases, "nosql", "nosql");
        add(aliases, "docker", "docker");
        add(aliases, "kubernetes", "kubernetes");
        add(aliases, "aws", "aws");
        add(aliases, "azure", "azure");
        add(aliases, "gcp", "gcp");
        add(aliases, "git", "git");
        add(aliases, "github", "github", "git hub");
        add(aliases, "maven", "maven");
        add(aliases, "gradle", "gradle");
        add(aliases, "junit", "junit", "junit 4", "junit 5");
        add(aliases, "mockito", "mockito");
        add(aliases, "jenkins", "jenkins");
        add(aliases, "ci/cd", "ci/cd", "ci cd", "ci/cd pipeline");
        add(aliases, "agile", "agile");
        add(aliases, "scrum", "scrum");
        add(aliases, "oop", "oop", "object oriented programming", "object-oriented programming");
        add(aliases, "multithreading", "multithreading");
        add(aliases, "collections", "collections");
        add(aliases, "design patterns", "design patterns");
        add(aliases, "system design", "system design");
        add(aliases, "data structures", "data structures");
        add(aliases, "algorithms", "algorithms");
        add(aliases, "kafka", "kafka");
        add(aliases, "rabbitmq", "rabbitmq");
        add(aliases, "redis", "redis");
        add(aliases, "html", "html");
        add(aliases, "css", "css");
        add(aliases, "javascript", "javascript");
        add(aliases, "typescript", "typescript");
        add(aliases, "react", "react");
        add(aliases, "angular", "angular");
        add(aliases, "node.js", "node.js", "nodejs");
        add(aliases, "python", "python");
        add(aliases, "linux", "linux");
        add(aliases, "unix", "unix");
        add(aliases, "shell scripting", "shell scripting");
        add(aliases, "api gateway", "api gateway");
        add(aliases, "jwt", "jwt");
        add(aliases, "oauth", "oauth");
        add(aliases, "swagger", "swagger");
        add(aliases, "openapi", "openapi");
        add(aliases, "unit testing", "unit testing");
        add(aliases, "integration testing", "integration testing");
        add(aliases, "tdd", "tdd");
        add(aliases, "postman", "postman");
        add(aliases, "jira", "jira");
        add(aliases, "elasticsearch", "elasticsearch");
        add(aliases, "graphql", "graphql");
        add(aliases, "load balancing", "load balancing");
        add(aliases, "caching", "caching");
        return aliases;
    }

    private static void add(Map<String, List<String>> aliases, String canonicalName, String... values) {
        aliases.put(canonicalName, List.of(values));
    }

    private static List<SkillPattern> createSkillPatterns() {
        List<SkillPattern> patterns = new ArrayList<>();
        SKILL_ALIASES.forEach((canonicalName, aliases) -> aliases.forEach(alias -> patterns.add(
                new SkillPattern(canonicalName, phrasePattern(alias)))));
        patterns.sort(Comparator.comparingInt((SkillPattern skill) -> skill.pattern().pattern().length()).reversed());
        return List.copyOf(patterns);
    }

    private static Pattern phrasePattern(String phrase) {
        String escaped = Pattern.quote(phrase);
        return Pattern.compile("(?<![a-z0-9])" + escaped + "(?![a-z0-9])");
    }

    private record SkillPattern(String canonicalName, Pattern pattern) {
    }

    public record MatchResult(double score, List<String> matchedSkills, List<String> missingSkills) {
    }
}