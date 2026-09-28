package com.rahul.resumematcher.service;

public class EvidenceScoreResult {

    private final double technicalSkillScore;
    private final double responsibilityScore;
    private final double experienceEvidenceScore;
    private final double projectEvidenceScore;
    private final double educationEvidenceScore;
    private final double certificationEvidenceScore;
    private final Double semanticScore;
    private final int totalRequiredSkills;
    private final int matchedSkills;
    private final int totalResponsibilities;
    private final int matchedResponsibilities;
    private final int experienceSupportedResponsibilities;
    private final int projectSupportedResponsibilities;
    private final boolean hasEducationRequirement;
    private final boolean hasCertificationRequirement;

    public EvidenceScoreResult(
            double technicalSkillScore,
            double responsibilityScore,
            double experienceEvidenceScore,
            double projectEvidenceScore,
            double educationEvidenceScore,
            double certificationEvidenceScore,
            Double semanticScore,
            int totalRequiredSkills,
            int matchedSkills,
            int totalResponsibilities,
            int matchedResponsibilities,
            int experienceSupportedResponsibilities,
            int projectSupportedResponsibilities,
            boolean hasEducationRequirement,
            boolean hasCertificationRequirement) {
        this.technicalSkillScore = technicalSkillScore;
        this.responsibilityScore = responsibilityScore;
        this.experienceEvidenceScore = experienceEvidenceScore;
        this.projectEvidenceScore = projectEvidenceScore;
        this.educationEvidenceScore = educationEvidenceScore;
        this.certificationEvidenceScore = certificationEvidenceScore;
        this.semanticScore = semanticScore;
        this.totalRequiredSkills = totalRequiredSkills;
        this.matchedSkills = matchedSkills;
        this.totalResponsibilities = totalResponsibilities;
        this.matchedResponsibilities = matchedResponsibilities;
        this.experienceSupportedResponsibilities = experienceSupportedResponsibilities;
        this.projectSupportedResponsibilities = projectSupportedResponsibilities;
        this.hasEducationRequirement = hasEducationRequirement;
        this.hasCertificationRequirement = hasCertificationRequirement;
    }

    public double getTechnicalSkillScore() { return technicalSkillScore; }
    public double getResponsibilityScore() { return responsibilityScore; }
    public double getExperienceEvidenceScore() { return experienceEvidenceScore; }
    public double getProjectEvidenceScore() { return projectEvidenceScore; }
    public double getEducationEvidenceScore() { return educationEvidenceScore; }
    public double getCertificationEvidenceScore() { return certificationEvidenceScore; }
    public Double getSemanticScore() { return semanticScore; }
    public int getTotalRequiredSkills() { return totalRequiredSkills; }
    public int getMatchedSkills() { return matchedSkills; }
    public int getTotalResponsibilities() { return totalResponsibilities; }
    public int getMatchedResponsibilities() { return matchedResponsibilities; }
    public int getExperienceSupportedResponsibilities() { return experienceSupportedResponsibilities; }
    public int getProjectSupportedResponsibilities() { return projectSupportedResponsibilities; }
    public boolean hasEducationRequirement() { return hasEducationRequirement; }
    public boolean hasCertificationRequirement() { return hasCertificationRequirement; }
}