package com.amulsoft.springai.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 * @author ManiReddy Somula
 */

@RestController
@RequestMapping(value = "/api/metadata")
public class MetadataController {

    private final ChatClient defaultChatClient;

    public MetadataController(@Qualifier("defaultChatClient") ChatClient defaultChatClient) {
        this.defaultChatClient = defaultChatClient;
    }

    @GetMapping(value = "/usermsg")
    public ResponseEntity<String> metaDataToUserMsg() {
        ChatResponse chatResponse = defaultChatClient.prompt()
            .user(u -> u.text("What's the weather like?")
                .metadata("messageId", "msg-123")
                .metadata("userId", "user-456")
                .metadata("priority", "high"))
            .call()
            .chatResponse();
        Generation result = chatResponse.getResult();
        return ResponseEntity.ok(result.getOutput().getText());
    }

    @GetMapping(value = "/usermsg/map")
    public ResponseEntity<String> metaDataToUserMsgUsingMap() {
        Map<String, Object> userMetadata = Map.of(
            "messageId", "msg-123",
            "userId", "user-456",
            "timestamp", System.currentTimeMillis()
        );
        String response = defaultChatClient.prompt()
            .user(u -> u.text("What's the weather like?")
                .metadata(userMetadata))
            .call()
            .content();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/systemmsg")
    public ResponseEntity<String> metaDataToSystemMsg() {
        String response = defaultChatClient.prompt()
            .system(s -> s.text("You are a helpful assistant.")
                .metadata("version", "1.0")
                .metadata("model", "gpt-4"))
            .user("Tell me a joke")
            .call()
            .content();
        return ResponseEntity.ok(response);
    }
}
