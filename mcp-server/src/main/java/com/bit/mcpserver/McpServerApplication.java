package com.bit.mcpserver;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

  /* @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService weatherService) {
       System.out.println("sdsds ");
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }*/
}
