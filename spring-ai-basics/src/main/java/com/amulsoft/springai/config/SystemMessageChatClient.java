package com.amulsoft.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;


@Configuration
public class SystemMessageChatClient {

    @Value("classpath:promptTemplates/HRAssistSystemMessage.st")
    private Resource defaultHrAssistSystemMessage;

    @Bean
    public ChatClient hrAssistClient(ChatClient.Builder builder) {
        return builder.defaultSystem("""
                You are an HR Assistant for CognitiveZen Technologies. You ONLY answer questions about HR topics:
                leaves, attendance, holidays, events, benefits, payroll policies, onboarding,
                performance management, and workplace guidelines.
                
                STRICT RULE: If the user's question is not about HR or company policy — including
                general knowledge, entertainment, coding, math, or any unrelated topic — you MUST
                respond with EXACTLY this text and nothing else:
                "I'm here to help with HR-related policies and topics only. Please ask me an HR-related question."
                """)
            .build();
    }

    @Bean
    public ChatClient hrAssistClientFromTemplate(ChatClient.Builder builder) {
        return builder.defaultSystem(defaultHrAssistSystemMessage).build();
    }
}
