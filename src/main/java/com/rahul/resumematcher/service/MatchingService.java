package com.rahul.resumematcher.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Rule-based resume <-> job description matcher.
 *
 * Delegates technical-skill extraction and matching to SkillMatcherService while preserving
 * the result shape consumed by the hybrid matcher and controllers.
 *
 * This runs entirely locally (no external API calls, no cost, no network dependency), which makes
 * the endpoint usable out of the box. AiMatchingService can optionally layer an LLM-based review
 * on top of this when an API key is configured.
 */
@Service
public class MatchingService {

    private final SkillMatcherService skillMatcherService;

    public MatchingService() {
        this(new SkillMatcherService());
    }

    @Autowired
    public MatchingService(SkillMatcherService skillMatcherService) {
        this.skillMatcherService = skillMatcherService;
    }

    public MatchResult match(String resumeText, String jobDescription) {
        SkillMatcherService.MatchResult result = skillMatcherService.match(resumeText, jobDescription);
        return new MatchResult(result.score(), result.matchedSkills(), result.missingSkills());
    }

    public static class MatchResult {
        private final double score;
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;

        public MatchResult(double score, List<String> matchedKeywords, List<String> missingKeywords) {
            this.score = score;
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
        }

        public double getScore() {
            return score;
        }

        public List<String> getMatchedKeywords() {
            return matchedKeywords;
        }

        public List<String> getMissingKeywords() {
            return missingKeywords;
        }
    }
}
