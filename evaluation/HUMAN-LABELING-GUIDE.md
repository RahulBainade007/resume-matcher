# Human Labeling Guide

This directory contains a template for building a real, human-labeled resume/job-description evaluation dataset. The template is intentionally unlabeled. It is not evaluation evidence and must not be presented as matcher accuracy data.

## Dataset Schema

Each JSON array item represents one unique resume/job evaluation pair:

```json
[
  {
    "pairId": "JOB-001-RESUME-001",
    "jobId": "JOB-001",
    "resumeId": "RESUME-001",
    "jobDescription": "PASTE THE JOB DESCRIPTION HERE",
    "resumeText": "PASTE THE RESUME TEXT HERE",
    "humanRelevanceScore": null
  }
]
```

- `pairId`: unique identifier for one resume-job evaluation pair.
- `jobId`: identifier for the job, shared by every resume evaluated against that job.
- `resumeId`: unique identifier for the resume. The same resume may appear in pairs for multiple jobs.
- `jobDescription`: the complete job description used for the evaluation.
- `resumeText`: the resume text evaluated against that job.
- `humanRelevanceScore`: an independent human label from 0 to 5.

The production `EvaluationDatasetLoader` expects a completed scored dataset with an integer `humanRelevanceScore`. Do not load this template until every placeholder has been replaced and every score has been assigned. The template uses `null` deliberately and is a separate labeling format; the scored loader must not be weakened to accept unlabeled records.

## Relevance Scale

Use exactly this scale:

- `0 = Completely irrelevant`
- `1 = Very weak match`
- `2 = Weak / partial match`
- `3 = Moderate match`
- `4 = Strong match`
- `5 = Very strong match`

The label should reflect the overall relevance of the resume to the specific job description. Consider the complete evidence available in the resume:

- technical skills
- responsibilities
- relevant experience
- relevant projects
- education requirements
- certifications when explicitly relevant
- overall evidence in the resume

Do not try to reproduce the application's predicted score. The human label is ground truth for evaluation.

Do not use any application output as the label. In particular, do not use:

- `keywordScore`
- `semanticScore`
- the existing hybrid score
- any diagnostic component score

Labels must be assigned independently by a human reviewer.

## Recommended Initial Dataset

Start with approximately:

- 10 different Java-related job descriptions
- 10 to 20 resumes per job
- approximately 100 to 200 unique resume/job pairs

Include a range of Java roles, such as:

- Junior Java Developer
- Java Developer
- Java + Spring Boot Developer
- Full Stack Java Developer
- Backend Java Developer
- Software Engineer - Java

Use real, permissioned job descriptions and resumes. Do not fabricate job descriptions, resumes, or human labels for the real dataset. The template demonstrates structure only and contains no actual evaluation content.

## Pair Identity and Ranking Groups

A resume can be evaluated against more than one job. Therefore, a `resumeId` may appear in multiple pairs, while each pair still needs its own unique `pairId`.

Example structure, with no relevance scores assigned:

```text
JOB-001 + RESUME-001 -> pair-001
JOB-001 + RESUME-002 -> pair-002
JOB-001 + RESUME-003 -> pair-003
JOB-002 + RESUME-001 -> pair-004
```

`jobId` identifies the ranking group. `pairId` identifies the unique job-resume comparison. `resumeId` identifies the resume independently of the job. NDCG and other grouped ranking analysis use `jobId`, not the full job-description text and not a derived ID convention.

## Synthetic Data

`evaluation-data/synthetic-test-data.json` is synthetic test data for exercising the evaluation machinery. Keep it separate from the real human-labeled dataset. Its labels are not human-reviewed and must not be used as an accuracy claim.

## Workflow

1. Copy the structure from `real-evaluation-template.json`.
2. Add one record per resume/job pair.
3. Assign globally unique `pairId` values.
4. Reuse `jobId` for all resumes evaluated against the same job.
5. Reuse a `resumeId` only when the same resume is evaluated against another job.
6. Paste the complete job description and resume text.
7. Have an independent human reviewer assign one score from 0 through 5.
8. Review labels for consistency before evaluation.
9. Save the completed dataset separately from the unlabeled template.
10. Load the completed dataset through the existing evaluation loader.

The current production baseline remains `keywordScore * 0.50 + semanticScore * 0.50`. Preparing human labels does not change production scoring, tune weights, or create a new final score.
