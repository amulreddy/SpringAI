package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ManiReddy Somula
 */

@RestController
@RequestMapping(value = "/api/multimodel")
public class MultiModelController {

    private ChatClient qwenChatClient;
    private ChatClient gemmaChatClient;

    public MultiModelController(ChatClient.Builder builder) {
        OpenAiChatModel qwenModel = OpenAiChatModel.builder()
            .options(OpenAiChatOptions.builder()
                .baseUrl("http://localhost:12434/engines/v1")
                .apiKey("DUMMY")
                .model("qwen3.5:4b-iq4_XS")
                .temperature(0.5)
                .build())
            .build();
        this.qwenChatClient = ChatClient.builder(qwenModel).build();

        OpenAiChatModel gemma4model = OpenAiChatModel.builder()
            .options(OpenAiChatOptions.builder()
                .baseUrl("http://localhost:12434/engines/v1")
                .apiKey("DUMMY")
                .model("huggingface.co/unsloth/gemma-4-e2b-it-qat-gguf:UD-Q4_K_XL")
                .temperature(0.7)
                .build())
            .build();
        this.gemmaChatClient = ChatClient.builder(gemma4model).build();
    }

    @GetMapping(value = "/qwenChat")
    public String qwenChat(@RequestParam(value = "message") String message) {
        return qwenChatClient.prompt(message).call().content();
    }

    @GetMapping(value = "/gemmaChat")
    public String gemmaChat(@RequestParam(value = "message") String message) {
        ChatResponse chatResponse = gemmaChatClient.prompt(message).call().chatResponse();
        if (chatResponse == null || chatResponse.getResult() == null) {
            return "";
        }
        return chatResponse.getResult().getOutput().getText();
    }
}
