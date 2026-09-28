package com.rahul.resumematcher.evaluation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.resumematcher.service.EmbeddingService;
import com.rahul.resumematcher.service.HybridMatchingService;

@Service
public class HumanRankingEvaluationService {

    private final HumanRankingDatasetLoader datasetLoader;
    private final EmbeddingService embeddingService;
    private final HybridMatchingService hybridMatchingService;
    private final ObjectMapper objectMapper;

    public HumanRankingEvaluationService(
            HumanRankingDatasetLoader datasetLoader,
            EmbeddingService embeddingService,
            HybridMatchingService hybridMatchingService,
            ObjectMapper objectMapper) {
        this.datasetLoader = datasetLoader;
        this.embeddingService = embeddingService;
        this.hybridMatchingService = hybridMatchingService;
        this.objectMapper = objectMapper;
    }

    public HumanRankingReport evaluate(
            Path manifestPath,
            Path sourceRoot,
            Path reportJson,
            Path reportMarkdown) throws IOException {
        HumanRankingDataset dataset = datasetLoader.load(manifestPath);
        Map<Integer, String> vacancies = loadVacancies(sourceRoot.resolve("5_vacancies.csv"));
        List<HumanRankingResumeResult> resumeResults = new ArrayList<>();
        int expectedPairs = dataset.annotatedResumes().size() * dataset.vacancies().size();
        int failedPairs = 0;

        for (HumanRankingResume resume : dataset.annotatedResumes()) {
            List<HumanRankingPairResult> pairs = new ArrayList<>();
            try {
                String resumeText = readDocx(sourceRoot.resolve(resume.sourceFile()));
                String runResumeId = createRunResumeId();
                embeddingService.processAndStoreDocument(runResumeId, resume.sourceFile(), resumeText);

                for (int index = 0; index < dataset.vacancies().size(); index++) {
                    HumanRankingVacancy vacancy = dataset.vacancies().get(index);
                    try {
                        String jobDescription = vacancies.get(vacancy.sourceRowId());
                        if (jobDescription == null) {
                            throw new IllegalStateException("Missing vacancy row " + vacancy.sourceRowId());
                        }
                        HybridMatchingService.HybridMatchResult result = hybridMatchingService.match(
                                runResumeId, resumeText, jobDescription);
                        if (result.getSemanticScore() == null) {
                            throw new IllegalStateException("Semantic score unavailable");
                        }
                        pairs.add(new HumanRankingPairResult(
                                vacancy.jobId(),
                                result.getKeywordScore(),
                                result.getSemanticScore(),
                                result.getFinalScore(),
                                resume.humanRankAnnotator1().get(index),
                                resume.humanRankAnnotator2().get(index),
                                null));
                    } catch (RuntimeException exception) {
                        failedPairs++;
                        pairs.add(HumanRankingPairResult.failure(vacancy.jobId(),
                                resume.humanRankAnnotator1().get(index),
                                resume.humanRankAnnotator2().get(index), exception.getMessage()));
                    }
                }
            } catch (Exception exception) {
                failedPairs += dataset.vacancies().size();
                for (int index = 0; index < dataset.vacancies().size(); index++) {
                    pairs.add(HumanRankingPairResult.failure(
                            dataset.vacancies().get(index).jobId(),
                            resume.humanRankAnnotator1().get(index),
                            resume.humanRankAnnotator2().get(index), exception.getMessage()));
                }
            }
            resumeResults.add(buildResumeResult(resume, pairs));
        }

        HumanRankingReport report = buildReport(dataset, expectedPairs, failedPairs, resumeResults);
        Files.createDirectories(reportJson.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportJson.toFile(), report);
        Files.writeString(reportMarkdown, toMarkdown(report));
        return report;
    }

    static String createRunResumeId() {
        return UUID.randomUUID().toString();
    }

    private Map<Integer, String> loadVacancies(Path csvPath) throws IOException {
        String csv = Files.readString(csvPath);
        List<List<String>> rows = parseCsv(csv);
        Map<Integer, String> vacancies = new LinkedHashMap<>();
        for (List<String> row : rows.subList(1, rows.size())) {
            if (row.size() >= 2) {
                vacancies.put(Integer.parseInt(row.get(0)), row.get(1));
            }
        }
        return vacancies;
    }

    private List<List<String>> parseCsv(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < text.length() && text.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                row.add(field.toString());
                field.setLength(0);
            } else if ((character == '\n' || character == '\r') && !quoted) {
                if (character == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                row.add(field.toString());
                rows.add(row);
                row = new ArrayList<>();
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        if (!field.isEmpty() || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    private String readDocx(Path path) throws Exception {
        try (InputStream input = Files.newInputStream(path); ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if ("word/document.xml".equals(entry.getName())) {
                    byte[] xmlBytes = zip.readAllBytes();
                    Document document = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new ByteArrayInputStream(xmlBytes)));
                    return document.getDocumentElement().getTextContent().replaceAll("\\s+", " ").trim();
                }
            }
        }
        throw new IOException("DOCX document.xml not found: " + path);
    }

    private HumanRankingResumeResult buildResumeResult(
            HumanRankingResume resume,
            List<HumanRankingPairResult> pairs) {
        List<HumanRankingPairResult> successful = pairs.stream()
                .filter(pair -> pair.systemScore() != null)
                .toList();
        List<String> systemRanking = successful.stream()
                .sorted(Comparator.comparingDouble(HumanRankingPairResult::systemScore).reversed())
                .map(HumanRankingPairResult::jobId)
                .toList();
        Double ndcg1 = ndcg(systemRanking, resume.humanRankAnnotator1(), 1);
        Double ndcg3 = ndcg(systemRanking, resume.humanRankAnnotator1(), 3);
        Double ndcg5 = ndcg(systemRanking, resume.humanRankAnnotator1(), 5);
        Double spearman = spearman(systemRanking, resume.humanRankAnnotator1());
        return new HumanRankingResumeResult(
                resume.resumeId(), resume.humanRankAnnotator1(), resume.humanRankAnnotator2(),
                systemRanking, ndcg1, ndcg3, ndcg5, spearman, pairs);
    }

    private HumanRankingReport buildReport(
            HumanRankingDataset dataset,
            int expectedPairs,
            int failedPairs,
            List<HumanRankingResumeResult> resumes) {
        List<HumanRankingResumeResult> evaluated = resumes.stream()
                .filter(result -> !result.systemRanking().isEmpty())
                .toList();
        return new HumanRankingReport(
                "human-ranking benchmark results",
                dataset.sourceRepository(),
                dataset.vacancies().size(),
                dataset.annotatedResumes().size(),
                expectedPairs,
                expectedPairs - failedPairs,
                failedPairs,
                2,
                0.50,
                0.50,
                "Current production baseline: keywordScore * 0.50 + semanticScore * 0.50",
                average(evaluated, HumanRankingResumeResult::ndcgAt1),
                average(evaluated, HumanRankingResumeResult::ndcgAt3),
                average(evaluated, HumanRankingResumeResult::ndcgAt5),
                average(evaluated, HumanRankingResumeResult::spearman),
                resumes,
                List.of());
    }

    private Double average(List<HumanRankingResumeResult> results,
                           java.util.function.Function<HumanRankingResumeResult, Double> value) {
        List<Double> values = results.stream().map(value).filter(item -> item != null).toList();
        return values.isEmpty() ? null : values.stream().mapToDouble(item -> item).average().orElse(0.0);
    }

    private Double ndcg(List<String> systemRanking, List<Integer> humanRanks, int k) {
        if (systemRanking.size() < 5) return null;
        double dcg = 0.0;
        double ideal = 0.0;
        for (int index = 0; index < k; index++) {
            int systemPosition = vacancyPosition(systemRanking.get(index));
            dcg += gain(humanRanks.get(systemPosition)) / discount(index);
            ideal += gain(index + 1) / discount(index);
        }
        return ideal == 0.0 ? null : dcg / ideal;
    }

    private Double spearman(List<String> systemRanking, List<Integer> humanRanks) {
        if (systemRanking.size() < 2) return null;
        double firstMean = 3.0;
        double secondMean = humanRanks.stream().mapToInt(item -> item).average().orElse(0.0);
        double numerator = 0.0;
        double firstVariance = 0.0;
        double secondVariance = 0.0;
        for (int index = 0; index < 5; index++) {
            double first = index + 1 - firstMean;
            double second = humanRanks.get(vacancyPosition(systemRanking.get(index))) - secondMean;
            numerator += first * second;
            firstVariance += first * first;
            secondVariance += second * second;
        }
        return secondVariance == 0.0 ? null : numerator / Math.sqrt(firstVariance * secondVariance);
    }

    private int vacancyPosition(String jobId) {
        return Integer.parseInt(jobId.substring(jobId.indexOf('-') + 1)) - 1;
    }

    private double gain(int rank) { return Math.pow(2.0, 5 - rank) - 1.0; }
    private double discount(int index) { return Math.log(index + 2.0) / Math.log(2.0); }

    private String toMarkdown(HumanRankingReport report) {
        return "# Human-Ranking Benchmark Results\n\n"
                + "Status: evaluated against the current production baseline.\n\n"
                + "## Dataset\n\n"
                + "- Vacancies: " + report.vacancies() + "\n"
                + "- Annotated resumes: " + report.annotatedResumes() + "\n"
                + "- Expected pairs: " + report.expectedPairs() + "\n"
                + "- Evaluated pairs: " + report.evaluatedPairs() + "\n"
                + "- Failed pairs: " + report.failedPairs() + "\n\n"
                + "## Baseline\n\n" + report.baselineDescription() + "\n\n"
                + "## Metrics\n\n"
                + "- NDCG@1: " + report.ndcgAt1() + "\n"
                + "- NDCG@3: " + report.ndcgAt3() + "\n"
                + "- NDCG@5: " + report.ndcgAt5() + "\n"
                + "- Spearman: " + report.spearman() + "\n\n"
                + "Per-resume results are stored in the JSON report. Human rankings are preserved and were not modified.\n";
    }
}