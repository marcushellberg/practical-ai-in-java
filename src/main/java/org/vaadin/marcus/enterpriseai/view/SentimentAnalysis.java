package org.vaadin.marcus.enterpriseai.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.vaadin.marcus.enterpriseai.data.Review;
import org.vaadin.marcus.enterpriseai.data.ReviewRepository;
import org.vaadin.marcus.enterpriseai.data.Sentiment;
import org.vaadin.marcus.enterpriseai.view.components.Badge;
import org.vaadin.marcus.enterpriseai.view.components.Markdown;

@Route
@Menu(title = "Sentiment Analysis", order = 5)
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

    public SentimentAnalysis(ReviewRepository repository, ChatModel chatModel) {
        this.repository = repository;
        chatClient = ChatClient.builder(chatModel).build();

        var analyzeButton = new Button("Analyze Reviews", e -> analyzeReviews());

        add(
            new HorizontalLayout(new H1("Sentiment Analysis"), analyzeButton) {{
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
        var sentiment = chatClient.prompt()
            .user(u -> {
                u.text("""
                    Analyze the sentiment of the following review:
                    ----
                    {review}
                    ----
                    
                    Answer ONLY ONE of the following: POSITIVE, NEUTRAL, NEGATIVE
                    """);
                u.param("review", review.getReview());
            })
            .call()
            .content();

        review.setSentiment(Sentiment.valueOf(sentiment));
        repository.save(review);
    }

    private String draftResponse(Review review) {
        return chatClient.prompt()
            .system("""
                You are a customer support representative at Bob's Pizza.
                Communicate with customers in a friendly and respectful manner""")
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
