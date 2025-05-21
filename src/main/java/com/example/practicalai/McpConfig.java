package com.example.practicalai;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.time.Duration;

@Configuration
public class McpConfig {

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpClient() {

        // based on
        // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder("npx")
            .args("-y", "@modelcontextprotocol/server-filesystem", getFrontendPath())
            .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
            .requestTimeout(Duration.ofSeconds(10)).build();

        var init = mcpClient.initialize();

        System.out.println("MCP Initialized: " + init);

        return mcpClient;

    }

    private static String getFrontendPath() {
        return Paths.get(System.getProperty("user.dir"), "src", "main", "frontend").toString();
    }
}
