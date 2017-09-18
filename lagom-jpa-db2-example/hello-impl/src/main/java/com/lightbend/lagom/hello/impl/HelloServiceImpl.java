/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.hello.api.GreetingMessage;
import com.lightbend.lagom.hello.api.HelloService;
import com.lightbend.lagom.hello.api.UserGreeting;
import com.lightbend.lagom.hello.impl.entity.HelloCommand;
import com.lightbend.lagom.hello.impl.entity.HelloCommand.Hello;
import com.lightbend.lagom.hello.impl.entity.HelloCommand.UseGreetingMessage;
import com.lightbend.lagom.hello.impl.entity.HelloEntity;
import com.lightbend.lagom.hello.impl.readside.Greetings;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implementation of the HelloService.
 */
public class HelloServiceImpl implements HelloService {
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final Greetings greetings;

    @Inject
    public HelloServiceImpl(PersistentEntityRegistry persistentEntityRegistry, Greetings greetings) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        this.greetings = greetings;
        persistentEntityRegistry.register(HelloEntity.class);
    }

    @Override
    public ServiceCall<NotUsed, String> hello(String id) {
        return request -> {
            // Look up the hello world entity for the given ID.
            PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
            // Ask the entity the Hello command.
            return ref.ask(new Hello(id, Optional.empty()));
        };
    }

    @Override
    public ServiceCall<GreetingMessage, Done> useGreeting(String id) {
        return request -> {
            // Look up the hello world entity for the given ID.
            PersistentEntityRef<HelloCommand> ref = persistentEntityRegistry.refFor(HelloEntity.class, id);
            // Tell the entity to use the greeting message specified.
            return ref.ask(new UseGreetingMessage(request.message));
        };

    }

    @Override
    public ServiceCall<NotUsed, PSequence<UserGreeting>> allGreetings() {
        return request -> greetings.all();
    }
}
