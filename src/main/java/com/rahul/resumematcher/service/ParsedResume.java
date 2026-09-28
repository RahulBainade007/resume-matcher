package com.rahul.resumematcher.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParsedResume {

    private final List<ResumeSection> sections;

    public ParsedResume(List<ResumeSection> sections) {
        this.sections = Collections.unmodifiableList(new ArrayList<>(sections));
    }

    public List<ResumeSection> getSections() {
        return sections;
    }

    public List<ResumeSection> getSectionsByType(ResumeSectionType sectionType) {
        return sections.stream()
                .filter(section -> section.getSectionType() == sectionType)
                .collect(Collectors.toList());
    }
}