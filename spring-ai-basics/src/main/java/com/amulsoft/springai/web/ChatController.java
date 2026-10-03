package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
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

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/chat")
    public String chat(@RequestParam(name = "message") String message) {
        return chatClient
            .prompt(message)
            .call().content();
    }

    @GetMapping("/hrAssist")
    public String inquire(@RequestParam(name = "message") String message) {
        return chatClient
            .prompt()
            .system("""
                You are an HR Assistant for CognitiveZen. You ONLY answer questions about HR topics:
                leaves, attendance, holidays, events, benefits, payroll policies, onboarding,
                performance management, and workplace guidelines.
                
                STRICT RULE: If the user's question is not about HR or company policy — including
                general knowledge, entertainment, coding, math, or any unrelated topic — you MUST
                respond with EXACTLY this text and nothing else:
                "I'm here to help with HR-related policies and topics only. Please ask me an HR-related question."
                """)
            .user(message)
            .call().content();
    }
}
