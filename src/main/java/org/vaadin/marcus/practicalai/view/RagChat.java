package org.vaadin.marcus.practicalai.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

import java.util.UUID;

@Menu(title = "RAG Chat", order = 6)
@Route("rag-chat")
public class RagChat extends VerticalLayout {

    private ChatClient chatClient;
    private final VectorStore vectorStore;
    private final String chatId = UUID.randomUUID().toString();

    public RagChat(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory) {
        this.vectorStore = vectorStore;
        chatClient = builder
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(chatMemory),
                RetrievalAugmentationAdvisor.builder()

                    ////// 1. Pre-Retrieval

                    .queryTransformers(
                        // Rewrite question into a standalone question based on chat history. Example:
                        // Original: What are the benefits?
                        // Rewritten: What are the benefits of exercising?
                        CompressionQueryTransformer.builder()
                            .chatClientBuilder(builder.build().mutate())
                            .build(),

                        // Rewrite the query to optimize for vector search. Example:
                        // Original: I've considered starting to run or maybe lift weights, are there benefits to that?
                        // Rewritten: What are the benefits of exercising?
                        RewriteQueryTransformer.builder()
                            .chatClientBuilder(builder.build().mutate())
                            .build()
                    )

                    // Create additional queries based on the original query to gather more relevant information. Example:
                    // Original: What are the benefits of exercising?
                    // New queries:
                    // - What are the benefits of exercising?
                    // - What are the benefits of cardio?
                    // - What are the benefits of strength training?
                    .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(builder.build().mutate())
                        .build())

                    ////// 2. Retrieval
                    .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())

                    ////// 3. Post-retrieval
                    .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                    .build()
            )
            .build();

        buildView();
    }

    private void buildView() {
        var messageList = new VerticalLayout();
        var messageInput = new MessageInput();
        var buffer = new MultiFileMemoryBuffer();
        var upload = new Upload(buffer);
        var popover = new Popover();
        var contextButton = new Button("Add Context");
        var inputLayout = new HorizontalLayout();

        popover.setTarget(contextButton);
        popover.setPosition(PopoverPosition.TOP);
        popover.add(upload);

        inputLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        inputLayout.addAndExpand(messageInput);
        inputLayout.add(contextButton);

        messageInput.addSubmitListener(e -> {
            var prompt = e.getValue();

            var answer = new MarkdownMessage("Assistant");
            messageList.add(new MarkdownMessage(prompt, "You"), answer);

            chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(PromptChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content()
                .subscribe(answer::appendMarkdownAsync);
        });

        upload.addSucceededListener(e -> {
            processDocument(new InputStreamResource(buffer.getInputStream(e.getFileName())));
            upload.clearFileList();
        });

        var scroller = new Scroller(messageList);
        scroller.setHeightFull();
        addAndExpand(scroller);
        add(inputLayout);
    }


    void processDocument(Resource resource) {
        vectorStore.write(
            new TokenTextSplitter().apply(
                new TikaDocumentReader(resource).read()));
    }
}
