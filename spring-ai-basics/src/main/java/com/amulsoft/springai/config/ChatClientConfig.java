package com.amulsoft.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author ManiReddy Somula
 */

@Configuration
public class ChatClientConfig {

    @Bean(name = "defaultChatClient")
    public ChatClient defaultChatclient(ChatClient.Builder builder) {
        return builder.build();
    }
}
