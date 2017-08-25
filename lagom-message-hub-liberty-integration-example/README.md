# Lagom integration with IBM Message Hub

[IBM Message Hub](https://www.ibm.com/software/products/en/ibm-message-hub) is a fully-managed Apache Kafka service running on the IBM Bluemix PaaS. It exposes a native Kafka interface, so Lagom services can communicate with it using the standard Lagom Message Broker API.

This project demonstrates a simple service that integrates with the IBM Message Hub Kafka [Liberty sample application](https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-liberty-sample). The project shows how to consume messages produced by the Liberty sample application and how to produce messages that can be consumed by it.

## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- [Message Hub Service Instance](https://console.ng.bluemix.net/catalog/services/message-hub/) provisioned in [IBM Bluemix](https://console.ng.bluemix.net/)
- [IBM Message Hub Kafka Liberty sample application](https://github.com/ibm-messaging/message-hub-samples/tree/master/kafka-java-liberty-sample) deployed to Bluemix

Once you have a Message Hub Service instance and Liberty sample application provisioned in Bluemix, the main steps to run this example are:

1.  [Gather Message Hub credentials](#gather-message-hub-credentials) that are needed for the service to authenticate with Message Hub
2.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
3.  [Start the Lagom service](#start-the-lagom-service)
4.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
5.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
6.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)
6.  [Stop the Lagom service](#stop-the-lagom-service) when finished

## Gather Message Hub credentials

1.  Log in to the [IBM Bluemix console](https://console.ng.bluemix.net/).
2.  Navigate to the Message Hub service you have created.
3.  Navigate to "Service credentials".
4.  Create new credentials if needed (no special parameters are required).
5.  Click "View credentials".
6.  Copy the following credential values to use in the Lagom service:
    - `"kafka_brokers_sasl"` — **Note:** the Lagom service requires the list of brokers to be formatted as a single-line, comma-separated list of hostname:port pairs. For example: `"host1:port1,host2:port2"`.
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
    cd lagom-message-hub-liberty-integration-example
    ```
3.  To supply the configuration, do one of the following:
    1. Open the `message-hub-liberty-integration-impl/src/main/resources/message-hub.conf` file in a text editor and fill in the empty values of the `brokers`, `user` and `password` properties from the credentials retrieved above.
        - You don't need to change the duplicate lines (with the form `brokers  = ${?KAFKA_BROKERS}`) beneath each property—these allow the values to be overridden by environment variables (see below) and are ignored if the environment variables are not set.
        - Be sure not to commit this file with your credentials in it.
    2. If you prefer not to enter credentials into the file, you can also set them as environment variables named `KAFKA_BROKERS`, `KAFKA_USER`, and `KAFKA_PASSWORD`.

## Start the Lagom service

In the command line shell where you downloaded the Lagom service, from the `lagom-message-hub-liberty-integration-example` directory, start the Lagom development environment by running:

```
mvn lagom:runAll
```

You should see some console output, including these lines:

```
...
[INFO] Service gateway is running at http://localhost:9000
...
[INFO] Service message-hub-liberty-integration-impl listening for HTTP on 0:0:0:0:0:0:0:0:51053
[INFO] (Service started, press enter to stop and go back to the console...)
```

These messages indicate that the service has started correctly.

## Connect to the Lagom message stream

From a WebSocket client, you can monitor the stream of messages that the Lagom service is consuming from Message Hub, and send messages to Lagom to produce to Message Hub, by connecting to the service URI as follows:

1.  Go to https://www.websocket.org/echo.html.
2.  In the **Location:** field, enter "`ws://localhost:9000/messages`".
3.  Click **Connect**.

## Test producing a message from the Liberty sample application

1.  In another browser window or tab, navigate to the URL of the Liberty application deployed to Bluemix, and click the **Produce a Message** button.
2.  Return to the WebSocket Echo Test tab in your browser.
3.  Within a few seconds, you should see the message produced from the Liberty application in the **Log** panel.

## Test producing a message from the Lagom service

1.  In the WebSocket Echo Test tab in your browser, enter a message into the **Message** field and click the **Send** button.
2.  Within a few seconds, you should see the message you sent repeated in the **Log** panel.
3.  Return to the Liberty application tab in your browser, and reload the page.
4.  You should see the message you sent in the list of **Already consumed messages**.


## Stop the Lagom service

Press "Enter" in the console running the Lagom development environment to stop the service.
