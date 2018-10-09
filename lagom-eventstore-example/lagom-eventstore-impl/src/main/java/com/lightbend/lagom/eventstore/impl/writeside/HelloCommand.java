/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.writeside;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

// This interface identifies all commands a HelloEntity is able to process.
// It extends Jsonable to the Lagom cluster can serialize a command sent from one node of
// the cluster to another node using a JSON format.
public interface HelloCommand extends Jsonable {

    enum Greet implements HelloCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        INSTANCE
    }

}
