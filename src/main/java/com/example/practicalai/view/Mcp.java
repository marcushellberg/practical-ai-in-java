package com.example.practicalai.view;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.vaadin.firitin.components.messagelist.MarkdownMessage;

import java.util.List;

@Menu(title = "MCP", order = 7)
@Route("mcp")
public class Mcp extends VerticalLayout {

    // The MCP servers are defined in mcp-servers-config.json, and configured in application.properties
    // Spring AI configures the clients for them
    public Mcp(ChatClient.Builder builder, List<McpSyncClient> mcpSyncClients) {
        setSizeFull();

        var chatClient = builder
            .defaultSystem("""
                You are a Vaadin expert.
                Answer the user's questions in a helpful manner using the provided tools as your source.
                If the tools don't provide the answer, say "I don't know."
                """)

            .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
            .defaultAdvisors(new MessageChatMemoryAdvisor(MessageWindowChatMemory.builder().build()))
            .build();

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
