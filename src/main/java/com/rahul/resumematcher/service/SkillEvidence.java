package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillEvidence {

    private final String canonicalSkill;
    private final List<ResumeSectionType> evidenceSections;
    private final List<String> evidenceTexts;
    private final List<EvidenceStrength> evidenceStrengths;

    public SkillEvidence(
            String canonicalSkill,
            List<ResumeSectionType> evidenceSections,
            List<String> evidenceTexts,
            List<EvidenceStrength> evidenceStrengths) {
        this.canonicalSkill = canonicalSkill;
        this.evidenceSections = immutableCopy(evidenceSections);
        this.evidenceTexts = immutableCopy(evidenceTexts);
        this.evidenceStrengths = immutableCopy(evidenceStrengths);
    }

    public String getCanonicalSkill() {
        return canonicalSkill;
    }

    public List<ResumeSectionType> getEvidenceSections() {
        return evidenceSections;
    }

    public List<String> getEvidenceTexts() {
        return evidenceTexts;
    }

    public List<EvidenceStrength> getEvidenceStrengths() {
        return evidenceStrengths;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}