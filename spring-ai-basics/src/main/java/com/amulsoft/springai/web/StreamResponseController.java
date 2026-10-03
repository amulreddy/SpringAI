package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 * @author ManiReddy Somula
 */

@RestController
@RequestMapping(value = "/api")
public class StreamResponseController {

    private final ChatClient chatClient;

    public StreamResponseController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/stream")
    public Flux<String> streamResponse(@RequestParam(value = "message") String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content();
    }
}
