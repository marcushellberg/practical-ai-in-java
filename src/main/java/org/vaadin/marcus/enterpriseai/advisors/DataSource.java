package org.vaadin.marcus.enterpriseai.advisors;

import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

/**
 * Interface for data sources that can provide documents for RAG.
 */
public interface DataSource {
    /**
     * Searches for relevant documents based on a query.
     * @param query The search query
     * @param filterExpression Optional filter expression (can be null)
     * @return Flux of relevant documents
     */
    Flux<Document> search(String query, String filterExpression);
}