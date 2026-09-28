package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ResponsibilityMatcherService {

    private static final Map<String, String> ACTION_FORMS = Map.ofEntries(
            Map.entry("develop", "develop"), Map.entry("developing", "develop"), Map.entry("developed", "develop"),
            Map.entry("build", "build"), Map.entry("building", "build"), Map.entry("built", "build"),
            Map.entry("design", "design"), Map.entry("designing", "design"), Map.entry("designed", "design"),
            Map.entry("implement", "implement"), Map.entry("implementing", "implement"), Map.entry("implemented", "implement"),
            Map.entry("maintain", "maintain"), Map.entry("maintaining", "maintain"), Map.entry("maintained", "maintain"),
            Map.entry("create", "create"), Map.entry("creating", "create"), Map.entry("created", "create"),
            Map.entry("write", "write"), Map.entry("writing", "write"), Map.entry("wrote", "write"),
            Map.entry("test", "test"), Map.entry("testing", "test"), Map.entry("tested", "test"),
            Map.entry("deploy", "deploy"), Map.entry("deploying", "deploy"), Map.entry("deployed", "deploy"),
            Map.entry("configure", "configure"), Map.entry("configuring", "configure"), Map.entry("configured", "configure"),
            Map.entry("integrate", "integrate"), Map.entry("integrating", "integrate"), Map.entry("integrated", "integrate"),
            Map.entry("debug", "debug"), Map.entry("debugging", "debug"), Map.entry("debugged", "debug"),
            Map.entry("troubleshoot", "troubleshoot"), Map.entry("troubleshooting", "troubleshoot"), Map.entry("troubleshot", "troubleshoot"),
            Map.entry("optimize", "optimize"), Map.entry("optimizing", "optimize"), Map.entry("optimized", "optimize"),
            Map.entry("automate", "automate"), Map.entry("automating", "automate"), Map.entry("automated", "automate"),
            Map.entry("monitor", "monitor"), Map.entry("monitoring", "monitor"), Map.entry("monitored", "monitor"),
            Map.entry("analyze", "analyze"), Map.entry("analyzing", "analyze"), Map.entry("analyzed", "analyze"),
            Map.entry("manage", "manage"), Map.entry("managing", "manage"), Map.entry("managed", "manage"),
            Map.entry("support", "support"), Map.entry("supporting", "support"), Map.entry("supported", "support"),
            Map.entry("secure", "secure"), Map.entry("securing", "secure"), Map.entry("secured", "secure"),
            Map.entry("document", "document"), Map.entry("documenting", "document"), Map.entry("documented", "document"),
                Map.entry("refactor", "refactor"), Map.entry("refactoring", "refactor"), Map.entry("refactored", "refactor"),
                Map.entry("work", "work"), Map.entry("working", "work"), Map.entry("worked", "work"));

            private static final Set<String> EVIDENCE_ACTIONS = Set.of("use", "using", "used");

    private static final Set<String> GENERIC_CONTEXT = Set.of(
            "a", "an", "and", "be", "candidate", "company", "communication", "environment", "experience",
            "for", "good", "goals", "in", "knowledge", "of", "on", "responsible", "responsibilities",
            "skills", "strong", "team", "teamwork", "the", "to", "used", "use", "using", "with", "work", "working");

    private static final Set<String> CONCRETE_CONTEXT = Set.of(
            "api", "application", "database", "data", "deployment", "issue", "microservice", "production",
            "software", "system", "test", "testing", "service", "security", "automation", "integration");

    private static final Pattern SENTENCE_SEPARATOR = Pattern.compile("[.!?;\\r\\n]+");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9+#]+(?:[./-][a-z0-9+#]+)*");

    public ResponsibilityMatchResult match(String resumeText, String jobDescription) {
        List<Responsibility> jobResponsibilities = extractResponsibilityDetails(jobDescription, false);
        List<Responsibility> resumeResponsibilities = extractResponsibilityDetails(resumeText, true);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Responsibility jobResponsibility : jobResponsibilities) {
            if (resumeResponsibilities.stream().anyMatch(jobResponsibility::hasEvidenceIn)) {
                matched.add(jobResponsibility.text());
            } else {
                missing.add(jobResponsibility.text());
            }
        }

        double score = jobResponsibilities.isEmpty()
                ? 0.0
                : Math.round((matched.size() * 1000.0) / jobResponsibilities.size()) / 10.0;
        return new ResponsibilityMatchResult(score, matched, missing);
    }

    public List<String> extractResponsibilities(String text) {
        return extractResponsibilityDetails(text, false).stream()
            .map(responsibility -> responsibility.text())
                .collect(Collectors.toList());
    }

    private List<Responsibility> extractResponsibilityDetails(String text, boolean allowEvidenceActions) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Responsibility> responsibilities = new ArrayList<>();
        for (String sentence : SENTENCE_SEPARATOR.split(text.toLowerCase(Locale.ROOT))) {
            String normalized = normalizeActions(sentence);
            if (normalized.isBlank() || !containsAction(normalized, allowEvidenceActions)) {
                continue;
            }

            String cleaned = normalized.replaceAll("^[\\s,\\-:*]+|[\\s,\\-:*]+$", "").trim();
            Set<String> context = contextTokens(cleaned);
            if (context.isEmpty()) {
                continue;
            }
            responsibilities.add(new Responsibility(cleaned, context));
        }
        return responsibilities;
    }

    private String normalizeActions(String text) {
        String normalized = text.replaceAll("[\\u2010-\\u2015]", "-");
        for (Map.Entry<String, String> entry : ACTION_FORMS.entrySet()) {
            normalized = normalized.replaceAll("(?<![a-z0-9])" + Pattern.quote(entry.getKey()) + "(?![a-z0-9])", entry.getValue());
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private boolean containsAction(String text, boolean allowEvidenceActions) {
        return tokenize(text).stream().anyMatch(token -> ACTION_FORMS.containsKey(token)
                || (allowEvidenceActions && EVIDENCE_ACTIONS.contains(token)));
    }

    private Set<String> contextTokens(String responsibility) {
        Set<String> tokens = tokenize(responsibility).stream()
                .map(this::normalizeToken)
                .filter(token -> !ACTION_FORMS.values().contains(token))
                .filter(token -> !GENERIC_CONTEXT.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (tokens.size() == 1 && tokens.stream().noneMatch(CONCRETE_CONTEXT::contains)) {
            return Set.of();
        }
        return tokens;
    }

    private Set<String> tokenize(String text) {
        var matcher = TOKEN_PATTERN.matcher(text);
        Set<String> tokens = new LinkedHashSet<>();
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private String normalizeToken(String token) {
        String normalized = token.toLowerCase(Locale.ROOT);
        if (normalized.endsWith("ies") && normalized.length() > 3) {
            return normalized.substring(0, normalized.length() - 3) + "y";
        }
        if (normalized.endsWith("s") && normalized.length() > 3) {
            return normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.equals("persistence")) {
            return "database";
        }
        return normalized;
    }

    private record Responsibility(String text, Set<String> contextTokens) {
        private boolean hasEvidenceIn(Responsibility resumeResponsibility) {
            Set<String> overlap = new LinkedHashSet<>(contextTokens);
            overlap.retainAll(resumeResponsibility.contextTokens());
            return !overlap.isEmpty();
        }
    }

    public static class ResponsibilityMatchResult {
        private final double responsibilityScore;
        private final List<String> matchedResponsibilities;
        private final List<String> missingResponsibilities;

        public ResponsibilityMatchResult(
                double responsibilityScore,
                List<String> matchedResponsibilities,
                List<String> missingResponsibilities) {
            this.responsibilityScore = responsibilityScore;
            this.matchedResponsibilities = matchedResponsibilities;
            this.missingResponsibilities = missingResponsibilities;
        }

        public double getResponsibilityScore() {
            return responsibilityScore;
        }

        public List<String> getMatchedResponsibilities() {
            return matchedResponsibilities;
        }

        public List<String> getMissingResponsibilities() {
            return missingResponsibilities;
        }
    }
}