# Lagom integration with IBM Message Hub

[IBM Message Hub](https://www.ibm.com/software/products/en/ibm-message-hub) is a fully-managed Apache Kafka service running on the IBM Bluemix PaaS. It exposes a native Kafka interface, so Lagom services can communicate with it using the standard Lagom Message Broker API.

This project demonstrates a simple consumer service, designed to consume messages produced by the [IBM Message Hub Kafka Java console sample application producer](https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-console-sample).

The Lagom consumer service also exposes a streaming service call, which allows WebSocket clients to receive broadcasts of the consumed messages.

## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- [Message Hub Service Instance](https://console.ng.bluemix.net/catalog/services/message-hub/) provisioned in [IBM Bluemix](https://console.ng.bluemix.net/)
- [IBM Message Hub Kafka Java console sample application](https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-console-sample)

Once you have a Message Hub Service instance provisioned in Bluemix, the main steps to run this example are:

1.  [Gather Message Hub credentials](#gather-message-hub-credentials) that are needed for the producer and consumer to authenticate with Message Hub
2.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
3.  [Start the console producer](#start-the-console-producer)
4.  [Start the Lagom consumer service](#start-the-lagom-consumer-service)
5.  [Monitor the stream of consumed messages](#monitor-the-stream-of-consumed-messages)
6.  [Stop the Lagom consumer and console producer](#stop-the-lagom-consumer-and-console-producer) when finished

## Gather Message Hub credentials

1.  Log in to the [IBM Bluemix console](https://console.ng.bluemix.net/).
2.  Navigate to the Message Hub service you have created.
3.  Navigate to "Service credentials".
4.  Create new credentials if needed (no special parameters are required).
5.  Click "View credentials".
6.  Copy the following credential values to use in the console producer and consumer service:
    - `"api_key"`
    - `"kafka_admin_url"`
    - `"kafka_brokers_sasl"` — **Note:** both the Lagom service and the console sample application require the list of brokers to be formatted as a single-line, comma-separated list of hostname:port pairs. For example: `"host1:port1,host2:port2"`.
    - `"user"`
    - `"password"`


## Download and set up the Lagom service

Follow these steps to get a local copy of this project and configure it with the Message Hub credentials you saved in the previous step. You can supply the credentials in a configuration file or as environment variables.

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/typesafehub/lagom-ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-message-hub-example
    ```
3.  To supply the configuration, do one of the following:
    1. Open the `message-hub-consumer-impl/src/main/resources/message-hub.conf` file in a text editor and fill in the empty values of the `brokers`, `user` and `password` properties from the credentials retrieved above.
        - You don't need to change the duplicate lines (with the form `brokers  = ${?KAFKA_BROKERS}`) beneath each property—these allow the values to be overridden by environment variables (see below) and are ignored if the environment variables are not set.
        - Be sure not to commit this file with your credentials in it.
    2. If you prefer not to enter credentials into the file, you can also set them as environment variables named `KAFKA_BROKERS`, `KAFKA_USER`, and `KAFKA_PASSWORD`.

## Start the console producer

Open a second command line shell and follow the [instructions to build and run the console sample producer](https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-console-sample#running-the-build-script).

For example:

```
java -jar build/libs/kafka-java-console-sample-2.0.jar "kafka02-prod01.messagehub.services.us-south.bluemix.net:9093,kafka04-prod01.messagehub.services.us-south.bluemix.net:9093,kafka01-prod01.messagehub.services.us-south.bluemix.net:9093,kafka05-prod01.messagehub.services.us-south.bluemix.net:9093,kafka03-prod01.messagehub.services.us-south.bluemix.net:9093" "https://kafka-admin-prod01.messagehub.services.us-south.bluemix.net:443" "<api_key>" -producer
```

After a few seconds, you should see messages logged to the console periodically:

```
[2017-08-09 15:23:44,339] INFO class com.messagehub.samples.ProducerRunnable is starting. (com.messagehub.samples.ProducerRunnable)
[2017-08-09 15:23:47,023] INFO Message produced, offset: 0 (com.messagehub.samples.ProducerRunnable)
[2017-08-09 15:23:49,369] INFO Message produced, offset: 1 (com.messagehub.samples.ProducerRunnable)
[2017-08-09 15:23:51,692] INFO Message produced, offset: 2 (com.messagehub.samples.ProducerRunnable)
[2017-08-09 15:23:54,008] INFO Message produced, offset: 3 (com.messagehub.samples.ProducerRunnable)
```

Leave the console producer running in the background as you complete the following steps.

## Start the Lagom consumer service

In the command line shell where you downloaded the Lagom service, from the `lagom-message-hub-example` directory, start the Lagom development environment by running:

```
mvn lagom:runAll
```

You should see some console output, including these lines:

```
...
[INFO] Service gateway is running at http://localhost:9000
...
[INFO] Service message-hub-consumer-impl listening for HTTP on 0:0:0:0:0:0:0:0:60025
[INFO] (Service started, press enter to stop and go back to the console...)
```

These messages indicate that the service has started correctly.

The Lagom consumer service will also begin logging messages to the console as they are consumed from Message Hub. It should quickly catch up to the messages already produced and then continue logging messages as they are produced.

```
16:29:48.804 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Message consumed: [This is a test message #0]
16:29:48.804 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Broadcasting message to internal subscribers: [This is a test message #0]
16:29:48.807 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Message consumed: [This is a test message #1]
16:29:48.807 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Broadcasting message to internal subscribers: [This is a test message #1]
16:29:48.808 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Message consumed: [This is a test message #2]
16:29:48.808 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Broadcasting message to internal subscribers: [This is a test message #2]
16:29:48.808 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Message consumed: [This is a test message #3]
16:29:48.808 [info] com.lightbend.lagom.messagehub.consumer.impl.MessageHubSubscriber [] - Broadcasting message to internal subscribers: [This is a test message #3]
...
```


## Monitor the stream of consumed messages

From a WebSocket client, you can monitor the stream of messages that the Lagom service is consuming by connecting to the service URI as follows:

1.  Go to https://www.websocket.org/echo.html.
2.  In the **Location:** field, enter "`ws://localhost:9000/message-hub-consumer`".
3.  Click **Connect**.
4.  You should begin seeing messages appear in the **Log** panel:
    ```
    CONNECTED

    RECEIVED: This is a test message #237

    RECEIVED: This is a test message #238

    RECEIVED: This is a test message #239
    ```

## Stop the Lagom consumer and console producer

To stop running the examples:

1.  Press "Control-C" in the the console running the console producer to stop it.
2.  Press "Enter" in the console running the Lagom development environment to stop the service.

