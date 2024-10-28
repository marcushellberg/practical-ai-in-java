package org.vaadin.marcus.enterpriseai.view;

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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;
import org.vaadin.marcus.enterpriseai.advisors.RerankQuestionAnswerAdvisor;
import org.vaadin.marcus.enterpriseai.advisors.StandaloneQuestionAdvisor;
import org.vaadin.marcus.enterpriseai.advisors.VectorStoreDataSource;

import java.util.List;
import java.util.UUID;

@Route("")
@Menu(title = "RAG Chat")
public class RagChat extends VerticalLayout {

    private ChatClient ai;
    private final VectorStore vectorStore;
    private final String chatId = UUID.randomUUID().toString();
    private final String cohereApiKey;

    public RagChat(ChatModel chatModel, VectorStore vectorStore, ChatMemory chatMemory, @Value("${cohere.api.key}") String cohereApiKey) {
        this.vectorStore = vectorStore;
        this.cohereApiKey = cohereApiKey;
        configureChatClient(chatModel, vectorStore, chatMemory);

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

            ai.prompt()
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

    private void configureChatClient(ChatModel chatModel, VectorStore vectorStore, ChatMemory chatMemory) {
        var standaloneChatClient = ChatClient.builder(chatModel).build();

        ai = ChatClient.builder(chatModel)
            .defaultSystem("""
                You are a helpful assistant that can answer questions and provide guidance.
                Use the provided context to generate a response, but do not refer to it directly.
                
                Bad answer: "Based on the context, the answer is..."
                Good answer: "The answer is..."
                """)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(chatMemory),
                new StandaloneQuestionAdvisor(standaloneChatClient, -1),
                RerankQuestionAnswerAdvisor.builder(
                        List.of(
                            new VectorStoreDataSource(vectorStore, SearchRequest.defaults())
                            // Could include DataSources like internet, keyword search, etc
                        ),
                        cohereApiKey)
                    .build()
            )
            .build();
    }


    void processDocument(Resource resource) {
        vectorStore.write(
            new TokenTextSplitter().apply(
                new TikaDocumentReader(resource).read()));
    }
}
