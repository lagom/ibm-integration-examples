package com.lightbend.lagom.messagehub.integration.impl.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;

class MessageHubPublisherState implements Jsonable {
    static final MessageHubPublisherState INITIAL = new MessageHubPublisherState(0);

    private final int nextMessageId;

    @JsonCreator
    MessageHubPublisherState(@JsonProperty("nextMessageId") int nextMessageId) {
        this.nextMessageId = nextMessageId;
    }

    MessageHubPublisherEvent.MessageReceived sendMessage(String message) {
        return new MessageHubPublisherEvent.MessageReceived(nextMessageId, message);
    }

    MessageHubPublisherState nextState() {
        return new MessageHubPublisherState(nextMessageId + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageHubPublisherState that = (MessageHubPublisherState) o;
        return nextMessageId == that.nextMessageId;
    }

    @Override
    public int hashCode() {
        return nextMessageId;
    }

    @Override
    public String toString() {
        return "MessageHubPublisherState{" +
                "nextMessageId=" + nextMessageId +
                '}';
    }
}
