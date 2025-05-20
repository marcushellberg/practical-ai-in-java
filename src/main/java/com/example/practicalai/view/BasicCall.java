package com.example.practicalai.view;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;

import java.time.Instant;

@Menu(title = "Basic Calling", order = 1)
@Route("")
public class BasicCall extends VerticalLayout {

    private final ChatClient chatClient;

    public BasicCall(ChatClient.Builder builder) {
        setSizeFull();

        chatClient = builder.build();

        buildView();
    }

    private void buildView() {
        var messages = new MessageList();
        var input = new MessageInput();

        messages.setMarkdown(true);
        input.setWidthFull();

        input.addSubmitListener(event -> {
            var userMessage = event.getValue();

            messages.addItem(new MessageListItem(userMessage, Instant.now(), "You"));

            var response = chatClient.prompt()
                .user(userMessage)
                .call()
                .content();

            messages.addItem(new MessageListItem(response, Instant.now(), "AI"));
        });

        addAndExpand(new Scroller(messages));
        add(input);
    }
}
