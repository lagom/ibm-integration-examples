package com.lightbend.lagom.messagehub.producer.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.topic;

/**
 * Models the API used by IBM's Message Hub Java console sample application. Consumers use the API description to build
 * type-safe clients. Lagom automatically handles building the Kafka consumer, deserializing the messages, and
 * committing offsets. It exposes an Akka Streams {@link akka.stream.javadsl.Flow} API to user code.
 * <p>
 * Note: this assumes the producer is launched with the default topic name ({@code "kafka-java-console-sample-topic"}).
 * </p>
 *
 * @see <a href="https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-console-sample">IBM Message
 * Hub Kafka Java console sample application</a>
 */
public interface MessageHubProducerService extends Service {
    /**
     * Represents the topic that the IBM sample application produces. It contains simple string messages of the form:
     * {@code "This is a test message #" + producedMessages}.
     *
     * @return a topic producing simple string messages
     */
    Topic<String> sampleTopic();

    @Override
    default Descriptor descriptor() {
        return named("message-hub-producer")
                .withTopics(
                        topic("kafka-java-console-sample-topic", this::sampleTopic)
                );
    }
}
