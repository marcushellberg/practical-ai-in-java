package org.vaadin.marcus.enterpriseai.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class SnackTools {

    public record SnackRequest(String location) {
    }

    public record SnackResponse(List<String> snacks) {
    }

    private final Map<String, List<String>> locationSnacks = Map.of(
        "Berlin", List.of("Currywurst", "Bockwurst", "Beer"),
        "Munich", List.of("Weisswurst", "Pretzel", "Beer"),
        "Hamburg", List.of("Fischbrötchen", "Labskaus", "Beer"),
        "Frankfurt", List.of("Rippchen", "Grüne Soße", "Apfelwein")
    );

    @Bean
    @Description("Function to get available snacks")
    public Function<SnackRequest, SnackResponse> getSnacks() {
        return snackRequest -> {
            System.out.println("Running getSnacks function");
            return new SnackResponse(locationSnacks.getOrDefault(snackRequest.location(), List.of()));
        };
    }
}
