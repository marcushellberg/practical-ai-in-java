package com.example.practicalai.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class ReviewDataInitializer {

    @Bean
    public CommandLineRunner initReviews(ReviewRepository repository) {
        return args -> {
            // Check if reviews already exist
            if (repository.count() == 0) {
                // Create 20 realistic reviews
                repository.saveAll(List.of(
                    new Review("Alex T.", "I'm absolutely in love with this place! The crust on their pizza is just the right combination of crispy and chewy, and they're always so generous with the toppings. Definitely, my go-to spot whenever I'm craving pizza."),
                    new Review("Lisa G.", "Not impressed. The pizza was bland and seriously lacking in toppings. Plus, the crust was burned. There are much better options out there."),
                    new Review("Sara K.", "Five stars for sure! The pizza is amazing, and they have a great selection of craft beers. It's also a bonus that they source their ingredients locally. Definitely recommend!"),
                    new Review("Jenny P.", "The pizza was good, not the best I've had, but pretty decent. The service was okay, could have been a bit more attentive. Might give it another try."),
                    new Review("Derek H.", "Had high hopes but was let down. The service was slow and unenthusiastic. My pizza was soggy and undercooked. Unfortunately, won't be visiting again."),
                    new Review("Tom N.", "It was an alright visit. The pizza had a good flavor, but I found it a bit pricey for the size. The staff were friendly though."),
                    new Review("David S.", "Really disappointed with my visit. Waited over an hour for our pizzas, and when they arrived, they were lukewarm. Not planning on returning.")
                ));
                
                System.out.println("Initialized database with 7 reviews");
            }
        };
    }

}