package com.example.practicalai.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@Menu(title = "MCP", order = 8)
@Route("mcp")
public class Mcp extends VerticalLayout {

    public Mcp(
        ChatClient.Builder builder,
        McpSyncClient mcpSyncClient,
        @Value("classpath:lumo-system-prompt.md") Resource systemPrompt
    ) {
        setSizeFull();

        var chatClient = builder
            .defaultSystem(systemPrompt)
            .defaultToolCallbacks(new SyncMcpToolCallbackProvider(List.of(mcpSyncClient)))
            .build();

        // Set up upload
        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFileSize(10 * 1024 * 1024);

        upload.addSucceededListener(e -> {
            var response = chatClient.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Analyze the attached imageand use it to create a Lumo theme for the Vaadin application.
                        Use the provided tools for updating the CSS.
                        When done, praise the user for their excellent design taste and skills.
                        """)
                    .media(
                        MimeTypeUtils.parseMimeType(e.getMIMEType()),
                        new InputStreamResource(buffer.getInputStream())
                    )
                )
                .call()
                .content();

            var notification = new Notification(new Markdown(response));
            notification.setDuration(15000);
            notification.setPosition(Notification.Position.MIDDLE);
            notification.open();

            upload.clearFileList();
        });

        add(
            new H2("Ain't nobody got time for CSS! 🎨"),
            new Paragraph("Upload an image and let the AI create a theme for you."),
            upload
        );
    }
}
