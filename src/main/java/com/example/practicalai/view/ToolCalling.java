package com.example.practicalai.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;
import com.example.practicalai.data.Product;
import com.example.practicalai.service.ProductService;

@Menu(title = "Tool Calling", order = 5)
@Route("tool-calling")
public class ToolCalling extends HorizontalLayout {

    public ToolCalling(ChatClient.Builder builder, ProductService productService, ChatMemory memory) {
        setSizeFull();
        var chatClient = builder
            .defaultTools(productService)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
            .build();

        add(getGridLayout(productService), getChatLayout(chatClient));
    }

    private VerticalLayout getGridLayout(ProductService productService) {
        var grid = new Grid<>(Product.class);
        grid.setItems(productService.findAll());
        grid.setColumns("id", "name", "description", "price");
        grid.getColumns().forEach(c -> {
            c.setResizable(true);
            if(!c.getKey().equals("description")) {
                c.setAutoWidth(true);
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.add("All products");
        layout.addAndExpand(grid);
        return layout;
    }

    private VerticalLayout getChatLayout(ChatClient chatClient) {
        var chatLayout = new VerticalLayout();
        chatLayout.setHeightFull();
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

        chatLayout.addAndExpand(new Scroller(messages));
        chatLayout.add(input);
        return chatLayout;
    }
}
