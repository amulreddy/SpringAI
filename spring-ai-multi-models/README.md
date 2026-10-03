# Spring AI Chatbot (Local Ollama)

A minimal chatbot built with **Spring Boot 3.5** and **Spring AI 2.0**, backed by a
**local Ollama model** (`llama3.2:3b`). No cloud APIs, no API keys — everything runs on your machine.

## Architecture

```
Browser (static/index.html)
        │  POST /api/chat/stream  (Server-Sent Events)
        ▼
Spring Boot app  ──ChatClient──►  Spring AI Ollama starter
        │                                   │  HTTP
        ▼                                   ▼
  ChatMemory (per conversation)      Ollama server @ localhost:11434
                                            │
                                            ▼
                                     llama3.2:3b (CPU inference)
```

## Prerequisites

- **Java 21** (project targets 21)
- **Maven 3.9+**
- **Ollama** running and reachable at `http://localhost:11434` with the model pulled.

### Ollama via Docker

```bash
docker run -d --name ollama \
  -v ollama_data:/root/.ollama \
  -p 11434:11434 \
  --restart unless-stopped \
  ollama/ollama:latest

docker exec ollama ollama pull llama3.2:3b
```

## Run

```bash
export JAVA_HOME=/usr/lib/jvm/amazon-corretto-21.0.9.11.1-linux-x64   # adjust to your JDK 21
mvn spring-boot:run
```

Then open http://localhost:8080

## API

| Method | Path                | Body                                            | Description                     |
|--------|---------------------|-------------------------------------------------|---------------------------------|
| POST   | `/api/chat`         | `{"message":"hi","conversationId":"abc"}`       | Blocking reply as JSON          |
| POST   | `/api/chat/stream`  | `{"message":"hi","conversationId":"abc"}`       | Token stream (text/event-stream)|

```bash
curl -s http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Say hello in one sentence."}'
```

## Configuration

Edit `src/main/resources/application.yml`:

- `spring.ai.ollama.base-url` — Ollama server URL
- `spring.ai.ollama.chat.options.model` — model name (must be pulled in Ollama)
- `spring.ai.ollama.chat.options.temperature` — creativity vs. focus

## Swapping the model

Pull any Ollama model and update the config, e.g. for a stronger (heavier) model:

```bash
docker exec ollama ollama pull llama3.1:8b
```
```yaml
spring.ai.ollama.chat.options.model: llama3.1:8b
```
