/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

public interface HelloEvent extends Jsonable {

    @SuppressWarnings("serial")
    @JsonDeserialize
    final class Greeted implements HelloEvent {
        public final String name;

        @JsonCreator
        public Greeted(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Greeted greeted = (Greeted) o;

            return name != null ? name.equals(greeted.name) : greeted.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Greeted{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
