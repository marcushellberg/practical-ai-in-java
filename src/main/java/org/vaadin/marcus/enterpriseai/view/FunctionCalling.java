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
@Menu(title = "Function Calling", order = 3)
public class FunctionCalling extends VerticalLayout {

    public FunctionCalling(ChatModel chatModel) {
        var system = """
            You are an expert on snacks and food.
            Recommend some snacks based on what is available at a given location.
            """;
        var chatClient = ChatClient.builder(chatModel)
            .defaultSystem(system)
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
            new H2("Today's snack recommendations"),
            new HorizontalLayout(location, getRecommendationsButton) {{ setDefaultVerticalComponentAlignment(Alignment.BASELINE);}}
        );
    }
}
