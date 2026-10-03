package com.amulsoft.springai.web;

import com.amulsoft.springai.model.CountryStates;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * @author ManiReddy Somula
 */

@RestController
@RequestMapping(value = "/api/response")
public class ChatResponseController {

    private final ChatClient defaultChatClient;

    public ChatResponseController(@Qualifier("defaultChatClient") ChatClient defaultChatClient) {
        this.defaultChatClient = defaultChatClient;
    }

    @GetMapping("/content")
    public ResponseEntity<String> stringResponse() {
        String content = defaultChatClient
            .prompt("Tell me a joke on Girls")
            .call()
            .content();
        return ResponseEntity.ok(content);
    }

    @GetMapping("/chatResponse")
    public ResponseEntity<String> chatResponse() {
        ChatResponse chatResponse = defaultChatClient
            .prompt("Tell me a joke on Girls")
            .call()
            .chatResponse();
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        return ResponseEntity.ok(assistantMessage.getText());
    }

    @GetMapping("/chatClientResponse")
    public ResponseEntity<String> chatClientResponse() {
        ChatClientResponse chatClientResponse = defaultChatClient
            .prompt("Tell me a joke on Girls")
            .call()
            .chatClientResponse();
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        Generation generation = chatResponse.getResult();
        AssistantMessage assistantMessage = generation.getOutput();
        return ResponseEntity.ok(assistantMessage.getText());
    }

    @GetMapping("/entity")
    public ResponseEntity<CountryStates> getCountryStates(
        @RequestParam("country") String country) {
        String prompt = "Return all the states in {country}";
        CountryStates countryStates = defaultChatClient
            .prompt()
            .user(spec -> spec.text(prompt).param("country", country))
            .call()
            .entity(CountryStates.class);
        return ResponseEntity.ok(countryStates);
    }

    @GetMapping("/entity/list")
    public ResponseEntity<List<CountryStates>> getAllCountries() {
        List<CountryStates> countries = defaultChatClient
            .prompt()
            .user("""
                Return the **top 5 most populated countries**, ordered by **country population in descending order**.
                
                For each country:
                
                * Return the **country name and total population**.
                * Return its **top 5 most populated states/provinces**, ordered by state/province population in descending order.
                * Include the **state/province name and population**.
                * Sort countries strictly by population DESC.
                * Sort states/provinces within each country strictly by population DESC.
                * Return structured, accurate data only. Do not include explanations or unrelated information.
                """)
            .call()
            .entity(new ParameterizedTypeReference<>() {
            });
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/responseEntity")
    public ResponseEntity<CountryStates> getAllCountriesUsingResponseEntity(@RequestParam("country") String country) {
        org.springframework.ai.chat.client.ResponseEntity<ChatResponse, CountryStates> responseEntity = defaultChatClient
            .prompt()
            .user(promptUserSpec -> promptUserSpec
                .text("Return all the states in <country>")
                .param("country", country))
            .templateRenderer(StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build())
            .call()
            .responseEntity(CountryStates.class);
        CountryStates entity = responseEntity.entity();
        return ResponseEntity.ok(entity);
    }
}
