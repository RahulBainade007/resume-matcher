package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvidenceMatchResult {

    private final List<SkillEvidence> skillEvidence;
    private final List<ResponsibilityEvidence> responsibilityEvidence;

    public EvidenceMatchResult(
            List<SkillEvidence> skillEvidence,
            List<ResponsibilityEvidence> responsibilityEvidence) {
        this.skillEvidence = immutableCopy(skillEvidence);
        this.responsibilityEvidence = immutableCopy(responsibilityEvidence);
    }

    public List<SkillEvidence> getSkillEvidence() {
        return skillEvidence;
    }

    public List<ResponsibilityEvidence> getResponsibilityEvidence() {
        return responsibilityEvidence;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}