/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.consumer.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.messagehub.consumer.api.MessageHubConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.concurrent.CompletableFuture.completedFuture;

// This implements the server side of the MessageHubConsumerService API.
//
// Read more about implementing services at
// https://www.lagomframework.com/documentation/1.4.x/java/ServiceImplementation.html#Implementing-services
// and in the source code for MessageHubConsumerService and MessageHubConsumerModule.
@Singleton
public class MessageHubConsumerServiceImpl implements MessageHubConsumerService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageHubSubscriber subscriber;

    @Inject
    public MessageHubConsumerServiceImpl(MessageHubSubscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public ServiceCall<NotUsed, Source<String, NotUsed>> liveMessageStream() {
        // Service calls are implemented by returning a lambda that expects the request and returns a CompletionStage
        // that will complete with the response. In this case, the request is expected to be empty and is not used,
        // and the response will stream messages over a WebSocket as each one is received.
        //
        // See https://www.lagomframework.com/documentation/1.4.x/java/ServiceImplementation.html#Working-with-streams
        // for details on how streaming service calls work in Lagom.
        //
        // Also see the comments in MessageHubConsumerService for more details on how to run this code.
        return _request -> completedFuture(
                subscriber
                        // stream of messages from the internal pub-sub publisher
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
    }
}
