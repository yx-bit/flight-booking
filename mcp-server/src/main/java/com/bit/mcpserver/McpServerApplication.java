package com.bit.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
