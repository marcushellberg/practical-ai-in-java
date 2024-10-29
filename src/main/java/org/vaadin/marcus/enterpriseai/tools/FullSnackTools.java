package org.vaadin.marcus.enterpriseai.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class FullSnackTools {

    public record FullSnackRequest(String location) {
    }

    public record FullSnackResponse(List<String> snacks) {
    }

    private final Map<String, List<String>> locationSnacks = Map.of(
        "Berlin", List.of("Currywurst", "Bockwurst", "Beer"),
        "Munich", List.of("Weisswurst", "Pretzel", "Beer"),
        "Hamburg", List.of("Fischbrötchen", "Labskaus", "Beer"),
        "Frankfurt", List.of("Rippchen", "Grüne Soße", "Apfelwein")
    );

    @Bean
    @Description("Function to get available snacks")
    public Function<FullSnackRequest, FullSnackResponse> getSnacks() {
        return fullSnackRequest -> {
            System.out.println("Running getSnacks function");
            return new FullSnackResponse(locationSnacks.getOrDefault(fullSnackRequest.location(), List.of()));
        };
    }
}
