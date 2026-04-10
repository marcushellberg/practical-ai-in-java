package com.example.practicalai.view;

import com.example.practicalai.ai.guardrails.JsonOutputGuardrailAdvisor;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Image data")
@Menu(title = "Image data", order = 6)
@Route("image-data")
public class ImageData extends VerticalLayout {

    public record Participant(String name, String company, String email, String tshirtSize) {
    }

    public record SignUpSheet(List<Participant> participants) {
    }

    private final Grid<Participant> grid = new Grid<>(Participant.class);

    private List<Participant> participants = new ArrayList<>();

    public ImageData(ChatClient.Builder builder) {
        var client = builder
                .defaultAdvisors(JsonOutputGuardrailAdvisor.builder()
                        .chatClientBuilder(builder.clone())
                        .type(SignUpSheet.class)
                        .build())
                .build();

        // Set up upload
        var handler = UploadHandler.inMemory((metadata, data) -> {
            var signUpSheet = client.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Please read the attached event signup sheet image and extract all participants.
                        """)
                    .media(
                        MimeTypeUtils.parseMimeType(metadata.contentType()),
                        new ByteArrayResource(data)
                    )
                )
                .call()
                .entity(SignUpSheet.class);

            showParticipants(signUpSheet);
        });
        var upload = new Upload(handler);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFileSize(10 * 1024 * 1024);

        Text instructions = new Text("Upload an image of an event signup sheet. The AI will extract participant data and display it here.");
        add(instructions, upload, createGridLayout());
    }

    private Div createGridLayout() {
        Div gridContainer = new Div();
        gridContainer.setWidthFull();

        grid.setColumns("name", "company", "email", "tshirtSize");
        grid.setSizeFull();
        grid.setItems(participants);
        grid.setAllRowsVisible(true);

        gridContainer.add(grid);
        return gridContainer;
    }

    private void showParticipants(SignUpSheet signUpSheet) {
        if (signUpSheet != null && signUpSheet.participants() != null) {
            participants = new ArrayList<>(signUpSheet.participants());
            grid.setItems(participants);
        }
    }
}