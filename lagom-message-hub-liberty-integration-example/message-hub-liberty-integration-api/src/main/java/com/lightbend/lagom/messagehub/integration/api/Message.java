package com.lightbend.lagom.messagehub.integration.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The format of messages is defined by the {@code MessageList} class in the Liberty sample application:
 * <p>
 * <blockquote>
 * The message list is in the form: [{ "value": base_64_string }, ...]
 * </blockquote>
 */
public final class Message {
    private final String value;

    @JsonCreator
    public Message(@JsonProperty("value") String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(value, message.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Message{" +
                "value='" + value + '\'' +
                '}';
    }
}
