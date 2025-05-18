package com.example.practicalai.view;

import com.example.practicalai.ai.guardrails.JsonOutputGuardrailAdvisor;
import com.example.practicalai.ai.guardrails.SensitiveDataInputGuardrailAdvisor;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.MimeTypeUtils;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Image data")
@Menu(title = "Image data", order = 4)
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
        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFileSize(10 * 1024 * 1024);

        upload.addSucceededListener(e -> {
            var signUpSheet = client.prompt()
                .user(userMessage -> userMessage
                    .text("""
                        Please read the attached event signup sheet image and extract all participants.
                        """)
                    .media(
                        MimeTypeUtils.parseMimeType(e.getMIMEType()),
                        new InputStreamResource(buffer.getInputStream())
                    )
                )
                .call()
                .entity(SignUpSheet.class);

            showParticipants(signUpSheet);
            upload.clearFileList();
        });

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