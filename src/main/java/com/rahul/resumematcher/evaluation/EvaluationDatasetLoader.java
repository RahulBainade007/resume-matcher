package com.rahul.resumematcher.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class EvaluationDatasetLoader {

    private final ObjectMapper objectMapper;

    public EvaluationDatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EvaluationExample> load(Path path) throws IOException {
        try (InputStream input = Files.newInputStream(path)) {
            try {
                return validate(objectMapper.readValue(input, new TypeReference<>() { }));
            } catch (ValueInstantiationException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof IllegalArgumentException illegalArgumentException) {
                    throw illegalArgumentException;
                }
                throw exception;
            }
        }
    }

    private List<EvaluationExample> validate(List<EvaluationExample> examples) {
        if (examples == null) {
            throw new IllegalArgumentException("Evaluation dataset must be a JSON array");
        }
        Set<String> pairIds = new HashSet<>();
        for (EvaluationExample example : examples) {
            if (!pairIds.add(example.pairId())) {
                throw new IllegalArgumentException("Duplicate pairId: " + example.pairId());
            }
        }
        return List.copyOf(examples);
    }
}