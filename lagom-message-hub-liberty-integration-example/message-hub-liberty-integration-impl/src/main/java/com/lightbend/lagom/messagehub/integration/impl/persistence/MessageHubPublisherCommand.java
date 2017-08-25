package com.lightbend.lagom.messagehub.integration.impl.persistence;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

public interface MessageHubPublisherCommand extends Jsonable {
    final class SendMessage implements MessageHubPublisherCommand, PersistentEntity.ReplyType<Done> {
        private final String message;

        @JsonCreator
        public SendMessage(@JsonProperty("message") String message) {
            this.message = Preconditions.checkNotNull(message);
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SendMessage that = (SendMessage) o;
            return message.equals(that.message);
        }

        @Override
        public int hashCode() {
            return message.hashCode();
        }

        @Override
        public String toString() {
            return "SendMessage{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }
}
