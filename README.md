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
* Container runtime (e.g. Podman or Docker) for running the OpenTelemetry and PostgreSQL Dev Services
* The application expects you to have the following API key as an environment variable:
- `OPENAI_API_KEY`: OpenAI API key

## Running the examples

Run `Application.java` in your IDE or use the following command:

```bash
mvn spring-boot:run
```

Under the hood, the Arconia framework will automatically spin up a PostgreSQL database server and a Grafana LGTM observability platform using Testcontainers.

## Observability

The application logs will show you the URL where you can access the Grafana observability platform and information about logs, metrics, and traces being exported to the platform.

```logs
...o.t.grafana.LgtmStackContainer           : Access to the Grafana dashboard: http://localhost:38125
```

By default, logs, metrics, and traces are exported via OTLP using the HTTP/Protobuf format.

In Grafana, you can query the traces from the "Explore" page, selecting the "Tempo" data source.
You can also explore metrics in "Explore > Metrics" and logs in "Explore > Logs".

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

