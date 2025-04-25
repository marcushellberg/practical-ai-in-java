package com.example.practicalai.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.chat.client.ChatClient;
import com.example.practicalai.view.components.Markdown;

import java.io.File;
import java.io.InputStream;

@Menu(title = "Document Analysis", order = 2)
@Route("document-analysis")
public class DocumentAnalysis extends VerticalLayout {

    private final ChatClient chatClient;

    public static final String SUMMARIZATION_SYSTEM_MESSAGE = """
        Summarize the following text into a concise paragraph that captures the main points and essential details without losing important information. 
        The summary should be as short as possible while remaining clear and informative.
        Use bullet points or numbered lists to organize the information if it helps to clarify the meaning. 
        Focus on the key facts, events, and conclusions. 
        Avoid including minor details or examples unless they are crucial for understanding the main ideas.
        """;

    private static final String ACTION_POINTS_SYSTEM_MESSAGE = """
        Please provide a list of action points based on the following text.
        The action points should be clear, specific, actionable, and have a person assigned to them.
        Use bullet points or numbered lists to organize the information if it helps to clarify the meaning.
        Include only the most important action items and avoid listing tasks that are already completed or not relevant.
        """;

    private final Div output = new Div();

    public DocumentAnalysis(ChatClient.Builder builder) {
        chatClient = builder.build();
        createUI();
    }

    private void createUI() {
        var fb = new FileBuffer();
        var upload = new Upload();
        upload.setReceiver(fb);


        var heading = new H1("Document Analysis Bot");
        heading.addClassName(LumoUtility.FontSize.XLARGE);

        var header = new HorizontalLayout(heading, upload);
        header.setAlignItems(Alignment.BASELINE);
        header.addClassName(LumoUtility.FlexWrap.WRAP);

        var modeSelector = new RadioButtonGroup<String>();
        modeSelector.setItems("Summarize", "Identify action points");
        modeSelector.setValue("Summarize");
        add(header, modeSelector, output);

        upload.addSucceededListener(e -> {
            var tmpFile = fb.getFileData().getFile();
            parseFile(tmpFile, modeSelector.getValue());
            tmpFile.delete();
        });
    }

    private void parseFile(File tmpFile, String mode) {
        var parser = new AutoDetectParser();
        var handler = new BodyContentHandler();

        try (InputStream stream = TikaInputStream.get(tmpFile)) {
            parser.parse(stream, handler, new Metadata());
            analyzeFile(handler.toString(), mode);
        } catch (WriteLimitReachedException ex) {
            Notification.show(ex.getMessage());
            analyzeFile(handler.toString(), mode);
        } catch (Exception ex) {
            output.add(new H2("Parsing Data failed: " + ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }

    private void analyzeFile(String content, String mode) {
        var markdown = chatClient.prompt()
            .system(mode.equals("Summarize") ? SUMMARIZATION_SYSTEM_MESSAGE : ACTION_POINTS_SYSTEM_MESSAGE)
            .user("Text to summarize: " + content)
            .call()
            .content();

        output.removeAll();
        output.add(new Markdown(markdown));
    }
}
