package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author ManiReddy Somula
 */


@RestController
@RequestMapping("/api/hr")
public class HRAssistantController {

    @Value("classpath:promptTemplates/HRAssistPromptStuffing.st")
    private Resource hrAssistPromptStuffingMsg;

    private final ChatClient chatClient;
    private final ChatClient hrAssistClientFromTemplate;

    public HRAssistantController(ChatClient hrAssistClient, ChatClient hrAssistClientFromTemplate) {
        this.chatClient = hrAssistClient;
        this.hrAssistClientFromTemplate = hrAssistClientFromTemplate;
    }

    @GetMapping("/inquire")
    public String inquireWithDefaultSystemMessage(@RequestParam(name = "message") String message) {
        return chatClient
            .prompt()
            .user(message)
            .call().content();
    }

    @GetMapping("/inquireWithStringTempalte")
    public String withSystemDefaultMessageFromStringTemplate(@RequestParam(name = "message") String message) {
        return hrAssistClientFromTemplate.prompt()
            .user(message).call().content();
    }

    @GetMapping("/inquireWithStuffing")
    public String inquireWithStuffing(@RequestParam(name = "message") String message) {
        return hrAssistClientFromTemplate.prompt()
            .system(hrAssistPromptStuffingMsg)
            .user(message)
            .call()
            .content();
    }

}
