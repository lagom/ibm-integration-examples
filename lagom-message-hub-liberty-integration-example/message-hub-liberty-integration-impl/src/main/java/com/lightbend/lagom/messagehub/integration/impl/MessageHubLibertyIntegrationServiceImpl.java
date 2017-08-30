/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.integration.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.messagehub.integration.api.Message;
import com.lightbend.lagom.messagehub.integration.api.MessageHubLibertyIntegrationService;
import com.lightbend.lagom.messagehub.integration.impl.persistence.MessageHubPublisherCommand.SendMessage;
import com.lightbend.lagom.messagehub.integration.impl.persistence.MessageHubPublisherEntity;
import com.lightbend.lagom.messagehub.integration.impl.persistence.MessageHubPublisherEvent;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

// This implements the server side of the MessageHubLibertyIntegrationService API.
//
// Read more about implementing services at
// https://www.lagomframework.com/documentation/1.3.x/java/ServiceImplementation.html#Implementing-services
// and in the source code for MessageHubLibertyIntegrationService and MessageHubLibertyIntegrationModule.
@Singleton
public class MessageHubLibertyIntegrationServiceImpl implements MessageHubLibertyIntegrationService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final MessageHubSubscriber subscriber;
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final Materializer materializer;

    @Inject
    public MessageHubLibertyIntegrationServiceImpl(MessageHubSubscriber subscriber,
                                                   PersistentEntityRegistry persistentEntityRegistry,
                                                   Materializer materializer) {
        this.subscriber = subscriber;
        this.persistentEntityRegistry = persistentEntityRegistry;
        this.materializer = materializer;

        // In Lagom, topics are produced by publishing the events from a Persistent Entity.
        // We register the entity class here to make it available to Lagom.
        // It is used below in the `sendMessage(String, String)` method.
        //
        // Read more about persistent entities at
        // https://www.lagomframework.com/documentation/1.3.x/java/PersistentEntity.html
        // and in the source code for MessageHubPublisherEntity
        persistentEntityRegistry.register(MessageHubPublisherEntity.class);
    }

    @Override
    public ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> bidirectionalMessageStream() {
        // Service calls are implemented by returning a lambda that expects the request and returns a CompletionStage
        // that will complete with the response.
        //
        // This service has two independent streams:
        //
        //   - The incoming stream of messages sent from the client, which this service publishes to Kafka
        //   - The outgoing stream of messages consumed from Kafka, which this service sends to the client
        //
        // See https://www.lagomframework.com/documentation/1.3.x/java/ServiceImplementation.html#Working-with-streams
        // for details on how streaming service calls work in Lagom.
        //
        // Also see the comments in MessageHubLibertyIntegrationService for more details on how to run this code.
        return incomingMessageStream -> {
            // First, add a handler for the incoming messages that sends them to a persistent entity with the ID "test".
            incomingMessageStream.runForeach(message -> sendMessage("test", message), materializer);

            // Then return a stream of messages from the internal pub-sub publisher
            return completedFuture(
                    subscriber
                            .messagesReceived()
                            .map(message -> {
                                // log each element as it passes through the stream
                                // at the INFO log level, so we see it on the console
                                log.info("Received message; forwarding to WebSocket client: [{}]", message);

                                // each message is then passed through unchanged
                                return message;
                                // Lagom will write these to the WebSocket stream
                            })
            );
        };
    }

    @Override
    public ServiceCall<String, Done> sendMessage(String id) {
        return message -> sendMessage(id, message);
    }

    @Override
    public Topic<PSequence<Message>> testTopic() {
        return TopicProducer.singleStreamWithOffset(offset ->
                // This produces the topic by reading the stream of all persisted MessageHubPublisherEvent instances,
                // and transforming them to the representation used by the Liberty sample application.
                // Producing topics from entity event streams is key to Lagom's guarantee of at-least-once processing.
                //
                // For more details, see https://www.lagomframework.com/documentation/1.3.x/java/MessageBrokerApi.html
                persistentEntityRegistry
                        .eventStream(MessageHubPublisherEvent.TAG, offset)
                        .map(mapEvent(this::eventMessage))
        );
    }

    private CompletionStage<Done> sendMessage(String entityId, String message) {
        return persistentEntityRegistry
                // Look up the entity by entityId.
                .refFor(MessageHubPublisherEntity.class, entityId)
                // Wrap the message in a `SendMessage` command and send it to the entity.
                // Processing of the command happens asynchronously.
                // This method returns a CompletionStage<Done>, which we can return directly to the caller.
                .ask(new SendMessage(message));
    }

    // The stream returned by `PersistentEntityRegistry.eventStream` provides pairs of events with their offsets.
    // We want to transform each event, while keeping its offset the same, and return new pairs.
    // This is a helper method for wrapping a function that transforms events with a function that handles unzipping
    // and rezipping the pairs.
    private <E, T> Function<Pair<E, Offset>, Pair<T, Offset>> mapEvent(Function<E, T> f) {
        return eventAndOffset -> Pair.create(f.apply(eventAndOffset.first()), eventAndOffset.second());
    }

    // Transforms internal `MessageReceived` persistent entity events into the message list format used by the
    // Liberty sample application.
    private PSequence<Message> eventMessage(MessageHubPublisherEvent event) {
        assert event instanceof MessageHubPublisherEvent.MessageReceived : "Unknown event type: " + event;
        MessageHubPublisherEvent.MessageReceived messageReceived = (MessageHubPublisherEvent.MessageReceived) event;
        String messageValue = messageReceived.getMessage() + ", msgId=" + messageReceived.getMessageId();
        log.info("Publishing: " + messageValue);
        return TreePVector.singleton(
                new Message(messageValue)
        );
    }
}
