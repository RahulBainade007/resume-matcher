# AI-Powered Resume Matcher

An intelligent **Resume–Job Matching System** built with **Java, Spring Boot, PostgreSQL, pgvector, Ollama, Spring AI, and semantic search**.

The project combines traditional keyword matching, vector-based semantic similarity, and an evaluation framework designed to measure how closely automated resume–job rankings agree with human relevance judgments.

> **Current development direction:** AI-Powered Evidence-Based Resume–Job Matching using **LLM + Vector Search**.

---

## 🚀 Project Overview

The Resume Matcher analyzes a candidate's resume against a Job Description (JD) and determines how well the candidate matches the requirements.

The system currently supports:

- Resume upload and PDF text extraction
- Job description processing
- Keyword-based matching
- Semantic/vector similarity search
- PostgreSQL + pgvector storage
- Ollama embeddings
- JWT authentication
- Resume–job match analysis
- Evaluation datasets and benchmarking
- Human-ranking evaluation
- NDCG and Spearman correlation metrics
- AI-based requirement analysis
- AI evidence retrieval using vector search

The long-term goal is to move from a simple score-based matcher toward an **evidence-based AI matching system** where every matching decision can be supported by evidence extracted from the resume.

---

# 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │      Frontend        │
                         │   Resume Matcher UI   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │    Spring Boot API   │
                         └──────────┬───────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
      ┌──────────────┐      ┌───────────────┐     ┌───────────────┐
      │ JWT Security │      │ Resume Parser  │     │ Match Engine  │
      └──────────────┘      │   PDFBox      │     │               │
                            └───────┬───────┘     └───────┬───────┘
                                    │                     │
                                    ▼                     ▼
                            ┌───────────────┐     ┌───────────────┐
                            │ Text Chunks   │     │ Hybrid Match  │
                            └───────┬───────┘     └───────────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ PostgreSQL + pgvector│
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ Ollama Embeddings    │
                         │  nomic-embed-text    │
                         └──────────────────────┘


              AI Evaluation / Evidence Pipeline
              
                         ┌──────────────────────┐
                         │ Job Description      │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ LLM Requirement      │
                         │ Analysis             │
                         └──────────┬───────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
             Technical Skills                 Responsibilities
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                         ┌──────────────────────┐
                         │ Vector Embeddings    │
                         └──────────┬───────────┘
                                    ▼
                         ┌──────────────────────┐
                         │ Resume Evidence      │
                         │ Retrieval             │
                         └──────────┬───────────┘
                                    ▼
                         ┌──────────────────────┐
                         │ LLM Evidence-Based   │
                         │ Matching              │
                         └──────────────────────┘

🛠️ Technology Stack
Backend
- Java 25
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- Hibernate
- REST APIs
- Maven
AI / Machine Learning
- Ollama
- nomic-embed-text
- llama3.2
- Spring AI
- Vector embeddings
- Semantic similarity
- LLM-based requirement analysis
- Evidence-based matching
Database
- PostgreSQL 16
- pgvector 0.8.6
Document Processing
- Apache PDFBox
- Resume text extraction
- Resume chunking
Security
- JWT Authentication
- BCrypt password hashing
- Spring Security
Testing / Evaluation
- JUnit
- Mockito
- NDCG@1
- NDCG@3
- NDCG@5
- Spearman correlation
- Human ranking evaluation
- Automated evaluation reports
✨ Current Features
1. Resume Processing
The system accepts resumes and extracts their textual content.
The extracted content can be divided into smaller chunks for semantic retrieval.
Example:
Resume
   │
   ├── Education
   ├── Skills
   ├── Experience
   ├── Projects
   └── Certifications

2. Job Description Matching
The system analyzes a Job Description and identifies relevant requirements.
Examples:
Java
Spring Boot
REST API
SQL
Docker
AWS
Microservices

These requirements can then be compared against resume information.
3. Keyword Matching
The existing matcher performs rule-based keyword analysis.
It identifies technical skills and compares them against the resume.
For example:
JD:
Java
Spring Boot
Docker
PostgreSQL

Resume:
Java
Spring Boot
PostgreSQL

Matched:
Java
Spring Boot
PostgreSQL

Missing:
Docker

4. Semantic Vector Search
The system uses Ollama's:
nomic-embed-text

model to generate embeddings.
The embeddings are stored in PostgreSQL using pgvector.
Conceptually:
Resume Text
     │
     ▼
Embedding Model
     │
     ▼
Vector
     │
     ▼
PostgreSQL + pgvector

When a requirement is searched, the system performs vector similarity retrieval.
5. PostgreSQL + pgvector
Resume chunks and their embeddings are stored in PostgreSQL.
The vector database allows the system to retrieve semantically similar resume content.
This is useful when the wording differs.
Example:
JD:
"Develop RESTful services"

Resume:
"Built backend APIs using Spring Boot"

A keyword-only system may miss the relationship.
Vector search can identify the semantic similarity.
6. JWT Authentication
The application uses JWT-based authentication.
The security flow is:
User
 │
 ▼
Login
 │
 ▼
JWT Token
 │
 ▼
Authenticated API Requests

Passwords are protected using BCrypt.
7. Evaluation Framework
A dedicated evaluation framework has been added to measure the quality of the matching system.
The evaluation framework supports:
- Real evaluation datasets
- Per-pair evaluation
- Keyword scores
- Semantic scores
- System scores
- Absolute errors
- Ranking evaluation
- NDCG
- Spearman correlation
- Disagreement analysis
📊 Human Ranking Benchmark
The project includes a human-ranking benchmark based on:
5 Job Vacancies
×
30 Resumes
=
150 Resume–Job Pairs

The dataset contains:
5 Job Descriptions
30+ Resume Documents
Human ranking annotations

Each resume is evaluated against the five vacancies.
Example:
Resume: CV-001

Human ranking:
[2, 1, 4, 3, 5]

This means:
Rank 1 → Vacancy 2
Rank 2 → Vacancy 1
Rank 3 → Vacancy 4
Rank 4 → Vacancy 3
Rank 5 → Vacancy 5

The system produces its own ranking from the matching scores.
The two rankings can then be compared.
📈 Evaluation Metrics
NDCG@1
Measures whether the system places highly relevant jobs at the very top.
NDCG@1

focuses only on the first result.
NDCG@3
Evaluates the quality of the top three results.
NDCG@3

is useful for measuring whether relevant jobs appear within the first three positions.
NDCG@5
Evaluates the complete five-job ranking.
NDCG@5

is particularly useful for this benchmark because each resume is compared against five vacancies.
Spearman Correlation
Spearman correlation measures how closely the complete system ranking agrees with the human ranking.
Interpretation:
+1  → strong agreement
 0  → little/no rank correlation
-1  → opposite ranking

The human ranking uses:
1 = Best match
5 = Worst match

The system ranking is also converted to:
1 = Best match
5 = Worst match

before correlation is calculated.
📊 Current Benchmark Result
The human-ranking benchmark has been successfully executed.
Evaluated pairs:       150
Failed pairs:            0

NDCG@1:             0.4111
NDCG@3:             0.5060
NDCG@5:             0.7253

Spearman:          -0.0941

These metrics are evaluation results, not manually assigned scores.
They are generated by comparing:
Human ranking
       VS
System ranking

for the 150 resume–job pairs.
🔎 Human Ranking vs System Ranking
Example:
Human:
[1, 2, 3, 4, 5]

System:
[5, 3, 2, 4, 1]

The evaluation framework measures how different these rankings are.
Large rank displacement can be investigated to identify possible weaknesses in the matching system.
🧪 Disagreement Analysis
The evaluation framework also identifies large ranking disagreements.
For example:
Human Rank: 1
System Rank: 5

or:
Human Rank: 5
System Rank: 1

These cases are useful for debugging the matching algorithm.
The report can identify:
- Resume ID
- Job ID
- Human rank
- System rank
- System score
- Keyword score
- Semantic score
- Rank displacement
🤖 AI-Powered Matching — Current Development
The next stage of the project is moving toward:
AI-Powered Evidence-Based Resume–Job Matching
Instead of relying only on:
Keyword Score
+
Semantic Score

the system is being extended to understand the actual requirements of a Job Description and retrieve supporting evidence from the resume.
Phase 1 — AI Job Requirement Analysis
An AI service has been added for structured Job Description analysis.
The LLM analyzes the JD and extracts categories such as:
Technical Skills
Responsibilities
Experience
Education
Certifications

The AI response is validated as structured JSON.
The current implementation uses:
Ollama
+
llama3.2
+
Spring AI
+
ChatClient

The LLM is configured through the existing Spring AI/Ollama setup.
Phase 2 — AI Evidence Retrieval
An evidence retrieval service has also been implemented.
The flow is:
JD Requirement
      │
      ▼
Embedding
      │
      ▼
Vector Search
      │
      ▼
PostgreSQL + pgvector
      │
      ▼
Relevant Resume Chunks

The retrieval service returns evidence candidates containing information such as:
Resume ID
Chunk Text
Section
Similarity

The service does not calculate a final match score.
This keeps evidence retrieval separate from final AI reasoning.
🧠 Planned Phase 3 — LLM Evidence-Based Matching
The next stage is to combine:
LLM Requirements
        +
Vector Evidence
        +
Resume Evidence

and allow the LLM to reason about the evidence.
Example:
Requirement:
Spring Boot REST API development

Resume Evidence:
"Developed REST APIs using Spring Boot for an inventory management
application."

LLM Assessment:
Supported

The important difference is that the system should provide evidence for its decision, rather than only producing a numeric score.
🎯 Planned Evidence Categories
The AI matching layer is intended to evaluate:
1. Technical Skills
2. Responsibilities
3. Experience
4. Projects
5. Education
6. Certifications

Each requirement should ideally be supported by resume evidence.
🔬 Planned AI Matching Output
A future result could look conceptually like:
{
  "match": true,
  "confidence": 0.87,
  "requirement": "Spring Boot",
  "evidence": [
    {
      "section": "Projects",
      "text": "Developed REST APIs using Spring Boot..."
    }
  ],
  "reason": "The resume contains direct project evidence of Spring Boot API development."
}

The exact implementation will be validated before being integrated into production scoring.
🧪 Evaluation Philosophy
The project intentionally separates:
Production Matching
        │
        └── Existing application logic


Evaluation
        │
        ├── Human rankings
        ├── NDCG
        ├── Spearman
        └── Error analysis


AI Evidence Layer
        │
        ├── LLM requirement analysis
        ├── Vector evidence retrieval
        └── Evidence-based reasoning

This prevents experimental evaluation code from accidentally changing the existing production matcher.
📁 Evaluation Dataset Structure
The human-ranking benchmark follows this structure:
evaluation/
└── human-ranking/
    ├── source/
    │   ├── 5_vacancies.csv
    │   ├── annotations.txt
    │   └── CV/
    │       ├── 1.docx
    │       ├── 2.docx
    │       ├── ...
    │       └── 30.docx
    │
    ├── human-ranking-report.json
    └── human-ranking-report.md

The source dataset contains the vacancy descriptions and resume documents used for the benchmark.
🗄️ Database
The project uses PostgreSQL with pgvector.
Example database architecture:
PostgreSQL
│
├── users
├── document_chunks
├── resume_analyses
├── analysis_matched_keywords
└── analysis_missing_keywords

The document_chunks table stores resume chunks and vector embeddings.
🐳 Docker
PostgreSQL + pgvector can be run using Docker.
Example:
docker start resume-matcher-db

Check the container:
docker ps

Check PostgreSQL:
docker exec resume-matcher-db \
pg_isready -U postgres -d resume_matcher

Check pgvector:
docker exec resume-matcher-db \
psql -U postgres -d resume_matcher \
-c "SELECT extversion FROM pg_extension WHERE extname='vector';"

🦙 Ollama Setup
Install Ollama and pull the required models.
Embedding model:
ollama pull nomic-embed-text

LLM:
ollama pull llama3.2

Verify:
ollama list

Expected models:
nomic-embed-text
llama3.2

▶️ Running the Application
1. Start PostgreSQL
docker start resume-matcher-db

Verify:
docker ps

2. Start Ollama
Make sure Ollama is running.
Verify:
ollama list

3. Start Spring Boot
From the project root:
mvn spring-boot:run

The application runs on:
http://localhost:8080

🧪 Running Tests
Run all tests:
mvn clean test

Run the AI analysis tests:
mvn -Dtest=AiJobAnalysisServiceTest test

Run the AI evidence retrieval tests:
mvn -Dtest=AiEvidenceRetrievalServiceTest test

Run evaluation tests:
mvn -Dtest=EvaluationDatasetLoaderTest,EvaluationMetricsTest,EvaluationRunnerTest test

📊 Running Human Ranking Evaluation
The benchmark requires the source dataset.
Run:
mvn -Dtest=HumanRankingEvaluationRunnerTest \
-DhumanRanking.run=true \
"-DhumanRanking.sourceRoot=E:\c\resume-matcher\evaluation\human-ranking\source" \
test

The benchmark produces:
human-ranking-report.json
human-ranking-report.md

The benchmark currently evaluates:
5 vacancies
×
30 resumes
=
150 pairs

🔐 Security
The application uses:
- Spring Security
- JWT authentication
- BCrypt password hashing
- Protected API endpoints
Sensitive credentials should never be committed to GitHub.
Use environment variables or external configuration for secrets.
🧪 Testing Strategy
The project uses multiple testing levels.
Unit Tests
Used for:
- AI JSON parsing
- Validation
- Evidence mapping
- Distance conversion
- Evaluation metrics
Integration/Evaluation Tests
Used for:
- PostgreSQL
- pgvector
- Ollama
- Resume processing
- Human ranking evaluation
📌 Current Project Status
Component	Status
Spring Boot backend	✅
JWT authentication	✅
Resume processing	✅
PDF extraction	✅
Keyword matching	✅
PostgreSQL	✅
pgvector	✅
Ollama embeddings	✅
Semantic search	✅
Evaluation framework	✅
Human ranking benchmark	✅
150-pair evaluation	✅
NDCG evaluation	✅
Spearman evaluation	✅
AI JD analysis	✅
AI evidence retrieval	✅
LLM evidence-based final scoring	🔄 In progress
Full AI replacement of hybrid scoring	🔄 Planned
Explainable evidence-based results	🔄 In progress


🗺️ Roadmap
Phase 1 — Foundation
- [x] Resume processing
- [x] Job description processing
- [x] Keyword matching
- [x] PostgreSQL
- [x] pgvector
- [x] JWT authentication
Phase 2 — Semantic Matching
- [x] Ollama integration
- [x] nomic-embed-text
- [x] Resume embeddings
- [x] Vector similarity search
- [x] Semantic matching
Phase 3 — Evaluation
- [x] Evaluation framework
- [x] Human ranking dataset
- [x] 5 vacancies
- [x] 30 resumes
- [x] 150 resume–job pairs
- [x] NDCG@1
- [x] NDCG@3
- [x] NDCG@5
- [x] Spearman correlation
- [x] Ranking disagreement analysis
Phase 4 — AI Matching
- [x] LLM Job Description analysis
- [x] Structured requirement extraction
- [x] Vector evidence retrieval
- [x] Evidence candidate generation
- [ ] LLM evidence reasoning
- [ ] Evidence-based final match score
- [ ] Explainable match results
- [ ] AI-based evaluation against human rankings
Phase 5 — Production
- [ ] Recruiter dashboard
- [ ] Resume analytics
- [ ] Match explanation UI
- [ ] Docker deployment
- [ ] AWS deployment
- [ ] Production monitoring
📈 Future Evaluation
The final AI-powered system will be evaluated against the same human-ranked benchmark.
The comparison will be:
Human Ranking
      │
      ▼
Ground Truth
      │
      ├───────────────┐
      ▼               ▼
Current Matcher    AI Matcher
      │               │
      ▼               ▼
System Ranking    AI Ranking
      │               │
      └───────┬───────┘
              ▼
        Evaluation
              │
       ┌──────┴──────┐
       ▼             ▼
     NDCG         Spearman

This will allow the project to measure whether the AI/evidence-based approach produces rankings that better align with the human relevance judgments.
🎯 Key Engineering Goals
The project is designed around five major principles:
1. Semantic Understanding
Understand meaning rather than relying only on exact keywords.
2. Evidence-Based Decisions
Every important matching decision should ideally be supported by resume evidence.
3. Explainability
The system should explain why a candidate matches a requirement.
4. Measurable Evaluation
Use human-ranked data and ranking metrics rather than relying only on subjective examples.
5. Separation of Concerns
Keep:
Production Matching
Evaluation
AI Experimentation

separated so experimental changes do not accidentally affect the existing application.
📚 Project Highlights
- Built a Java/Spring Boot Resume–Job Matching platform.
- Implemented PostgreSQL + pgvector semantic search.
- Integrated Ollama embeddings using nomic-embed-text.
- Implemented JWT-based authentication and secure API access.
- Built a human-ranking evaluation pipeline covering 150 resume–job pairs.
- Evaluated ranking quality using NDCG@1, NDCG@3, NDCG@5, and Spearman correlation.
- Added LLM-based Job Description requirement extraction using Spring AI and Ollama.
- Implemented vector-based resume evidence retrieval.
- Moving toward explainable LLM + Vector Search evidence-based matching.
👨‍💻 Author
Rahul Bainade
B.Sc. (Hons.) Computer Science
MGM University, Chhatrapati Sambhajinagar
Areas of interest:
- Java Development
- Spring Boot
- Artificial Intelligence
- Machine Learning
- Cybersecurity
- Backend Engineering
- Vector Search
- LLM Applications
📄 License
This project is intended for educational, research, and portfolio purposes.
