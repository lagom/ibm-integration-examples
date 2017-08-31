# Run the Lagom Message Hub Liberty integration example in development mode

Lagom provides a highly productive development environment, which automatically reloads running services whenever you make changes to the source code.

This guide demonstrates how to start the Lagom development environment and test its integration with IBM Message Hub and the Message Hub Liberty sample application.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Start the Lagom service](#start-the-lagom-service)
3.  [Test the Lagom service](#test-the-lagom-service)
    1.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
    2.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
    3.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)
4.  [Stop the Lagom service](#stop-the-lagom-service)
5.  [Next steps](#next-steps)

## Prerequisites

Before performing the following steps, follow the instructions in [`README.md`](../README.md).

If this is not the first time you have run the Lagom Message Hub Liberty integration example service, ensure that you have stopped all other running copies that are configured with the same Message Hub service. Only one instance of the Lagom service can read from your sample application topic in the Message Hub service at one time, due to the way Kafka assigns partitions to consumers. In a realistic production application, you can create partitioned topics to allow multiple instances of a consumer to balance the load of processing a topic. See the [Kafka documentation](http://kafka.apache.org/documentation/) for detailed information on how topic partitions are assigned to consumers.

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

## Test the Lagom service

You can test the running Lagom service by following these three steps:

1.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
2.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
3.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)

### Connect to the Lagom message stream

From a WebSocket client, you can monitor the stream of messages that the Lagom service is consuming from Message Hub, and send messages to Lagom to produce to Message Hub, by connecting to the service URI as follows:

1.  Go to https://www.websocket.org/echo.html.
2.  In the **Location:** field, enter "`ws://localhost:9000/messages`".
3.  Click **Connect**.

### Test producing a message from the Liberty sample application

1.  In another browser window or tab, navigate to the URL of the Liberty application deployed to Bluemix, and click the **Produce a Message** button.
2.  Return to the WebSocket Echo Test tab in your browser.
3.  Within a few seconds, you should see the message produced from the Liberty application in the **Log** panel.

### Test producing a message from the Lagom service

1.  In the WebSocket Echo Test tab in your browser, enter a message into the **Message** field and click the **Send** button.
2.  Within a few seconds, you should see the message you sent repeated in the **Log** panel.
3.  Return to the Liberty application tab in your browser, and reload the page.
4.  You should see the message you sent in the list of **Already consumed messages**.

## Stop the Lagom service

Press "Enter" in the console running the Lagom development environment to stop the service.

## Next steps

From here, you can proceed to deploy the service to a Kubernetes cluster using [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/) or [IBM Bluemix Container Service](https://www.ibm.com/cloud-computing/bluemix/containers).

- [Deploy with Minikube](deploy-with-minikube.md)
- [Deploy with Bluemix](deploy-with-bluemix.md)
