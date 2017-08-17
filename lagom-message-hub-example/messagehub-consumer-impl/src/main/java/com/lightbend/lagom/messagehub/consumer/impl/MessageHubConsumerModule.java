/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.consumer.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.messagehub.consumer.api.MessageHubConsumerService;
import com.lightbend.lagom.messagehub.producer.api.MessageHubProducerService;

/**
 * The module that binds components used by this project for dependency injection by Guice.
 */
public class MessageHubConsumerModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        // Binding a client dynamically generates a proxy implementation of the MessageHubProducerService interface that
        // can be injected into other class constructors.
        //
        // When calling the "sampleTopic" method on this proxy, it returns an implementation of Topic with a subscribe
        // method backed by a Kafka consumer.
        //
        // See https://www.lagomframework.com/documentation/1.4.x/java/ServiceClients.html#Binding-a-service-client
        // for more info on consuming 3rd party services.
        bindClient(MessageHubProducerService.class);

        // The subscriber class is an ordinary Java object that requires the MessageHubProducerService client bound
        // above to be injected into its constructor. It uses this client to subscribe to "sampleTopic"
        //
        // We use "asEagerSingleton" to initialize it at startup (by default, Guice is lazy, and only instantiates
        // each object when it is needed to inject into another one). It begins consuming messages immediately.
        bind(MessageHubSubscriber.class).asEagerSingleton();

        // Binding a service registers it with Lagom and makes it available to serve requests.
        //
        // The MessageHubConsumerService provides a real-time stream of consumed messages over a WebSocket interface.
        //
        // Read more about implementing services at
        // https://www.lagomframework.com/documentation/1.4.x/java/ServiceImplementation.html#Implementing-services
        // and in the source code for MessageHubConsumerService and MessageHubConsumerServiceImpl.
        bindService(MessageHubConsumerService.class, MessageHubConsumerServiceImpl.class);
    }
}
