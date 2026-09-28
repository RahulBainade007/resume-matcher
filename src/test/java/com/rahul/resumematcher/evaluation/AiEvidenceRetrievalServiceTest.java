package com.rahul.resumematcher.evaluation;

import com.rahul.resumematcher.repository.DocumentChunkRepository;
import com.rahul.resumematcher.service.EmbeddingService;
import com.rahul.resumematcher.service.MatchingProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiEvidenceRetrievalServiceTest {

    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final DocumentChunkRepository documentChunkRepository = mock(DocumentChunkRepository.class);
    private final MatchingProperties matchingProperties = new MatchingProperties();
    private final AiEvidenceRetrievalService service = new AiEvidenceRetrievalService(
            embeddingService, documentChunkRepository, matchingProperties);

    @Test
    void embedsSkillsAndResponsibilitiesAndScopesRetrievalToResume() {
        float[] skillEmbedding = {1.0f, 2.0f};
        float[] responsibilityEmbedding = {3.0f, 4.0f};
        when(embeddingService.createEmbedding("Java")).thenReturn(skillEmbedding);
        when(embeddingService.createEmbedding("Build APIs")).thenReturn(responsibilityEmbedding);
        when(documentChunkRepository.findSimilar(any(float[].class), eq("resume-123"), eq(5)))
                .thenReturn(List.of(Map.of(
                        "chunk_text", "Built Java APIs",
                        "section", "Experience",
                        "distance", 0.2)));

        List<AiEvidenceCandidate> candidates = service.retrieve(
                "resume-123",
                new AiJobAnalysis(List.of("Java"), List.of("Build APIs"), List.of(), List.of(), List.of()));

        assertEquals(2, candidates.size());
        assertEquals("Java", candidates.get(0).requirement());
        assertEquals("Built Java APIs", candidates.get(0).chunkText());
        assertEquals("Experience", candidates.get(0).section());
        assertEquals(0.8, candidates.get(0).similarityScore());
        verify(documentChunkRepository).findSimilar(skillEmbedding, "resume-123", 5);
        verify(documentChunkRepository).findSimilar(responsibilityEmbedding, "resume-123", 5);
    }

    @Test
    void leavesSectionNullWhenRepositoryDoesNotProvideOne() {
        when(embeddingService.createEmbedding("Java")).thenReturn(new float[]{1.0f});
        when(documentChunkRepository.findSimilar(any(float[].class), eq("resume-123"), eq(5)))
                .thenReturn(List.of(Map.of("chunk_text", "Java", "distance", 1.5)));

        AiEvidenceCandidate candidate = service.retrieve(
                "resume-123",
                new AiJobAnalysis(List.of("Java"), List.of(), List.of(), List.of(), List.of())).get(0);

        assertEquals(null, candidate.section());
        assertEquals(0.0, candidate.similarityScore());
    }

    @Test
    void rejectsMissingScopeOrAnalysis() {
        AiJobAnalysis analysis = new AiJobAnalysis(List.of(), List.of(), List.of(), List.of(), List.of());
        assertThrows(IllegalArgumentException.class, () -> service.retrieve("", analysis));
        assertThrows(IllegalArgumentException.class, () -> service.retrieve("resume-123", null));
    }
}