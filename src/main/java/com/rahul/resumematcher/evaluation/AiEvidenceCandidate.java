package com.rahul.resumematcher.evaluation;

public record AiEvidenceCandidate(
        String requirement,
        String chunkText,
        String section,
        double similarityScore) {
}