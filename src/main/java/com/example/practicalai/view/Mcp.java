package com.example.practicalai.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Menu(title = "MCP", order = 8)
@Route("mcp")
public class Mcp extends VerticalLayout {

    public Mcp(
        ChatClient.Builder builder,
        McpSyncClient mcpSyncClient,
        @Value("classpath:aura-system-prompt.md") Resource systemPrompt
    ) {
        setSizeFull();

        var chatClient = builder
            .defaultSystem(systemPrompt)
            .defaultToolCallbacks(new SyncMcpToolCallbackProvider(List.of(mcpSyncClient)))
            .build();

        // Set up upload
        var handler = UploadHandler.inMemory((metadata, data) -> {
            var response = chatClient.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Analyze the attached image and use it to create an Aura theme for the Vaadin application.
                        Use the provided tools for updating the CSS.
                        When done, praise the user for their excellent design taste and skills.
                        """)
                    .media(
                        MimeTypeUtils.parseMimeType(metadata.contentType()),
                        new ByteArrayResource(data)
                    )
                )
                .call()
                .content();

            showNotification(response);
        });
        var upload = new Upload(handler);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFileSize(10 * 1024 * 1024);

        add(
            new H2("Ain't nobody got time for CSS! 🎨"),
            new Paragraph("Upload an image and let the AI create a theme for you."),
            upload
        );
    }

    private void showNotification(String response) {
        var notification = new Notification();
        var notificationLayout = new VerticalLayout(
            new Markdown(response),
            new Button("Yay!", e -> notification.close()) {{
                addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }}
        );
        notification.add(notificationLayout);
        notification.setDuration(0);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
    }
}
