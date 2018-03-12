/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.consumer.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.namedCall;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

/**
 * API of a service allowing WebSocket clients to consume a real-time stream of messages as they are received from
 * Message Hub.
 */
public interface MessageHubConsumerService extends Service {

    /**
     * Streams messages from IBM Message Hub to clients. This is an "at most once" transport. No measures are taken to
     * ensure that messages are received or processed by downstream clients. This makes it useful mostly as a real-time
     * monitor of messages that are consumed.
     * <p>
     * You can test this with any WebSocket client, but an easy to use one is available from WebSocket.org:
     * </p>
     * <ul>
     * <li>Ensure that both this service and the kafka-java-console-sample producer are running</li>
     * <li>Go to <a href="http://www.websocket.org/echo.html">http://www.websocket.org/echo.html</a></li>
     * <li>Enter <code>ws://localhost:9000/message-hub-consumer</code> into the <b>Location:</b> field</li>
     * <li>Click <b>Connect</b></li>
     * </ul>
     * <p>
     * You should see messages start to appear in the <b>Log</b>.
     * </p>
     */
    ServiceCall<NotUsed, Source<String, NotUsed>> liveMessageStream();

    @Override
    default Descriptor descriptor() {
        return named("message-hub-consumer-service")
                .withCalls(
                        namedCall("message-hub-consumer", this::liveMessageStream)
                )
                // withAutoAcl(true) makes all calls available for proxying from the service gateway,
                // in addition to being available by connecting directly to the back-end service port
                .withAutoAcl(true);
    }
}
