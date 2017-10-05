/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.eventstore.hello.api.HelloService;
import com.lightbend.lagom.eventstore.impl.readside.EventStoreRepositoryImpl;
import com.lightbend.lagom.eventstore.impl.readside.GreetingsRepository;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class LagomEventStoreModule extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        // Bind an implementation of the Lagom API to the API.
        bindService(HelloService.class, HelloServiceImpl.class);
        // Eagerly start a GreetingsRepository. The GreetingsRepository provides access to
        // an external, alternative storage (in this case IBM Project EventStore) so write-side
        // events are processed, converted into  the read-side model and the stored.
        bind(GreetingsRepository.class).asEagerSingleton();
        bind(EventStoreRepositoryImpl.class).asEagerSingleton();
    }

}
