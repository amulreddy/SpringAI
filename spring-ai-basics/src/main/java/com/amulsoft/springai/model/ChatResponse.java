package com.amulsoft.springai.model;

/**
 * Simple response payload for the non-streaming chat endpoint.
 *
 * @param reply          the assistant's reply text
 * @param conversationId the conversation this reply belongs to
 */
public record ChatResponse(String reply, String conversationId) {
}
