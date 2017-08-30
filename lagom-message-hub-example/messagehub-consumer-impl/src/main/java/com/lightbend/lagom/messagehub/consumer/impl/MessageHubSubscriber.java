/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.consumer.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;
import com.lightbend.lagom.messagehub.producer.api.MessageHubProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Subscribes to the MessageHubProducerService Kafka topic. As each message is received, it's logged at INFO level.
 * By default, this prints to the console and {@code logs/application.log}. Also broadcasts each message to internal
 * subscribers using Lagom's
 * <a href="https://www.lagomframework.com/documentation/1.3.x/java/PubSub.html">distributed publish-subscribe API</a>.
 */
@Singleton
public class MessageHubSubscriber {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final PubSubRegistry pubSub;
    private final TopicId<String> internalTopicId;

    @Inject
    public MessageHubSubscriber(MessageHubProducerService messageHubProducerService, PubSubRegistry pubSub) {
        this.pubSub = pubSub;

        Topic<String> sampleTopic = messageHubProducerService.sampleTopic();
        log.info("Creating subscriber for topic: [{}]", sampleTopic.topicId());
        internalTopicId = TopicId.of(String.class, sampleTopic.topicId().value());

        // Create a subscriber
        sampleTopic.subscribe()
                // The consumer group ID defaults to the service name. This overrides it.
                .withGroupId("kafka-java-console-sample-group-lagom-test")

                // And subscribe to it with at-least-once processing semantics.
                // Messages will be sent through the provided Flow and offsets are committed after processing completes.
                // Commits are batched - this is one reason that a message might be processed more than once.
                .atLeastOnce(messageConsumerFlow());

    }

    // This allows messages to be broadcast to subscribers within the service.
    // Subscribers can be either local (in which case messages will be passed directly to them)
    // or remote (in which case messages will be serialized and sent over the network).
    //
    // There are multiple benefits to this approach:
    //   1. Decouples web consumers from Kafka consumption
    //   2. Avoids proliferation of many per-request Kafka consumers
    //   3. Allows deployment of web servers across multiple clustered nodes that can stream messages from a Kafka
    //      consumer running on a different node.
    //
    // This is a best-effort, at-most-once delivery mechanism. For a more robust approach, persist messages to a
    // database, or use Lagom's persistent entity API.
    Source<String, NotUsed> messagesReceived() {
        return internalTopic().subscriber();
    }

    private Flow<String, Done, NotUsed> messageConsumerFlow() {
        // Create a flow that emits a Done for each message it processes.
        return Flow.<String>create()
                .map(this::processMessage); // send each message to processMessage
    }

    // Ths processes each message synchronously, but it's also possible
    // to create flows that process batches of messages in parallel.
    private Done processMessage(String message) {
        log.info("Message consumed: [{}]", message);
        publishMessageInternally(message);
        return Done.getInstance();
    }

    private void publishMessageInternally(String message) {
        log.info("Broadcasting message to internal subscribers: [{}]", message);
        internalTopic().publish(message);
    }

    private PubSubRef<String> internalTopic() {
        return pubSub.refFor(internalTopicId);
    }
}
