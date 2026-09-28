package com.rahul.resumematcher.service;

public class ResumeSection {

    private final ResumeSectionType sectionType;
    private final String sectionTitle;
    private final String content;

    public ResumeSection(ResumeSectionType sectionType, String sectionTitle, String content) {
        this.sectionType = sectionType;
        this.sectionTitle = sectionTitle;
        this.content = content;
    }

    public ResumeSectionType getSectionType() {
        return sectionType;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public String getContent() {
        return content;
    }
}