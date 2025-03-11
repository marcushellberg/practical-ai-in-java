package org.vaadin.marcus.enterpriseai.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.vaadin.marcus.enterpriseai.view.components.Markdown;

@Route
@Menu(title = "Function Calling", order = 6)
public class FunctionCalling extends VerticalLayout {

    public static final String SYSTEM_MESSAGE = """
        You are an expert on snacks and food.
        Recommend some snacks based on what is available at a given location.
        """;

    public FunctionCalling(ChatClient.Builder builder) {
        var chatClient = builder
            .defaultSystem(SYSTEM_MESSAGE)
            .defaultFunctions("getSnacks")
            .build();

        var location = new TextField("Location");

        var getRecommendationsButton = new Button("Get recommendations", e -> {
            var recommendation = chatClient.prompt()
                .user(u -> {
                    u.text("I'm in {location} and I'm hungry, what should I eat?");
                    u.param("location", location.getValue());
                })
                .call()
                .content();

            add(new Markdown(recommendation));
        });


        add(
            new H2("FullSnack recommendations"),
            new HorizontalLayout(location, getRecommendationsButton) {{ setDefaultVerticalComponentAlignment(Alignment.BASELINE);}}
        );
    }
}
