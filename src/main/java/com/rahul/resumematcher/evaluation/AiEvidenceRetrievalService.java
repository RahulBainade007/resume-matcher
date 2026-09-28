package com.rahul.resumematcher.evaluation;

import com.rahul.resumematcher.repository.DocumentChunkRepository;
import com.rahul.resumematcher.service.EmbeddingService;
import com.rahul.resumematcher.service.MatchingProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiEvidenceRetrievalService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;
    private final MatchingProperties matchingProperties;

    public AiEvidenceRetrievalService(
            EmbeddingService embeddingService,
            DocumentChunkRepository documentChunkRepository,
            MatchingProperties matchingProperties) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
        this.matchingProperties = matchingProperties;
    }

    public List<AiEvidenceCandidate> retrieve(String resumeId, AiJobAnalysis analysis) {
        if (resumeId == null || resumeId.isBlank()) {
            throw new IllegalArgumentException("resumeId is required");
        }
        if (analysis == null) {
            throw new IllegalArgumentException("job analysis is required");
        }

        List<AiEvidenceCandidate> candidates = new ArrayList<>();
        retrieveForRequirements(resumeId, analysis.technicalSkills(), candidates);
        retrieveForRequirements(resumeId, analysis.responsibilities(), candidates);
        return candidates;
    }

    private void retrieveForRequirements(
            String resumeId,
            List<String> requirements,
            List<AiEvidenceCandidate> candidates) {
        for (String requirement : requirements) {
            float[] embedding = embeddingService.createEmbedding(requirement);
            List<Map<String, Object>> chunks = documentChunkRepository.findSimilar(
                    embedding,
                    resumeId,
                    matchingProperties.getSemanticTopK());
            for (Map<String, Object> chunk : chunks) {
                candidates.add(toCandidate(requirement, chunk));
            }
        }
    }

    private AiEvidenceCandidate toCandidate(String requirement, Map<String, Object> chunk) {
        Object chunkText = chunk.get("chunk_text");
        Object distance = chunk.get("distance");
        if (!(chunkText instanceof String text) || !(distance instanceof Number number)) {
            throw new IllegalArgumentException("Retrieved chunk must contain chunk_text and distance");
        }
        double similarity = Math.max(0.0, Math.min(1.0, 1.0 - number.doubleValue()));
        return new AiEvidenceCandidate(
                requirement,
                text,
                chunk.get("section") instanceof String section ? section : null,
                similarity);
    }
}