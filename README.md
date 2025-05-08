# Practical AI examples using Spring AI

## Included examples

- Basic AI Chat - Simple chat interface with Spring AI
- Document Analysis - Upload and analyze documents for summarization and action points
- Sentiment Analysis - Analyze customer reviews with sentiment detection and response drafting
- Image Data Extraction - Extract structured data from images
- RAG Chat - Advanced Retrieval Augmented Generation with:
  - Multi-source document retrieval
  - Context-aware query rewriting
  - Chat memory for conversational context
- Function Calling - Demonstrate AI function calling capabilities with product data

## Pre-Requisites

* Java 21+
* Container runtime (e.g. Podman or Docker) for running the PostgreSQL Dev Service
* The application expects you to have the following API key as an environment variable:
- `OPENAI_API_KEY`: OpenAI API key

## Running the examples

Run `Application.java` in your IDE or use the following command:

```bash
mvn spring-boot:run
```

## Using local models

You can use local models with Ollama or LM Studio by updating the configuration in the `application.properties` file. Uncomment and modify the following settings:

```properties
# Local Open AI compatible (LM Studio/ollama). Be sure to change the port and model names as needed.
#spring.ai.openai.base-url=http://localhost:1234
#spring.ai.openai.api-key=not-needed
#spring.ai.openai.chat.options.model=qwen2.5-7b-instruct-1m
#spring.ai.openai.embedding.base-url=http://localhost:1234
#spring.ai.openai.embedding.options.model=text-embedding-nomic-embed-text-v1.5-embedding
#spring.ai.openai.embedding.api-key=not-needed
```

