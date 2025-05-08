package com.example.practicalai.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration(proxyBeanMethods = false)
public class ProductDataInitializer {

    @Bean
    public CommandLineRunner initProducts(ProductRepository repository) {
        return args -> {
            // Check if products already exist
            if (repository.count() == 0) {
                // Create 20 realistic products
                repository.saveAll(Arrays.asList(
                    new Product("Smartphone X12", "Latest flagship smartphone with 6.7-inch OLED display, 128GB storage, and triple camera system.", new BigDecimal("999.99")),
                    new Product("Laptop Pro 15", "Professional laptop with 15-inch Retina display, 16GB RAM, 512GB SSD, and 10-hour battery life.", new BigDecimal("1499.99")),
                    new Product("Wireless Earbuds", "True wireless earbuds with noise cancellation, 24-hour battery life, and water resistance.", new BigDecimal("149.99")),
                    new Product("Smart Watch Series 5", "Health and fitness tracker with heart rate monitor, GPS, and 5-day battery life.", new BigDecimal("299.99")),
                    new Product("4K Ultra HD TV 55\"", "55-inch 4K Smart TV with HDR, Dolby Vision, and built-in streaming apps.", new BigDecimal("699.99")),
                    new Product("Digital Camera DSLR", "24.2MP DSLR camera with 18-55mm lens, 4K video recording, and Wi-Fi connectivity.", new BigDecimal("849.99")),
                    new Product("Gaming Console Pro", "Next-gen gaming console with 1TB storage, 4K gaming, and backward compatibility.", new BigDecimal("499.99")),
                    new Product("Bluetooth Speaker", "Portable Bluetooth speaker with 360° sound, 20-hour battery life, and waterproof design.", new BigDecimal("129.99")),
                    new Product("Tablet Air", "10.9-inch tablet with Liquid Retina display, A14 chip, and all-day battery life.", new BigDecimal("599.99")),
                    new Product("Robot Vacuum Cleaner", "Smart robot vacuum with mapping technology, app control, and automatic emptying.", new BigDecimal("399.99")),
                    new Product("Coffee Maker Deluxe", "Programmable coffee maker with built-in grinder, thermal carafe, and multiple brew settings.", new BigDecimal("199.99")),
                    new Product("Fitness Tracker Band", "Slim fitness tracker with heart rate monitoring, sleep tracking, and 7-day battery life.", new BigDecimal("79.99")),
                    new Product("Wireless Charging Pad", "Fast wireless charging pad compatible with all Qi-enabled devices.", new BigDecimal("39.99")),
                    new Product("Smart Home Hub", "Central hub for controlling all your smart home devices with voice commands.", new BigDecimal("129.99")),
                    new Product("External SSD 1TB", "Portable SSD with 1TB storage, USB-C connectivity, and 1050MB/s transfer speeds.", new BigDecimal("179.99")),
                    new Product("Noise-Cancelling Headphones", "Over-ear headphones with active noise cancellation, 30-hour battery life, and premium sound quality.", new BigDecimal("249.99")),
                    new Product("Electric Toothbrush Pro", "Smart electric toothbrush with pressure sensor, multiple cleaning modes, and app connectivity.", new BigDecimal("89.99")),
                    new Product("Air Purifier Premium", "HEPA air purifier for rooms up to 500 sq ft with air quality sensor and quiet operation.", new BigDecimal("299.99")),
                    new Product("Mechanical Keyboard", "RGB mechanical keyboard with customizable keys, macro support, and ergonomic design.", new BigDecimal("149.99")),
                    new Product("Wireless Mouse", "Ergonomic wireless mouse with adjustable DPI, programmable buttons, and long battery life.", new BigDecimal("59.99"))
                ));
                
                System.out.println("Initialized database with 20 products");
            }
        };
    }
}