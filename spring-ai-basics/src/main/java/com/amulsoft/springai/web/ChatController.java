package com.amulsoft.springai.web;

import com.amulsoft.springai.model.ChatRequest;
import com.amulsoft.springai.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 *
 * @author ManiReddy Somula
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    @Value("classpath:/promptTemplates/CustomerSupportAssistTemplate.st")
    private Resource customerSupportAssistTemplate;

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    @PostMapping(value = "/chatTxt")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationIdOrDefault();

        String reply = chatClient.prompt()
            .system("""
                You are an HR Assistant for [Company Name]. You ONLY answer questions about HR topics:
                leaves, attendance, holidays, events, benefits, payroll policies, onboarding,
                performance management, and workplace guidelines.
                
                STRICT RULE: If the user's question is not about HR or company policy — including
                general knowledge, entertainment, coding, math, or any unrelated topic — you MUST
                respond with EXACTLY this text and nothing else:
                "I'm here to help with HR-related policies and topics only. Please ask me an HR-related question."
                
                Do not explain, apologize, or provide any information related to the off-topic request,
                even partially. Do not answer the question "helpfully" before redirecting.
                
                Examples:
                User: Tell me a list of movies
                Assistant: I'm here to help with HR-related policies and topics only. Please ask me an HR-related question.
                
                User: What's the capital of France?
                Assistant: I'm here to help with HR-related policies and topics only. Please ask me an HR-related question.
                
                User: How many paid leaves do I get per year?
                Assistant: [answer normally using HR policy knowledge]
                
                User: Write me a Python script
                Assistant: I'm here to help with HR-related policies and topics only. Please ask me an HR-related question.
                """)
            .user(request.message())
            .call()
            .content();

        return new ChatResponse(reply, conversationId);
    }

    @GetMapping("/email")
    public String emailResponse(@RequestParam("customerName") String customerName,
                                @RequestParam("customerMessage") String customerMessage) {
        return chatClient
            .prompt()
            .system("""
                You are a professional customer service assistant which helps drafting email
                responses to improve the productivity of the customer support team.
                We will have the subject and other details we just need to generate email body.
                """)
            .user(promptTemplateSpec ->
                promptTemplateSpec.text(customerSupportAssistTemplate)
                    .param("customerName", customerName)
                    .param("customerMessage", customerMessage))
            .call().content();
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatRequest request) {
        return chatClient.prompt()
            .user(request.message())
            .stream()
            .content();
    }
}
