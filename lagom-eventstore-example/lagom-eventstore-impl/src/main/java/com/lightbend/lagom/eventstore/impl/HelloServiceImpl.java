/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl;

import akka.NotUsed;
import com.lightbend.lagom.eventstore.hello.api.HelloService;
import com.lightbend.lagom.eventstore.impl.writeside.HelloCommand;
import com.lightbend.lagom.eventstore.impl.writeside.HelloEntity;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

// A Minimal Lagom Service with an endpoint to send greetings to other users.
// This is a simplified example for demo purposes.
public class HelloServiceImpl implements HelloService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(HelloEntity.class);
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return
                request ->
                        persistentEntityRegistry
                                // get the persistent instance with entityId == 'id'
                                .refFor(HelloEntity.class, id)
                                // send a command to that instance
                                .ask(HelloCommand.GREET_INSTANCE)
                                // ignore the response from the entity and send "Hi" back as the HTTP response
                                .thenApply(ignored -> "Hi!");
    }

}
