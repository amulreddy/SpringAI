# Spring AI Multi-Model Chatbot

A multi-model AI chatbot built using **Spring Boot 4.0**, **Spring AI 2.0**, and multiple LLM providers.

This application demonstrates how to integrate different Large Language Models (LLMs) into a single Spring Boot application using Spring AI's `ChatClient` abstraction.

The application currently supports:

* **Docker Model Runner (Local LLM):** Runs Gemma locally without cloud inference APIs.
* **Google Gemini:** Uses Google GenAI API for cloud-based inference.
* **Multiple ChatClient Beans:** Creates independent ChatClient instances for each LLM provider.
* **Primary ChatClient:** Uses the local OpenAI-compatible model as the default client.
* **Provider-specific APIs:** Allows clients to select the required LLM through separate REST endpoints.
* **Shared Configuration:** Reuses Spring AI observations, advisors, and builder configuration across providers.

---

# 1. Architecture

```text
                         Browser / REST Client
                                  |
                                  |
                            Spring Boot 4.0.0
                                  |
                                  |
                            ChatController
                                  |
                  +---------------+----------------+
                  |                                |
                  |                                |
          OpenAI ChatClient                Google ChatClient
             (@Primary)                    (@Qualifier)
                  |                                |
                  |                                |
          OpenAiChatModel                GoogleGenAiChatModel
                  |                                |
                  |                                |
          OpenAI-Compatible API                Gemini API
                  |                                |
                  |                                |
          Docker Model Runner                 Google Cloud
                  |                                |
                  |                                |
             Local LLM                       Gemini Model
             Gemma 4                         Gemini Flash
                  |                                |
                  +---------------+----------------+
                                  |
                                  |
                            Chat Response
```

## Architecture Components

| Component            | Responsibility                                  |
| -------------------- | ----------------------------------------------- |
| Spring Boot          | Application framework                           |
| Spring AI            | LLM integration abstraction                     |
| ChatClient           | Provides a fluent API for interacting with LLMs |
| OpenAiChatModel      | Connects to the OpenAI-compatible API           |
| GoogleGenAiChatModel | Connects to Google Gemini                       |
| ChatClientConfig     | Configures multiple ChatClient beans            |
| ChatController       | Exposes provider-specific REST APIs             |
| Docker Model Runner  | Executes local LLM inference                    |
| Gemini API           | Provides cloud-based LLM inference              |

---

# 2. Technology Stack

| Technology          | Version / Description       |
| ------------------- | --------------------------- |
| Java                | 25                          |
| Spring Boot         | 4.0.x                       |
| Spring AI           | 2.0.x                       |
| Maven               | 3.9+                        |
| Docker Model Runner | Local LLM runtime           |
| Local Model         | Gemma 4                     |
| Cloud Model         | Gemini Flash                |
| API Communication   | REST                        |
| Logging             | SLF4J / Spring Boot Logging |

---

# 3. Project Structure

```text
spring-ai-chatbot/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── amulsoft/
│       │           └── springai/
│       │               │
│       │               ├── config/
│       │               │   └── ChatClientConfig.java
│       │               │
│       │               ├── web/
│       │               │   └── ChatController.java
│       │               │
│       │               └── SpringAiChatbotApplication.java
│       │
│       └── resources/
│           └── application.yml
│
├── pom.xml
└── README.md
```

---

# 4. Prerequisites

Install the following:

* Java 25
* Maven 3.9+
* Docker Desktop or Docker Engine
* Docker Model Runner
* Google Gemini API key

Verify installations:

```bash
java -version
mvn -version
docker --version
```

---

# 5. LLM Provider Configuration

The application supports multiple LLM providers through a single `application.yml`.

## 5.1 Complete application.yml

```yaml
spring:
  application:
    name: spring-ai-chatbot

  ai:
    openai:
      # Placeholder API key for local OpenAI-compatible models
      api-key: dummy

      # Docker Model Runner endpoint
      base-url: http://localhost:12434/engines/v1

      chat:
        model: gemma4:E4B

    google:
      genai:
        # Google Gemini API key
        api-key: ${GEMINI_API_KEY}

        chat:
          options:
            model: gemini-3.6-flash
            temperature: 0.3
            max-output-tokens: 1024

server:
  port: 8097

logging:
  level:
    org.springframework.ai.chat.client.advisor: DEBUG
```

### Configuration Explanation

| Property                                    | Description                               |
| ------------------------------------------- | ----------------------------------------- |
| `spring.application.name`                   | Application name                          |
| `spring.ai.openai.api-key`                  | Placeholder key for local DMR integration |
| `spring.ai.openai.base-url`                 | Docker Model Runner endpoint              |
| `spring.ai.openai.chat.model`               | Local Gemma model identifier              |
| `spring.ai.google.genai.api-key`            | Google Gemini API key                     |
| `spring.ai.google.genai.chat.options.model` | Gemini model identifier                   |
| `temperature`                               | Controls response variability             |
| `max-output-tokens`                         | Maximum generated output tokens           |
| `server.port`                               | Application port                          |
| `logging.level`                             | Enables Spring AI advisor debugging       |

**Note:** The configuration above follows the properties supplied for your project. Confirm the model property names against the exact Spring AI 2.0.x version used in your `pom.xml`. Spring AI configuration property names have changed across releases.

---

# 6. Docker Model Runner Setup

Docker Model Runner allows developers to execute LLMs locally and access them through an OpenAI-compatible REST API.

In this application, Docker Model Runner is used as the local inference engine for the OpenAI ChatClient.

## Step 1: Enable Docker Model Runner

For Docker Desktop:

1. Open Docker Desktop.
2. Navigate to Settings → AI.
3. Enable Docker Model Runner.
4. Enable TCP access for host applications.

For TCP access:

```bash
docker desktop enable model-runner --tcp 12434
```

For Docker Engine, install and configure Docker Model Runner according to the official documentation.

Documentation:

https://docs.docker.com/ai/model-runner/

## Step 2: Pull the Model

Pull the required Gemma model using Docker Model Runner.

```bash
docker model pull ai/gemma
```

Check downloaded models:

```bash
docker model list
```

Use the exact model identifier supported by your Docker Model Runner installation.

## Step 3: Verify Docker Model Runner

```bash
docker model status
```

Check the available models through the API:

```bash
curl http://localhost:12434/engines/v1/models
```

## Step 4: Test Local Model Inference

```bash
curl http://localhost:12434/engines/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "ai/gemma",
    "messages": [
      {
        "role": "user",
        "content": "Explain Spring AI in simple terms."
      }
    ],
    "stream": false
  }'
```

If the model returns a response, the local LLM is ready.

## Step 5: Configure Spring AI

Configure the Docker Model Runner endpoint:

```yaml
spring:
  ai:
    openai:
      api-key: dummy
      base-url: http://localhost:12434/engines/v1
      chat:
        model: gemma4:E4B
```

The configured model identifier must match the model name exposed by Docker Model Runner.

The placeholder API key is used to satisfy the OpenAI client configuration. It is not a real OpenAI credential.

---

# 7. Google Gemini Configuration

Google Gemini provides cloud-based LLM inference through the Google GenAI API.

Unlike Docker Model Runner, this provider requires an API key and an internet connection.

## Step 1: Generate Gemini API Key

Visit:

https://aistudio.google.com/apikey

Generate an API key and configure it as an environment variable.

Ubuntu:

```bash
export GEMINI_API_KEY=your_gemini_api_key
```

Verify:

```bash
echo $GEMINI_API_KEY
```

For persistent configuration, add the environment variable to your shell configuration file or use an appropriate secrets-management solution.

Do not commit actual API keys to GitHub.

## Step 2: Configure application.yml

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: gemini-3.6-flash
            temperature: 0.3
            max-output-tokens: 1024
```

The configured model must be available for your Google GenAI API account.

---

# 8. Maven Dependencies

Use the Spring AI BOM to manage compatible Spring AI dependencies.

Example:

```xml
<properties>
    <java.version>25</java.version>
    <spring-ai.version>2.0.1</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>

    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- OpenAI-compatible Chat Model -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

    <!-- Google Gemini Chat Model -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-google-genai</artifactId>
    </dependency>

</dependencies>
```

The exact BOM and dependency versions must be aligned with the Spring Boot and Spring AI versions used by the application.

---

# 9. Multiple ChatClient Implementation

Spring AI provides the `ChatClient` abstraction to interact with different LLM providers.

Instead of directly invoking model-specific APIs from the controller, this implementation creates independent ChatClient beans for each provider.

This provides:

* Separation between providers.
* Independent model configuration.
* Shared observation and advisor configuration.
* Easy integration of additional providers.
* Cleaner controller implementation.

## 9.1 ChatClientConfig.java

Package:

```text
com.amulsoft.springai.config
```

```java
@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient openAiChatClient(
            OpenAiChatModel chatModel,
            ChatClientBuilderConfigurer configurer,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
            ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
            ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {

        return buildChatClient(
                chatModel,
                configurer,
                observationRegistry,
                chatClientObservationConvention,
                advisorObservationConvention,
                toolCallingAdvisorBuilder
        );
    }

    @Bean
    public ChatClient googleChatClient(
            GoogleGenAiChatModel chatModel,
            ChatClientBuilderConfigurer configurer,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
            ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
            ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {

        return buildChatClient(
                chatModel,
                configurer,
                observationRegistry,
                chatClientObservationConvention,
                advisorObservationConvention,
                toolCallingAdvisorBuilder
        );
    }

    private ChatClient buildChatClient(
            ChatModel chatModel,
            ChatClientBuilderConfigurer configurer,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
            ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
            ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {

        ChatClient.Builder builder = ChatClient.builder(
                chatModel,
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                chatClientObservationConvention.getIfUnique(),
                advisorObservationConvention.getIfUnique(),
                toolCallingAdvisorBuilder.getIfAvailable()
        );

        return configurer.configure(builder).build();
    }
}
```

### Implementation Explanation

#### 1. OpenAI ChatClient

```java
@Bean
@Primary
public ChatClient openAiChatClient(OpenAiChatModel chatModel, ...)
```

Creates a ChatClient using `OpenAiChatModel`.

In this application, the OpenAI model integration communicates with Docker Model Runner rather than the actual OpenAI cloud API.

The `@Primary` annotation makes this bean the default choice when Spring finds multiple candidates of type `ChatClient` and no qualifier is specified.

#### 2. Google ChatClient

```java
@Bean
public ChatClient googleChatClient(GoogleGenAiChatModel chatModel, ...)
```

Creates a separate ChatClient using `GoogleGenAiChatModel`.

This client communicates with Google's Gemini API.

#### 3. Shared Builder Method

```java
private ChatClient buildChatClient(ChatModel chatModel, ...)
```

Both providers use the same builder method.

The method accepts the provider-specific `ChatModel` and configures:

* ObservationRegistry.
* ChatClientObservationConvention.
* AdvisorObservationConvention.
* ToolCallingAdvisor.Builder.
* ChatClientBuilderConfigurer.

This avoids duplicating the ChatClient initialization logic.

#### 4. Observation Configuration

```java
observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)
```

Uses the available observation registry when unique, otherwise falls back to a no-operation registry.

This allows the application to work without requiring a custom observation registry.

---

# 10. ChatController Implementation

Package:

```text
com.amulsoft.springai.web
```

```java
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatClient googleChatClient;

    public ChatController(
            ChatClient chatClient,
            @Qualifier("googleChatClient")
            ChatClient googleChatClient) {

        this.chatClient = chatClient;
        this.googleChatClient = googleChatClient;
    }

    @GetMapping(value = "/chatWithOpenAIClient")
    public String chatWithOpenAIClient(
            @RequestParam(name = "message") String message) {

        return chatClient
                .prompt(message)
                .call()
                .content();
    }

    @GetMapping(value = "/chatWithGoogleChatClient")
    public String chatWithGoogleChatClient(
            @RequestParam(name = "message") String message) {

        return googleChatClient
                .prompt(message)
                .call()
                .content();
    }
}
```

## 10.1 Dependency Injection

The controller receives two different ChatClient beans.

```java
private final ChatClient chatClient;
private final ChatClient googleChatClient;
```

### Default ChatClient

```java
ChatClient chatClient
```

Because the OpenAI ChatClient is annotated with `@Primary`, Spring injects the local Docker Model Runner ChatClient by default.

### Qualified ChatClient

```java
@Qualifier("googleChatClient")
ChatClient googleChatClient
```

The `@Qualifier` annotation explicitly selects the Google ChatClient bean.

This avoids ambiguity when multiple ChatClient beans are registered.

## 10.2 Calling the LLM

The application uses the Spring AI fluent API:

```java
chatClient
    .prompt(message)
    .call()
    .content();
```

Execution flow:

1. Accepts the user message.
2. Creates a prompt using `ChatClient`.
3. Invokes the configured LLM.
4. Waits for the generated response.
5. Extracts the response content.
6. Returns the response to the REST client.

The same implementation works with different providers because Spring AI abstracts the underlying model interaction.

---

# 11. REST API Endpoints

The application exposes two endpoints.

| HTTP Method | Endpoint                        | Provider            | Description        |
| ----------- | ------------------------------- | ------------------- | ------------------ |
| GET         | `/api/chatWithOpenAIClient`     | Docker Model Runner | Local LLM response |
| GET         | `/api/chatWithGoogleChatClient` | Google Gemini       | Cloud LLM response |

## 11.1 Docker Model Runner API

Request:

```bash
curl -G "http://localhost:8097/api/chatWithOpenAIClient" \
  --data-urlencode "message=Explain Spring AI in simple terms"
```

Response:

```text
Spring AI is a framework that simplifies the integration
of AI models into Java applications.
```

## 11.2 Google Gemini API

Request:

```bash
curl -G "http://localhost:8097/api/chatWithGoogleChatClient" \
  --data-urlencode "message=Explain Spring AI in simple terms"
```

Response:

```text
Spring AI provides abstractions for integrating
different AI models into Java applications.
```

The actual generated response will vary depending on the model and prompt.

---

# 12. Run the Application

## Step 1: Configure Java 21

Ubuntu example:

```bash
export JAVA_HOME=/usr/lib/jvm/amazon-corretto-21.0.9.11.1-linux-x64

export PATH=$JAVA_HOME/bin:$PATH
```

Verify:

```bash
java -version
```

## Step 2: Configure Gemini API Key

```bash
export GEMINI_API_KEY=your_gemini_api_key
```

## Step 3: Start Docker Model Runner

Verify that the required model is available:

```bash
docker model list
```

Verify the endpoint:

```bash
curl http://localhost:12434/engines/v1/models
```

## Step 4: Build the Application

```bash
mvn clean install
```

## Step 5: Start Spring Boot

```bash
mvn spring-boot:run
```

Application URL:

```text
http://localhost:8097
```

---

# 13. Adding Another LLM Provider

One of the key benefits of this architecture is that additional LLM providers can be integrated without rewriting the controller's existing model interaction logic.

For example, to introduce another provider:

1. Add the corresponding Spring AI starter dependency.
2. Configure the provider's credentials and model properties.
3. Inject its provider-specific `ChatModel`.
4. Register another `ChatClient` bean.
5. Assign a bean name and use `@Qualifier` wherever required.

Example:

```java
@Bean
public ChatClient anotherChatClient(
        AnotherChatModel chatModel,
        ChatClientBuilderConfigurer configurer) {

    return configurer
            .configure(ChatClient.builder(chatModel))
            .build();
}
```

The exact model type and builder method depend on the provider integration.

---

# 14. Local vs Cloud LLM

| Feature           | Docker Model Runner                                | Google Gemini                     |
| ----------------- | -------------------------------------------------- | --------------------------------- |
| Execution         | Local machine                                      | Google infrastructure             |
| API Key           | Not required for local DMR                         | Required                          |
| Internet          | Not required for local inference after model setup | Required                          |
| Model hosting     | Local Docker environment                           | Google                            |
| Inference cost    | Local compute resources                            | Subject to API pricing and quotas |
| Data transmission | Sent to local inference service                    | Sent to Google API                |
| Model selection   | Locally available models                           | Supported Gemini models           |
| ChatClient        | OpenAI-compatible ChatClient                       | Google GenAI ChatClient           |

**Note:** Local execution does not automatically guarantee that all application data remains local. Other application integrations, logging, and external services should also be considered.

---

# 15. Troubleshooting

### Issue 1: Connection Refused

Check whether Docker Model Runner is running:

```bash
docker model status
```

Verify its endpoint:

```bash
curl http://localhost:12434/engines/v1/models
```

### Issue 2: Model Not Found

Check available models:

```bash
docker model list
```

Verify that the configured model identifier matches the model exposed by Docker Model Runner.

### Issue 3: Gemini API Authentication Failure

Verify the environment variable:

```bash
echo $GEMINI_API_KEY
```

Make sure the key is valid and available to the Spring Boot process.

### Issue 4: No Unique ChatClient Bean

When multiple `ChatClient` beans exist, use:

```java
@Primary
```

for the default bean and:

```java
@Qualifier("googleChatClient")
```

for provider-specific injection.

### Issue 5: Missing ChatModel Bean

Verify that the required Spring AI starter dependencies are included and that the corresponding model auto-configuration has not been disabled.

---

# 16. Useful References

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [Spring AI ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
* [Spring AI OpenAI Chat Model](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
* [Spring AI Google GenAI Chat Model](https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html)
* [Docker Model Runner](https://docs.docker.com/ai/model-runner/)
* [Google AI Studio](https://aistudio.google.com/)

---

# 17. Summary

This project demonstrates how to integrate multiple LLM providers into a single Spring Boot application using Spring AI.

The architecture separates model-specific configuration from application-level chat interactions.

Docker Model Runner provides local inference through an OpenAI-compatible API, while Google Gemini provides cloud-based inference through the Google GenAI integration.

By creating independent ChatClient beans, the application can communicate with different LLM providers using a consistent programming model.

**Key takeaway:** Spring AI's ChatClient abstraction allows developers to integrate, configure, and switch between multiple LLM providers while keeping the application-level interaction code largely unchanged.