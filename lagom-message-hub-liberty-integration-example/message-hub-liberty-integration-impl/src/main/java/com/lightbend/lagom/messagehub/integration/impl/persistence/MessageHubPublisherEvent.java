package com.lightbend.lagom.messagehub.integration.impl.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;

import java.util.Objects;

public interface MessageHubPublisherEvent extends Jsonable, AggregateEvent<MessageHubPublisherEvent> {
    AggregateEventTag<MessageHubPublisherEvent> TAG = AggregateEventTag.of(MessageHubPublisherEvent.class);

    @Override
    default AggregateEventTagger<MessageHubPublisherEvent> aggregateTag() {
        return TAG;
    }

    final class MessageReceived implements MessageHubPublisherEvent {
        private final int messageId;
        private final String message;

        @JsonCreator
        MessageReceived(@JsonProperty("messageId") int messageId, @JsonProperty("message") String message) {
            this.messageId = messageId;
            this.message = Preconditions.checkNotNull(message);
        }

        public int getMessageId() {
            return messageId;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageReceived that = (MessageReceived) o;
            return messageId == that.messageId && message.equals(that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, message);
        }

        @Override
        public String toString() {
            return "MessageReceived{" +
                    "messageId=" + messageId +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
