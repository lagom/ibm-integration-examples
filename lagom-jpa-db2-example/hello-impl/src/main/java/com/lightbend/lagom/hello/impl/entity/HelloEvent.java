/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.impl.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * This interface defines all the events that the Hello entity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface HelloEvent extends Jsonable, AggregateEvent<HelloEvent> {
    AggregateEventTag<HelloEvent> TAG = AggregateEventTag.of(HelloEvent.class);

    @Override
    default AggregateEventTagger<HelloEvent> aggregateTag() {
        return TAG;
    }

    /**
     * An event that represents a change in greeting message.
     */
    @SuppressWarnings("serial")
    @Immutable
    @JsonDeserialize
    final class GreetingMessageChanged implements HelloEvent {
        public final String id;
        public final String message;

        @JsonCreator
        public GreetingMessageChanged(String id, String message) {
            this.id = Preconditions.checkNotNull(id, "id");
            this.message = Preconditions.checkNotNull(message, "message");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GreetingMessageChanged that = (GreetingMessageChanged) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, message);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("message", message)
                    .toString();
        }
    }
}
