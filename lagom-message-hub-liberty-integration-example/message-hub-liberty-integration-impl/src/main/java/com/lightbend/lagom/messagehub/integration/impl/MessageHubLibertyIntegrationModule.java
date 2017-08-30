/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.integration.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.messagehub.integration.api.MessageHubLibertyIntegrationService;

/**
 * The module that binds components used by this project for dependency injection by Guice.
 */
public class MessageHubLibertyIntegrationModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        // Binding a service registers it with Lagom and makes it available to serve requests. It also makes a client
        // available for injecting into other components of this service.
        //
        // Read more about implementing services at
        // https://www.lagomframework.com/documentation/1.3.x/java/ServiceImplementation.html#Implementing-services
        // and in the source code for MessageHubLibertyIntegrationService and MessageHubLibertyIntegrationServiceImpl.
        bindService(MessageHubLibertyIntegrationService.class, MessageHubLibertyIntegrationServiceImpl.class);

        // The subscriber class is an ordinary Java object that requires the MessageHubLibertyIntegrationService client
        // bound above to be injected into its constructor. It uses this client to subscribe to "testTopic" - the same
        // topic name that is used by the Liberty sample app.
        //
        // We use "asEagerSingleton" to initialize it at startup (by default, Guice is lazy, and only instantiates
        // each object when it is needed to inject into another one). It begins consuming messages immediately.
        bind(MessageHubSubscriber.class).asEagerSingleton();

    }
}
