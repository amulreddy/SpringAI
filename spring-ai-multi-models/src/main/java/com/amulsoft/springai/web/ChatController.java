package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ManiReddy Somula
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatClient googleChatClient;

    public ChatController(ChatClient chatClient,
                          @Qualifier("googleChatClient") ChatClient googleChatClient) {
        this.chatClient = chatClient;
        this.googleChatClient = googleChatClient;
    }

    @GetMapping(value = "/chatWithOpenAIClient")
    public String chatWithOpenAIClient(@RequestParam(name = "message") String message) {
        return chatClient.prompt(message).call().content();
    }

    @GetMapping(value = "/chatWithGoogleChatClient")
    public String chatwithGoogleChatClient(@RequestParam(name = "message") String message) {
        return googleChatClient.prompt(message).call().content();
    }

}
