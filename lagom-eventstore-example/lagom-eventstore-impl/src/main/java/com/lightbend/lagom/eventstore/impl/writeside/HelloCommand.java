/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.writeside;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

// This inteface identifies all commands a HelloEntity is able to prcess.
// It extends Jsonable to the Lagom cluster can serialize a command sent from one node of
// the cluster to another node using a JSON format.
public interface HelloCommand extends Jsonable {

    Greet GREET_INSTANCE = new HelloCommand.Greet();

    @SuppressWarnings("serial")
    @JsonDeserialize
    final class Greet implements HelloCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        @JsonCreator
        private Greet(){
        }
    }

}
