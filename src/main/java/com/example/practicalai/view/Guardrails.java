package com.example.practicalai.view;

import com.example.practicalai.ai.guardrails.SensitiveDataInputGuardrailAdvisor;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.time.Instant;

@Menu(title = "Guardrails", order = 2)
@Route("/guardrails")
public class Guardrails extends VerticalLayout {

    public Guardrails(ChatClient.Builder builder) {
        setSizeFull();

        var chatClient = builder
                .defaultAdvisors(SensitiveDataInputGuardrailAdvisor.builder()
                        .chatClientBuilder(builder.clone())
                        .build())
                .defaultOptions(ChatOptions.builder()
                        .model("gpt-4o-mini")
                        .build())
                .build();

        var messages = new MessageList();
        messages.setMarkdown(true);

        var input = new MessageInput();
        input.setWidthFull();

        input.addSubmitListener(event -> {
            var message = event.getValue();

            messages.addItem(new MessageListItem(message, Instant.now(), "You"));

            var response = chatClient.prompt().user(message).call().content();
            messages.addItem(new MessageListItem(response, Instant.now(), "AI"));
        });

        addAndExpand(new Scroller(messages));
        add(input);
    }
}
