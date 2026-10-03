# Spring AI Chat Application with Docker Model Runner

A minimal, locally hosted AI Chat Application with Spring AI basic concepts built using **Spring Boot 4.0.**, **Spring AI 2.0**, and **Docker Model Runner (DMR)**.

The application uses a locally running Large Language Model (LLM) through an OpenAI-compatible API. No cloud AI APIs, external API keys, or paid inference services are required.

The chatbot supports:

* Synchronous chat responses.
* Real-time token streaming using Server-Sent Events (SSE).
* Local LLM inference using Docker Model Runner.
* Configurable model and inference parameters.
* A simple browser-based chat interface.

---

## 1. Architecture

```text
                    Browser
               (static/index.html)
                       |
                       |
              POST /api/chat/stream
                 (SSE Streaming)
                       |
                       ▼
              Spring Boot 4.0.0
                       |
                       ▼
                  ChatClient
                  (Spring AI)
                       |
                       ▼
              OpenAI Chat Model
                       |
                       ▼
             HTTP / OpenAI API
                       |
                       ▼
             Docker Model Runner
              localhost:12434
                       |
                       ▼
                Local LLM
                       |
                       ▼
               CPU / GPU Inference
```

### Architecture Overview

| Component           | Responsibility                                   |
| ------------------- | ------------------------------------------------ |
| Browser             | Provides the chatbot user interface              |
| Spring Boot         | Exposes REST APIs                                |
| Spring AI           | Provides the ChatClient abstraction              |
| OpenAI Starter      | Communicates with the OpenAI-compatible endpoint |
| Docker Model Runner | Hosts and executes the local LLM                 |
| Local LLM           | Generates chatbot responses                      |

**Note:** Docker Model Runner supports OpenAI-compatible APIs, allowing Spring AI to communicate with local models without requiring a cloud OpenAI account.

---

## 2. Technology Stack

| Technology          | Version / Description |
| ------------------- | --------------------- |
| Java                | 25                    |
| Spring Boot         | 4.0.0                 |
| Spring AI           | 2.0.x                 |
| Maven               | 3.9+                  |
| Docker Model Runner | Local LLM inference   |
| Model               | Configurable          |
| Frontend            | HTML, CSS, JavaScript |
| Communication       | REST API / SSE        |

---

## 3. Prerequisites

Make sure the following are installed:

* Java 25
* Maven 3.9+
* Docker Desktop or Docker Engine with Docker Model Runner support
* Sufficient system RAM or GPU memory for the selected model

Verify Java and Maven:

```bash
java -version
mvn -version
docker --version
```

---

## 4. Docker Model Runner Setup

Docker Model Runner allows you to run LLM models locally and expose them through an API compatible with OpenAI.

Unlike traditional cloud-based LLM integrations, the inference happens on your local machine.

### Step 1: Enable Docker Model Runner

#### Option A: Docker Desktop

1. Open Docker Desktop.
2. Navigate to **Settings → AI**.
3. Enable Docker Model Runner.
4. Enable TCP access for host applications.

Alternatively, execute:

```bash
docker desktop enable model-runner --tcp 12434
```

#### Option B: Docker Engine on Ubuntu

Install and configure Docker Model Runner according to the official Docker documentation.

Docker Engine exposes the Model Runner API through TCP port `12434`.

Official documentation:

* [Docker Model Runner](https://docs.docker.com/ai/model-runner/)
* [Docker Model Runner API Reference](https://docs.docker.com/ai/model-runner/api-reference/)

### Step 2: Verify Docker Model Runner

Check the Model Runner status:

```bash
docker model status
```

Verify that the API is accessible:

```bash
curl http://localhost:12434/engines/v1/models
```

Expected result: A JSON response containing the available models.

### Step 3: Pull an LLM Model

Pull the model you want to use.

For example:

```bash
docker model pull ai/llama3.2
```

Check available models:

```bash
docker model list
```

**Important:** Docker Model Runner uses model identifiers such as `ai/llama3.2`. This is different from Ollama model identifiers such as `llama3.2:3b`.

Use the exact model identifier returned by Docker Model Runner.

### Step 4: Test the Model Independently

Before integrating it with Spring Boot, test the API directly.

```bash
curl http://localhost:12434/engines/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "ai/llama3.2",
    "messages": [
      {
        "role": "user",
        "content": "Say hello in one sentence."
      }
    ],
    "stream": false
  }'
```

If you receive a generated response, your local LLM is ready for integration.

### Step 5: Test Streaming

Docker Model Runner also supports streaming responses.

```bash
curl -N http://localhost:12434/engines/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "ai/llama3.2",
    "messages": [
      {
        "role": "user",
        "content": "Explain Spring AI in simple terms."
      }
    ],
    "stream": true
  }'
```

---

## 5. Spring AI Configuration

Spring AI uses its OpenAI integration to communicate with Docker Model Runner.

Although the OpenAI starter is used, no external OpenAI service is involved.

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: spring-ai-local-chatbot

  ai:
    model:
      embedding: none

    openai:
      base-url: http://localhost:12434
      api-key: not-needed

      chat:
        options:
          model: ai/llama3.2
          temperature: 0.7
```

### Configuration Explanation

| Property                              | Description                                        |
| ------------------------------------- | -------------------------------------------------- |
| `spring.ai.openai.base-url`           | Docker Model Runner URL                            |
| `spring.ai.openai.api-key`            | Placeholder value; DMR does not require an API key |
| `spring.ai.openai.chat.options.model` | Local model identifier                             |
| `temperature`                         | Controls response variability                      |
| `spring.ai.model.embedding`           | Disables embedding auto-configuration              |

**Important:** The OpenAI-compatible base URL for Docker Model Runner must point to the host endpoint. Spring AI constructs the API paths using the configured base URL.

For this setup:

```text
Base URL:
http://localhost:12434

API endpoint:
http://localhost:12434/engines/v1/chat/completions
```

---

## 6. Maven Dependencies

Add the following dependencies to your `pom.xml`.

```xml
<properties>
    <java.version>21</java.version>
    <spring-ai.version>2.0.0</spring-ai.version>
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

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring AI OpenAI Integration -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

</dependencies>
```

Ensure that the Spring Boot parent version is configured as `3.5.x`.

---

## 7. API Endpoints

| Method | Endpoint           | Request | Description                 |
| ------ | ------------------ | ------- | --------------------------- |
| POST   | `/api/chat`        | JSON    | Returns a complete response |
| POST   | `/api/chat/stream` | JSON    | Streams response using SSE  |

### Request

```json
{
  "message": "Explain Spring AI",
  "conversationId": "abc"
}
```

### Blocking Chat API

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Say hello in one sentence."
  }'
```

### Streaming Chat API

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain Docker Model Runner."
  }'
```

---

## 8. Run the Application

### Configure Java 21

For Ubuntu:

```bash
export JAVA_HOME=/usr/lib/jvm/amazon-corretto-21.0.9.11.1-linux-x64

export PATH=$JAVA_HOME/bin:$PATH
```

Verify:

```bash
java -version
```

### Start Spring Boot

Make sure Docker Model Runner is running and the model is available.

Execute:

```bash
mvn clean install
```

Start the application:

```bash
mvn spring-boot:run
```

Open the chatbot:

http://localhost:8080

---

## 9. Swapping the LLM Model

One of the benefits of using Spring AI is that you can change the model through configuration without changing the ChatClient implementation.

For example, to use another Docker Model Runner model:

```bash
docker model pull ai/qwen2.5-coder
```

Update `application.yml`:

```yaml
spring:
  ai:
    openai:
      chat:
        options:
          model: ai/qwen2.5-coder
```

Restart the Spring Boot application.

The application will now use the newly configured model.

---

## 10. Docker Model Runner vs Ollama

Both Docker Model Runner and Ollama allow you to run LLMs locally.

However, their setup and API configuration are different.

| Feature               | Docker Model Runner | Ollama            |
| --------------------- | ------------------- | ----------------- |
| Runtime               | Docker Model Runner | Ollama            |
| Default endpoint      | `localhost:12434`   | `localhost:11434` |
| API integration       | OpenAI-compatible   | Ollama-compatible |
| Spring AI integration | OpenAI starter      | Ollama starter    |
| Model identifier      | `ai/llama3.2`       | `llama3.2:3b`     |
| API key               | Not required        | Not required      |

### Alternative: Running Ollama with Docker

If you prefer Ollama instead of Docker Model Runner, use:

```bash
docker run -d \
  --name ollama \
  -v ollama_data:/root/.ollama \
  -p 11434:11434 \
  --restart unless-stopped \
  ollama/ollama:latest
```

Pull the model:

```bash
docker exec ollama ollama pull llama3.2:3b
```

Configure Spring AI:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2:3b
          temperature: 0.7
```

For this configuration, use the Spring AI Ollama starter instead of the OpenAI starter.

---

## 11. Troubleshooting

### Connection Refused

Verify Docker Model Runner:

```bash
docker model status
```

Check the API:

```bash
curl http://localhost:12434/engines/v1/models
```

### Model Not Found

Check the available models:

```bash
docker model list
```

Ensure the configured model name exactly matches the available model identifier.

### Slow Responses

Model inference performance depends on:

* Model size and quantization.
* Available RAM and GPU memory.
* CPU/GPU acceleration.
* Context window configuration.

For initial testing, start with a smaller model.

### Port Already in Use

Check port `12434`:

```bash
sudo lsof -i :12434
```

---

## 12. Important References

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [Spring AI OpenAI Chat Model](https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html)
* [Docker Model Runner Documentation](https://docs.docker.com/ai/model-runner/)
* [Docker Model Runner API Reference](https://docs.docker.com/ai/model-runner/api-reference/)

---

## 13. Summary

This project demonstrates how to build a local AI chatbot using Spring Boot and Spring AI without depending on external cloud-based LLM APIs.

Docker Model Runner provides the local model inference infrastructure, while Spring AI provides a consistent application-level interface through `ChatClient`.

The model can be replaced through configuration, making the application flexible for experimenting with different local LLMs.
