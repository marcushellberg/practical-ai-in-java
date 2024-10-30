# Practical AI examples using Spring AI

## Included examples

- Document summarization and analysis
- Sentiment analysis
- Text drafting
- Image data extraction
- Advanced RAG techniques
  - Multi-source retrieval
  - Re-ranking
  - Question rewriting

## Requirements

The application expects you to have the following two API keys as environment variables:
- `OPENAI_API_KEY`: OpenAI API key
- `COHERE_API_KEY`: Cohere API key (for document re-ranking)

## Running the examples

Run `Application.java` in your IDE or use the following command:

```bash
mvn spring-boot:run
```

## Using local models

You can use local models with Ollama by updating the spring-ai dependency in the `pom.xml` file, and defining which models to use in the `application.properties` file.

