package com.example.practicalai.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.ai.chat.client.ChatClient;
import com.example.practicalai.data.Review;
import com.example.practicalai.data.ReviewRepository;
import com.example.practicalai.data.Sentiment;
import com.example.practicalai.view.components.Badge;
import com.example.practicalai.view.components.Markdown;

@Menu(title = "Sentiment Analysis", order = 3)
@Route("sentiment-analysis")
public class SentimentAnalysis extends VerticalLayout {

    private final ReviewRepository repository;
    private final VerticalLayout reviews = new VerticalLayout();
    private final ChatClient chatClient;

    class ReviewCard extends VerticalLayout {

        public ReviewCard(Review review) {
            addClassNames(
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.SMALL,
                LumoUtility.Margin.Bottom.XLARGE
            );
            setMaxWidth("500px");

            var name = new H3(review.getName()) {{
                addClassName(LumoUtility.FontSize.LARGE);
            }};
            var sentiment = new Badge(review.getSentiment());
            var reviewText = new Paragraph(review.getReview());
            var respondButton = new Button("Respond");

            respondButton.addClickListener(e -> {
                remove(respondButton);
                var responseField = new TextArea();
                responseField.setWidthFull();
                responseField.setValue(draftResponse(review));

                var sendButton = new Button("Send", ev -> {
                    review.setResponse(responseField.getValue());
                    repository.save(review);
                    updateReviews();
                });

                add(responseField, sendButton);
            });

            add(
                new HorizontalLayout(name, sentiment),
                reviewText,
                review.getResponse()== null || review.getResponse().isBlank() ?
                    respondButton :
                    new Markdown(review.getResponse()) {{
                        addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.MEDIUM);
                    }}
            );
        }
    }

    public SentimentAnalysis(ReviewRepository repository, ChatClient.Builder builder) {
        this.repository = repository;
        chatClient = builder.build();

        var analyzeButton = new Button("Analyze Reviews", e -> analyzeReviews());

        add(
            new H1("Bob's pizza"),
            new HorizontalLayout(new H2("Customer reviews"), analyzeButton) {{
                setAlignItems(Alignment.CENTER);
            }},
            reviews
        );

        updateReviews();
    }

    private void analyzeReviews() {
        repository.findAll().forEach(this::analyzeReview);
        updateReviews();
    }

    private void analyzeReview(Review review) {
        record SentimentResponse(Sentiment sentiment) {}

        var sentimentResponse = chatClient.prompt()
            .user(u -> {
                u.text("""
                    Analyze the sentiment of the following review:
                    ----
                    {review}
                    ----
                    """);
                u.param("review", review.getReview());
            })
            .call()
            .entity(SentimentResponse.class);

        review.setSentiment(sentimentResponse.sentiment());
        repository.save(review);
    }

    private String draftResponse(Review review) {
        return chatClient.prompt()
            .system("""
                You are a customer support representative at Bob's Pizza.
                Communicate with customers in a friendly and respectful manner.
                Messages should be concise and to the point.
                """)
            .user(u -> {
                u.text("Draft a response to the following customer review posted by {name}: {review}");
                u.param("name", review.getName());
                u.param("review", review.getReview());
            })
            .call()
            .content();
    }

    private void updateReviews() {
        reviews.removeAll();
        repository.findAll().forEach(review -> reviews.add(new ReviewCard(review)));
    }
}
