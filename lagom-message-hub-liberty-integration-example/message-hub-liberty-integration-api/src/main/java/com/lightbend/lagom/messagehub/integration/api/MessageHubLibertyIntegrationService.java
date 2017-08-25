/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.messagehub.integration.api;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import org.pcollections.PSequence;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * API of the Lagom service that integrates with the Message Hub Liberty sample application.
 * <p>
 * This provides three interfaces to clients:
 * </p>
 * <ol>
 * <li>A Kafka topic, exactly matching the one used by the Liberty sample application.</li>
 * <li>An HTTP endpoint that allows you to {@code POST} messages to publish to the topic.</li>
 * <li>A bidirectional WebSocket interface that publishes messages sent from a client to the topic, and broadcasts
 * messages consumed from the topic to all clients.</li>
 * </ol>
 *
 * @see <a href="https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-liberty-sample">IBM Message
 * Hub Kafka Liberty sample application</a>
 */
public interface MessageHubLibertyIntegrationService extends Service {
    /**
     * Streams messages between IBM Message Hub and clients. This is an "at most once" transport. No measures are taken
     * to ensure that messages are received or processed by downstream clients.
     * <p>
     * You can test this with any WebSocket client, but an easy to use one is available from WebSocket.org:
     * </p>
     * <ul>
     * <li>Ensure that both this service and the kafka-java-liberty-sample producer are running</li>
     * <li>Go to <a href="https://www.websocket.org/echo.html">https://www.websocket.org/echo.html</a></li>
     * <li>Enter <code>ws://localhost:9000/messages</code> into the <b>Location:</b> field</li>
     * <li>Click <b>Connect</b></li>
     * <li>In another browser window or tab, navigate to the URL of the Liberty application, and click the
     * <b>Produce a Message</b> button.</li>
     * <li>Return to the WebSocket Echo Test tab in your browser.</li>
     * <li>Within a few seconds, you should see the message produced from the Liberty application in the <b>Log</b>
     * panel.</li>
     * <li>Enter a message into the <b>Message</b> field and click the <b>Send</b> button.</li>
     * <li>Within a few seconds, you should see the message you sent repeated in the <b>Log</b> panel.</li>
     * <li>Return to the Liberty application tab in your browser, and reload the page.</li>
     * <li>You should see the message you sent in the list of <b>Already consumed messages</b>.</li>
     * </ul>
     */
    ServiceCall<Source<String, NotUsed>, Source<String, NotUsed>> bidirectionalMessageStream();

    /**
     * Sends messages to the shared topic in IBM Message Hub.
     * <p>
     * You can test this using an ordinary HTTP client, such as {@code curl} or
     * <a href="https://www.getpostman.com/">Postman</a>.
     * </p>
     * <p>Example {@code curl} command:</p>
     * {@code curl -H "Content-Type: text/plain" -X POST -d "This is a test message from Lagom" http://localhost:9000/sendMessage/test}
     * <p>
     * A few seconds after this command completes, you can reload the Liberty sample application page. You should see
     * the message in the list of <b>Already consumed messages</b>.
     * </p>
     *
     * @param id the ID of the internal Lagom entity to send the message to, for example "test".
     *           The value is not important for this example.
     */
    ServiceCall<String, Done> sendMessage(String id);

    /**
     * Represents the topic that the IBM Message Hub Liberty sample application produces.
     * <p>
     * The format of messages is defined by the {@code MessageList} class in the Liberty sample application:
     * </p>
     * <blockquote>
     * The message list is in the form: [{ "value": base_64_string }, ...]
     * </blockquote>
     * <p>
     * Lagom can automatically deserialize JSON arrays into {@code PSequence} instances &mdash; immutable lists provided
     * by the <a href="https://pcollections.org/">PCollections</a> library. Each JSON object within the array is
     * deserialized into a {@link Message} instance.
     * </p>
     *
     * @return a topic producing lists of messages
     */
    Topic<PSequence<Message>> testTopic();

    @Override
    default Descriptor descriptor() {
        return named("message-hub-liberty-integration-service")
                .withCalls(
                        pathCall("/messages", this::bidirectionalMessageStream),
                        pathCall("/sendMessage/:id", this::sendMessage)
                )
                .withTopics(
                        // the topic name must match the one used in the Liberty sample application
                        topic("testTopic", this::testTopic)
                )
                // withAutoAcl(true) makes all calls available for proxying from the service gateway,
                // in addition to being available by connecting directly to the back-end service port
                .withAutoAcl(true);
    }
}
