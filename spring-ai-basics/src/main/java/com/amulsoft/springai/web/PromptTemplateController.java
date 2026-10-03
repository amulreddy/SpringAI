package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ManiReddy Somula
 */

@RestController
@RequestMapping("/api/email")
public class PromptTemplateController {

    @Value("classpath:promptTemplates/EmailAssistPromptTemplate.st")
    private Resource emailAssistTemplate;

    private ChatClient chatClient;

    public PromptTemplateController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/build")
    public String emailAssist(@RequestParam(value = "customerName") String customerName,
                              @RequestParam(value = "subject") String subject,
                              @RequestParam(value = "customerEmail") String customerEmail) {
         return chatClient.prompt()
             .user(spec -> spec.text(emailAssistTemplate)
                 .param("customerName", customerName)
                 .param("subject", subject)
                 .param("customerEmail", customerEmail))
             .call().content();
    }

    @GetMapping(value = "/build/customTemplate")
    public String emailAssistWithCustomPromptTemplate() {
        return chatClient.prompt()
            .user(spec -> spec
                .text("Tell me the names of 5 movies whose soundtrack was composed by <composer>")
                .param("composer", "M.M.Keeravani")
            ).templateRenderer(StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
            .call().content();
    }
}
