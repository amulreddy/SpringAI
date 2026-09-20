package com.amulsoft.springai.model;

/**
 * Incoming chat request payload.
 *
 * @param message        the user's message (required)
 * @param conversationId optional id to group messages into one conversation;
 *                       when omitted, a default conversation is used
 */
public record ChatRequest(String message, String conversationId) {

    public String conversationIdOrDefault() {
        return (conversationId == null || conversationId.isBlank()) ? "default" : conversationId;
    }
}
