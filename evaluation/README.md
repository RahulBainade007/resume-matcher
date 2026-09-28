# AI-Powered Resume Matcher

An intelligent Resume–Job Matching system built with **Java, Spring Boot, PostgreSQL, pgvector, Ollama, Spring AI, and RAG-based vector search**.

The project is evolving from a traditional keyword + semantic matching system toward an **AI-powered, evidence-based resume screening architecture** where an LLM analyzes job requirements and evaluates them against evidence retrieved from the candidate's resume.

---

## 🚀 Project Overview

Recruiters often need to evaluate a large number of resumes against job descriptions. Traditional keyword-based systems can produce false positives because they may identify a skill without considering the actual context in which that skill appears.

This project addresses that problem using:

- Resume document parsing
- Resume chunking
- Vector embeddings
- PostgreSQL + pgvector
- Semantic similarity search
- LLM-based job requirement extraction
- Evidence retrieval from resumes
- Structured AI analysis
- Human-ranking evaluation
- Explainable matching

The long-term goal is:

> **Job Description → AI Requirement Analysis → Vector Evidence Retrieval → Evidence-Based LLM Evaluation → Explainable Match Result**

---

# 🏗️ Current Architecture

```text
                         ┌─────────────────────┐
                         │   Job Description   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   AI Job Analysis   │
                         │      Ollama LLM     │
                         └──────────┬──────────┘
                                    │
                         Requirements / Skills
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Vector Retrieval  │
                         │       pgvector      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Resume Evidence     │
                         │     Candidates      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Evidence-Based LLM  │
                         │     Evaluation      │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │ Explainable Match   │
                         │       Result        │
                         └─────────────────────┘

🧠 Technology Stack
Backend
- Java 25
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- Hibernate
- Maven
- REST APIs
AI / Machine Learning
- Ollama
- llama3.2
- nomic-embed-text
- Spring AI
- Vector embeddings
- Semantic similarity search
- Retrieval-Augmented Generation (RAG)
Database
- PostgreSQL 16
- pgvector 0.8.6
Document Processing
- Apache PDFBox
- Resume text extraction
- Resume chunking
Security
- JWT authentication
- BCrypt password hashing
- Spring Security
Testing / Evaluation
- JUnit
- Mockito
- Human-ranked resume/job benchmark
- NDCG@1
- NDCG@3
- NDCG@5
- Spearman correlation
✨ Current Features
1. Resume Upload and Processing
The system processes candidate resumes and extracts text from PDF documents.
Resume PDF
    ↓
PDFBox
    ↓
Text Extraction
    ↓
Chunking
    ↓
Embedding Generation
    ↓
PostgreSQL + pgvector

2. Vector Embeddings
Resume chunks are converted into vector embeddings using:
nomic-embed-text

The embeddings are stored in PostgreSQL using the pgvector extension.
This enables semantic retrieval rather than relying only on exact keyword matching.
3. Semantic Vector Search
The system can search resume chunks using vector similarity.
For a requirement such as:
"Experience developing REST APIs using Spring Boot"

the system can retrieve semantically relevant resume content even when the resume uses slightly different wording.
🤖 AI Job Analysis
The project includes an AI job-analysis layer using the existing Spring AI / Ollama configuration.
The LLM analyzes a job description and extracts structured requirements such as:
- Technical skills
- Responsibilities
- Experience requirements
- Education
- Certifications
The output is represented as structured data instead of relying on free-form LLM responses.
Example:
{
  "technicalSkills": [
    "Java",
    "Spring Boot",
    "REST APIs",
    "PostgreSQL"
  ],
  "responsibilities": [
    "Develop REST APIs",
    "Implement backend services"
  ],
  "experienceRequirements": [
    "Experience with Java development"
  ],
  "educationRequirements": [
    "Bachelor's degree in Computer Science"
  ],
  "certifications": []
}

The AI response is validated for:
- Missing fields
- Null values
- Blank values
- Invalid JSON
- Unexpected structures
🔎 AI Evidence Retrieval
The next stage of the architecture retrieves evidence from the candidate's resume.
For every job requirement:
Job Requirement
      ↓
Embedding
      ↓
pgvector Search
      ↓
Relevant Resume Chunks
      ↓
Evidence Candidates

The retrieval service:
- Generates embeddings for requirements
- Searches PostgreSQL/pgvector
- Keeps retrieval scoped to the correct resume
- Retrieves relevant resume chunks
- Maps document sections
- Converts vector distance into similarity
- Returns structured evidence candidates
Example:
Requirement:
"Spring Boot REST API development"

Retrieved Resume Evidence:
"Developed REST APIs using Spring Boot and Spring Security..."

Similarity:
0.82

Section:
Projects

The evidence retrieval layer does not calculate the final match score.
This separation allows retrieval and reasoning to be evaluated independently.
🧩 Evidence-Based AI Matching
Planned Architecture
The final matching architecture is being developed around evidence rather than simply asking an LLM to generate a score.
Instead of:
Keyword Score
      +
Semantic Score
      ↓
Final Score

the target architecture is:
Job Requirement
      +
Retrieved Resume Evidence
      ↓
LLM Evaluation
      ↓
Requirement-Level Result
      ↓
Deterministic Aggregation
      ↓
Final Match Result

The AI should determine whether the retrieved evidence supports the requirement.
Example:
Requirement:
Experience with Spring Boot REST APIs

Resume Evidence:
"Developed REST APIs using Spring Boot and Spring Security."

Evaluation:
SUPPORTED

Evidence:
Projects section

Confidence:
HIGH

The system is designed to avoid allowing the LLM to invent candidate qualifications that are not present in the resume.
📊 Evaluation Framework
A major part of this project is evaluating the matching system against human rankings.
The evaluation framework supports:
- Dataset loading
- Per-pair evaluation
- Human ranking comparison
- NDCG calculation
- Spearman correlation
- Absolute rank error
- Largest disagreement detection
- Per-resume diagnostics
- Benchmark reports
🧪 Human-Ranking Dataset
The current benchmark contains:
5 Job Vacancies
×
30 Resumes
=
150 Resume–Job Pairs

The human annotations provide rankings for the vacancies for each resume.
The ranking follows:
Rank 1 = Most relevant
Rank 5 = Least relevant

The same 150 pairs are used to evaluate the matching system.
📈 Baseline Evaluation
Before introducing the new evidence-based AI matching layer, the existing matcher was evaluated against the human-ranked dataset.
Baseline Results
Metric	Baseline
Evaluated pairs	150
Failed pairs	0
NDCG@1	0.4111
NDCG@3	0.5060
NDCG@5	0.7253
Spearman correlation	-0.0941


These values represent the current baseline and will be used for comparison after the AI-powered matching layer is completed.
No improvement will be claimed until the new implementation is evaluated on the same benchmark.
📐 Evaluation Metrics
NDCG@1
Measures how well the system places the most relevant job at the first position.
It focuses only on the top result.
NDCG@1

is useful for evaluating whether the system identifies the best job match immediately.
NDCG@3
Evaluates the quality of the top three results.
It gives more importance to higher-ranked positions.
NDCG@3

helps determine whether relevant vacancies are being placed near the top.
NDCG@5
Evaluates the complete five-vacancy ranking.
Because the benchmark contains five vacancies:
NDCG@5

evaluates the complete ranking list.
Spearman Correlation
Spearman correlation measures how similarly the system ranks the vacancies compared with the human ranking.
Conceptually:
Human Ranking
      ↕
System Ranking

A positive value indicates agreement in ranking direction, while a negative value indicates inverse association.
The current baseline is:
Spearman = -0.0941

This gives us a useful baseline for evaluating the new AI-powered approach.
🔬 Error Analysis
The evaluation framework also identifies large disagreements between human and system rankings.
For example:
Human Rank: 1
System Rank: 5

or:
Human Rank: 5
System Rank: 1

These cases are used to investigate potential causes such as:
- Technical skill mismatch
- Semantic retrieval problems
- Responsibility mismatch
- Experience mismatch
- Project relevance
- Education mismatch
- Certification mismatch
- Insufficient evidence
- False-positive semantic matches
The evaluation reports preserve the original dataset identifiers while using safe UUID-based database identifiers internally.
🗂️ Project Structure
resume-matcher/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rahul/resumematcher/
│   │   │
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── security/
│   │   │   └── evaluation/
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │
│   └── test/
│       └── java/
│
├── evaluation/
│   └── human-ranking/
│       ├── source/
│       │   ├── 5_vacancies.csv
│       │   ├── annotations.txt
│       │   └── CV/
│       │       ├── 1.docx
│       │       ├── 2.docx
│       │       └── ...
│       │
│       ├── human-ranking-report.json
│       └── human-ranking-report.md
│
├── pom.xml
├── Dockerfile
└── README.md

⚙️ Prerequisites
Install the following:
Java
Java 25
Verify:
java -version

Maven
Verify:
mvn -version

Docker
Verify:
docker --version

Ollama
Install Ollama and verify:
ollama --version

🐘 PostgreSQL + pgvector
The project uses PostgreSQL with the pgvector extension.
The development environment uses:
PostgreSQL 16
pgvector 0.8.6

A Docker container can be used:
docker start resume-matcher-db

Check:
docker ps

Verify PostgreSQL:
docker exec resume-matcher-db \
pg_isready -U postgres -d resume_matcher

Verify pgvector:
docker exec resume-matcher-db \
psql -U postgres -d resume_matcher \
-c "SELECT extversion FROM pg_extension WHERE extname='vector';"

🧠 Ollama Setup
Pull the embedding model:
ollama pull nomic-embed-text

Pull the LLM:
ollama pull llama3.2

Verify:
ollama list

Expected models:
nomic-embed-text
llama3.2

🔐 Configuration
Configure the database connection and other environment-specific settings in:
src/main/resources/application.properties

Do not commit database passwords, API keys, JWT secrets, or other sensitive credentials.
Use environment variables for secrets where appropriate.
▶️ Running the Application
Start PostgreSQL:
docker start resume-matcher-db

Start Ollama.
Then run:
mvn spring-boot:run

The application runs on:
http://localhost:8080

🧪 Running Tests
Run the complete test suite:
mvn clean test

Run a specific test:
mvn -Dtest=AiJobAnalysisServiceTest test

Run evidence retrieval tests:
mvn -Dtest=AiEvidenceRetrievalServiceTest test

📊 Running the Human-Ranking Benchmark
The benchmark uses the evaluation source directory:
evaluation/human-ranking/source

The source contains:
5_vacancies.csv
CV/
    1.docx
    2.docx
    ...
    30.docx

The benchmark evaluates:
30 resumes × 5 vacancies = 150 pairs

The benchmark is explicitly enabled using:
mvn -Dtest=HumanRankingEvaluationRunnerTest \
-DhumanRanking.run=true \
"-DhumanRanking.sourceRoot=E:\c\resume-matcher\evaluation\human-ranking\source" \
test

The generated reports contain the aggregate evaluation metrics and per-resume results.
🔒 Security
The application uses:
- Spring Security
- JWT authentication
- BCrypt password hashing
- Protected API endpoints
Sensitive information should never be committed to Git.
Before pushing to GitHub, verify:
git diff
git status

🐳 Docker
The project contains a Dockerfile for containerized deployment.
The intended deployment architecture is:
                  ┌───────────────┐
                  │    Client     │
                  └───────┬───────┘
                          │
                          ▼
                  ┌───────────────┐
                  │ Spring Boot   │
                  │ Application   │
                  └───────┬───────┘
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
       ┌─────────────┐        ┌─────────────┐
       │ PostgreSQL  │        │   Ollama    │
       │ + pgvector  │        │    LLM      │
       └─────────────┘        └─────────────┘

🔄 Development Roadmap
Phase 1 — AI Job Analysis
Status: Completed
- [x] Integrate existing Spring AI / Ollama configuration
- [x] Extract structured job requirements
- [x] Validate LLM output
- [x] Add unit tests
Phase 2 — Vector Evidence Retrieval
Status: Completed
- [x] Generate requirement embeddings
- [x] Search pgvector
- [x] Scope retrieval to the correct resume
- [x] Retrieve resume chunks
- [x] Map document sections
- [x] Convert vector distance to similarity
- [x] Add unit tests
Phase 3 — Evidence-Based LLM Matching
Status: In Progress
- [ ] Send requirements + retrieved evidence to LLM
- [ ] Evaluate requirement-level evidence
- [ ] Return structured results
- [ ] Prevent unsupported claims
- [ ] Add confidence/evidence fields
- [ ] Add deterministic validation
Phase 4 — Requirement-Level Scoring
Status: Planned
- [ ] Technical skills evaluation
- [ ] Responsibility evaluation
- [ ] Experience evaluation
- [ ] Education evaluation
- [ ] Certification evaluation
- [ ] Deterministic aggregation
- [ ] Explainable final result
Phase 5 — Production Integration
Status: Planned
- [ ] Integrate AI evidence pipeline with production matching
- [ ] Preserve existing authentication
- [ ] Preserve existing resume processing
- [ ] Preserve PostgreSQL/pgvector storage
- [ ] Maintain backward compatibility
Phase 6 — AI Benchmark
Status: Planned
Run the same:
30 resumes × 5 vacancies = 150 pairs

and calculate:
NDCG@1
NDCG@3
NDCG@5
Spearman correlation

Then compare against the baseline:
Baseline
   vs.
AI Evidence-Based Matcher

Phase 7 — Error Analysis
Status: Planned
Analyze:
- False positives
- False negatives
- Ranking disagreements
- Retrieval failures
- Unsupported AI decisions
- Missing evidence
- Semantic mismatches
Phase 8 — Final Release
Status: Planned
- [ ] Full regression tests
- [ ] Security review
- [ ] Remove unnecessary files
- [ ] Update documentation
- [ ] Update benchmark results
- [ ] Clean Git history
- [ ] Push final version to GitHub
🎯 Design Principles
1. Evidence over assumptions
The AI should base its evaluation on retrieved resume evidence.
No evidence
    ≠
Candidate has the skill

2. Retrieval and reasoning are separate
The system separates:
Retrieval

from:
LLM reasoning

This makes the architecture easier to test and debug.
3. Structured AI output
LLM responses are converted into structured objects rather than relying on arbitrary natural-language responses.
4. Deterministic final aggregation
The final scoring layer is intended to use deterministic logic rather than allowing the LLM to arbitrarily choose a final score.
5. Reproducible evaluation
The same human-ranked dataset is used to compare different versions of the matching system.
📈 Current Performance Baseline
The current non-AI baseline produced:
150 evaluated pairs
0 failed pairs

NDCG@1  = 0.4111
NDCG@3  = 0.5060
NDCG@5  = 0.7253

Spearman = -0.0941

These numbers are preserved as the baseline for future experiments.
The AI-powered system will be evaluated using the same 150 resume–job pairs before any performance improvement is claimed.
🛠️ Future Improvements
Potential future improvements include:
- Better requirement decomposition
- Improved semantic retrieval
- Hybrid retrieval
- Requirement-level reranking
- Evidence confidence estimation
- Explainable matching reports
- Recruiter dashboard
- Resume analytics
- Candidate comparison
- Job recommendation
- Dockerized deployment
- AWS deployment
- Production monitoring
- Evaluation dataset expansion
👨‍💻 Author
Rahul Bainade
B.Sc. (Hons.) Computer Science
MGM University, Chhatrapati Sambhajinagar
Areas of Interest
- Java Development
- Spring Boot
- Artificial Intelligence
- RAG
- Vector Databases
- Cybersecurity
- Backend Systems
- Machine Learning
📜 License
This project is intended for educational, research, and portfolio purposes.
Add an appropriate open-source license if you decide to distribute the project under a specific license.