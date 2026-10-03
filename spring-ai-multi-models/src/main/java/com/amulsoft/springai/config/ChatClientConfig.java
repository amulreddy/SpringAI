package com.amulsoft.springai.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient openAiChatClient(OpenAiChatModel chatModel, ChatClientBuilderConfigurer configurer,
                                       ObjectProvider<ObservationRegistry> observationRegistry,
                                       ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
                                       ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
                                       ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {
        return buildChatClient(chatModel, configurer, observationRegistry,
            chatClientObservationConvention, advisorObservationConvention, toolCallingAdvisorBuilder);
    }

    @Bean
    public ChatClient googleChatClient(GoogleGenAiChatModel chatModel, ChatClientBuilderConfigurer configurer,
                                       ObjectProvider<ObservationRegistry> observationRegistry,
                                       ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
                                       ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
                                       ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {
        return buildChatClient(chatModel, configurer, observationRegistry,
            chatClientObservationConvention, advisorObservationConvention, toolCallingAdvisorBuilder);
    }

    private ChatClient buildChatClient(ChatModel chatModel, ChatClientBuilderConfigurer configurer,
                                       ObjectProvider<ObservationRegistry> observationRegistry,
                                       ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
                                       ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
                                       ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {
        ChatClient.Builder builder = ChatClient.builder(chatModel,
            observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
            chatClientObservationConvention.getIfUnique(),
            advisorObservationConvention.getIfUnique(),
            toolCallingAdvisorBuilder.getIfAvailable());
        return configurer.configure(builder).build();
    }
}