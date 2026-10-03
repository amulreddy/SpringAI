package com.amulsoft.springai.web;

import com.amulsoft.springai.model.CountryStates;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
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
@RequestMapping(value = "/api")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/country/states")
    public ResponseEntity<CountryStates> getCountryStates(
        @RequestParam("country") String country) {
        String prompt = "Return all the states in {country}";
//        CountryStates countryStates = chatClient
//            .prompt()
//            .user(spec -> spec.text(prompt).param("country", country))
//            .call()
//            .entity(CountryStates.class);

        CountryStates countryStates = chatClient
            .prompt()
            .user(spec -> spec.text(prompt).param("country", country))
            .call()
            .entity(CountryStates.class, spec -> {
                spec.useProviderStructuredOutput();
                spec.validateSchema();
            });
        return ResponseEntity.ok(countryStates);
    }

    @GetMapping("/structuredOutputConverter")
    public ResponseEntity<CountryStates> getCountryStatesWithStructuredConverter(
        @RequestParam("country") String country) {
        String prompt = "Return all the states in {country}";
        StructuredOutputConverter<CountryStates> converter = new BeanOutputConverter<>(CountryStates.class);
        CountryStates countryStates = chatClient
            .prompt()
            .user(spec -> spec
                .text(prompt)
                .param("country", country)
                .param("format", converter.getFormat()))
            .call()
            .entity(CountryStates.class);
        return ResponseEntity.ok(countryStates);
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryStates>> getAllCountries() {
        /*List<CountryStates> countries = chatClient
            .prompt()
            .user("Return top 5 countries with their top states.")
            .call()
            .entity(new ParameterizedTypeReference<>() {
            });*/
        List<CountryStates> countries = chatClient
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
            }, ChatClient.EntityParamSpec::validateSchema);
        return ResponseEntity.ok(countries);
    }
}
