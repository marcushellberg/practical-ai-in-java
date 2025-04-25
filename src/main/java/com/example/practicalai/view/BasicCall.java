package com.example.practicalai.view;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

@Menu(title = "Basic Calling", order = 1)
@Route("")
public class BasicCall extends VerticalLayout {

    public BasicCall(ChatClient.Builder builder) {
        setSizeFull();

        var chatClient = builder.build();

        var messages = new VerticalLayout();
        var input = new MessageInput();
        input.setWidthFull();

        input.addSubmitListener(event -> {
            var message = event.getValue();
            var response = new MarkdownMessage("Bot");

            messages.add(
                new MarkdownMessage(message, "You"),
                response
            );

            chatClient.prompt()
                .user(event.getValue())
                .stream()
                .content()
                .subscribe(response::appendMarkdownAsync);
        });

        addAndExpand(new Scroller(messages));
        add(input);
    }
}
