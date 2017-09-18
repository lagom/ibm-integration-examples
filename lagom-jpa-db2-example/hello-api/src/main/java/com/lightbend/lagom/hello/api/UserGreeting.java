package com.lightbend.lagom.hello.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
@JsonDeserialize
public class UserGreeting {
    public final String id;
    public final String message;

    @JsonCreator
    public UserGreeting(String id, String message) {
        this.id = Preconditions.checkNotNull(id, "id");
        this.message = Preconditions.checkNotNull(message, "message");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGreeting that = (UserGreeting) o;
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
