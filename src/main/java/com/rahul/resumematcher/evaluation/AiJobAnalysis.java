package com.rahul.resumematcher.evaluation;

import java.util.List;

public record AiJobAnalysis(
        List<String> technicalSkills,
        List<String> responsibilities,
        List<String> experienceRequirements,
        List<String> educationRequirements,
        List<String> certificationRequirements) {
}