package com.example.practicalai.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.time.Instant;

@Menu(title = "Simple RAG", order = 6)
@Route("simple-rag")
public class SimpleRag extends VerticalLayout {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public SimpleRag(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        chatClient = builder
                .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(VectorStoreDocumentRetriever.builder()
                                .vectorStore(vectorStore)
                                .build())
                        .build())
                .build();

        buildView();
    }

    private void buildView() {
        var messageList = new MessageList();
        messageList.setMarkdown(true);
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

            messageList.addItem(new MessageListItem(prompt, Instant.now(), "You"));

            var answer = new MessageListItem("", Instant.now(), "Bot");
            messageList.addItem(answer);

            var ui = UI.getCurrent();
            chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .subscribe(token -> ui.access(() -> answer.appendText(token)));
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
        var documentReader = new TikaDocumentReader(resource);
        var documentTransformer = new TokenTextSplitter();
        vectorStore.write(documentTransformer.apply(documentReader.read()));
    }

}
