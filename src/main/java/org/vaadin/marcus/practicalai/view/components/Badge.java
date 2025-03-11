package org.vaadin.marcus.practicalai.view.components;

import com.vaadin.flow.component.html.Span;
import org.vaadin.marcus.practicalai.data.Sentiment;

import java.util.List;

public class Badge extends Span {

    public Badge() {
    }

    public Badge(Sentiment sentiment) {
        super(sentiment == null ? "" : sentiment.name());
        setSentiment(sentiment);
    }

    public void setSentiment(Sentiment sentiment) {
        if (sentiment == null) {
            setText("");
            getElement().getThemeList().clear();
            return;
        }

        switch (sentiment) {
            case NEUTRAL -> getElement().getThemeList().add("badge");
            case POSITIVE -> getElement().getThemeList().addAll(List.of("badge", "success"));
            case NEGATIVE -> getElement().getThemeList().addAll(List.of("badge", "error"));
        }
    }
}
