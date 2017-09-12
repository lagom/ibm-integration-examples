/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.writeside;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

public interface HelloEvent extends AggregateEvent<HelloEvent>, Jsonable {

    AggregateEventShards<HelloEvent> HELLO_EVENT_TAG = AggregateEventTag.sharded(HelloEvent.class, 10);

    @Override
    default AggregateEventShards<HelloEvent> aggregateTag() {
        return HELLO_EVENT_TAG;
    }


    @SuppressWarnings("serial")
    @JsonDeserialize
    final class Greeted implements HelloEvent {
        public final String name;
        private long instant;

        @JsonCreator
        public Greeted(String name, long instant) {
            this.name = name;
            this.instant = instant;
        }

        public String getName() {
            return name;
        }

        public long getInstant() {
            return instant;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Greeted greeted = (Greeted) o;

            if (instant != greeted.instant) return false;
            return name != null ? name.equals(greeted.name) : greeted.name == null;
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (int) (instant ^ (instant >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Greeted{" +
                    "name='" + name + '\'' +
                    ", instant=" + instant +
                    '}';
        }
    }
}
