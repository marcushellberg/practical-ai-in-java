package org.vaadin.marcus.enterpriseai.advisors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An advisor that rewrites user questions into standalone questions better suited for vector search.
 * This advisor modifies the outbound request to improve vector search relevance by removing
 * conversational dependencies.
 */
public class StandaloneQuestionAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private final ChatClient chatClient;
    private final int order;

    public StandaloneQuestionAdvisor(ChatClient chatClient) {
        // Set order between MessageChatMemoryAdvisor and QuestionAnswerAdvisor
        this(chatClient, Ordered.LOWEST_PRECEDENCE - 2000);
    }

    public StandaloneQuestionAdvisor(ChatClient chatClient, int order) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        this.chatClient = chatClient;
        this.order = order;
    }

    @Override
    public String getName() {
        return "standalone-question-advisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // If there's only one message (the current one), no need to rewrite
        if (!isRewriteNeeded(advisedRequest.messages())) {
            return chain.nextAroundCall(advisedRequest);
        }
        System.out.println("Original question: " + advisedRequest.userText());
        String standaloneQuestion = generateStandaloneQuestion(advisedRequest);
        System.out.println("Standalone question: " + standaloneQuestion);
        AdvisedRequest modifiedRequest = createModifiedRequest(advisedRequest, standaloneQuestion);
        return chain.nextAroundCall(modifiedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // If there's only one message (the current one), no need to rewrite
        if (!isRewriteNeeded(advisedRequest.messages())) {
            return chain.nextAroundStream(advisedRequest);
        }

        return Mono.fromCallable(() -> generateStandaloneQuestion(advisedRequest))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(standaloneQuestion ->
                chain.nextAroundStream(createModifiedRequest(advisedRequest, standaloneQuestion)));
    }

    private boolean isRewriteNeeded(List<Message> messages) {
        if (messages == null) {
            return false;
        }

        long conversationalMessages = messages.stream()
            .filter(msg -> msg.getMessageType() == MessageType.USER
                || msg.getMessageType() == MessageType.ASSISTANT)
            .count();

        return conversationalMessages > 1;
    }

    private String generateStandaloneQuestion(AdvisedRequest advisedRequest) {
        String chatHistory = formatChatHistory(advisedRequest.messages());

        String promptTemplate = """
            Given the following conversation and a follow up question, rephrase the follow-up question to be a standalone question.
            
            Chat History:
            {chat_history}
            
            Follow Up Input: {question}
            
            Standalone Question:""";

        return this.chatClient
            .prompt()
            .system("You are a helpful assistant that rephrases questions to be standalone, removing dependencies on chat history.")
            .user(u -> u.text(promptTemplate)
                .param("chat_history", chatHistory)
                .param("question", advisedRequest.userText())
            )
            .call()
            .content();
    }

    private AdvisedRequest createModifiedRequest(AdvisedRequest original, String standaloneQuestion) {
        Map<String, Object> params = new HashMap<>(original.userParams());
        params.put("original_question", original.userText());
        params.put("standalone_question", standaloneQuestion);

        return AdvisedRequest.from(original)
            .withUserText(standaloneQuestion)
            .withUserParams(params)
            .withAdviseContext(original.adviseContext())
            .build();
    }

    private String formatChatHistory(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        return messages.stream()
            .filter(msg -> msg.getMessageType() == MessageType.USER
                || msg.getMessageType() == MessageType.ASSISTANT)
            .map(msg -> String.format("%s: %s",
                msg.getMessageType().toString(),
                msg.getMessageType() == MessageType.USER ? msg.getContent() :
                    "\"" + msg.getContent() + "\""))
            .collect(Collectors.joining("\n"));
    }
}