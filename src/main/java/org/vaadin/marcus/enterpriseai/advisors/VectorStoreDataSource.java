package org.vaadin.marcus.enterpriseai.advisors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

public class VectorStoreDataSource implements DataSource {
    private final VectorStore vectorStore;
    private final SearchRequest baseSearchRequest;

    public VectorStoreDataSource(VectorStore vectorStore) {
        this(vectorStore, SearchRequest.defaults());
    }

    public VectorStoreDataSource(VectorStore vectorStore, SearchRequest baseSearchRequest) {
        this.vectorStore = vectorStore;
        this.baseSearchRequest = baseSearchRequest;
    }

    @Override
    public Flux<Document> search(String query, String filterExpression) {
        SearchRequest searchRequest = SearchRequest.from(baseSearchRequest)
            .withQuery(query);

        if (StringUtils.hasText(filterExpression)) {
            searchRequest = searchRequest.withFilterExpression(
                new FilterExpressionTextParser().parse(filterExpression)
            );
        }

        return Flux.fromIterable(vectorStore.similaritySearch(searchRequest));
    }
}