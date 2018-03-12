# Lagom integration with IBM MQ

[IBM MQ](http://www.ibm.com/software/products/en/ibm-mq) is messaging middleware
that can be used to connect applications and business data across diverse platforms.
[Lagom](https://www.lagomframework.com/) applications can conveniently integrate with
IBM MQ by using the [Alpakka JMS connector](http://developer.lightbend.com/docs/alpakka/current/jms.html).

This example project implements a simple "hello, world" service that can store
custom per-user greetings. Greetings are stored using Lagom's [Persistent Entities](https://www.lagomframework.com/documentation/1.4.x/scala/ES_CQRS.html).
Changes to greetings are sent over IBM MQ before being processed.

In practice, MQ message queues are often be used to integrate multiple
services, with one service sending messages and another service receiving
and processing the messages. For simplicity, this demonstration uses a
single service to handle both sending and receiving MQ messages.

Lagom applications can be run as a cluster. In this demonstration application, every node in
the cluster can send MQ messages, but only a single node will receive MQ messages.
Using a single node for receiving messages is beneficial because it simplifies issues of
ordering when processing messages. [Akka's Cluster Singleton](http://doc.akka.io/docs/akka/current/scala/cluster-singleton.html)
feature automatically manages the process of selecting a node to run the reader logic
and migrating to another node in the cluster in the event of failure.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Set up IBM MQ](#set-up-ibm-mq)
3.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
5.  [Start the Lagom service](#start-the-lagom-service)
6.  [Test the Lagom service](#test-the-lagom-service)
7.  [Stop the Lagom service](#stop-the-lagom-service-and-docker)

## Prerequisites

To build and run this example, you need to have the following installed:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- [Docker](https://www.docker.com/) to run the IBM MQ server

## Start IBM MQ Docker image

IBM provides a Docker image that runs MQ and sets up a few demonstration queues.
Our example application will send messages to these demonstration queues.

To start a docker container based on this image, run:

```
$ docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --publish 1414:1414 --publish 9443:9443 ibmcom/mq:9
```

Note that the `--env LICENSE=accept` argument indicates that you
[accept the Docker image licenses](https://github.com/ibm-messaging/mq-docker#usage).
 
Once the container is running you'll see a message like:

```
IBM MQ Queue Manager QM1 is now fully running
```

You can view the IBM MQ administration interface at [https://localhost:9443].

Docker will continue to run the container until you terminate the process, for example by
typing _Control-C_ in the terminal.

## Download and set up the Lagom service

Follow these steps to get a local copy of this project. You can supply the credentials in a configuration file or as environment variables.

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/lagom/ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-mq-example-scala
    ```

By default, the example Lagom service is configured to connect to the MQ Docker
image running on localhost using its [default settings](https://github.com/ibm-messaging/mq-docker#mq-developer-defaults).
You do not need to change these settings if you are using the default Docker
image. If you do wish to use different MQ settings you can change them in the
Lagom service's `application.conf` configuration file. The settings are documented
in the comments in that file.

## Start the Lagom service

From the `lagom-mq-example-scala` directory run:

```
sbt runAll
```

Once the service has started you'll see a message like:

```
(Service started, press enter to stop and go back to the console...)
```

## Test the Lagom service

Now that the service is running you can interact with it via HTTP.

For example:

```
$ curl http://localhost:9000/api/hello/World
Hello, World!
$ curl -H "Content-Type: application/json" -X POST -d '{"message":"Hi"}' http://localhost:9000/api/hello/World
$ curl http://localhost:9000/api/hello/World
Hi, World!
```

While testing, the Lagom service will log messages to the console
explaining what it's doing. For example:

```
[info] c.e.h.i.HelloJmsSender - Sending greeting update to 'World' with message 'Hi'.
[info] c.e.h.i.HelloJmsSender - Encoded JMS message as {"id":"World","message":"Hi"}
[info] c.e.h.i.HelloJmsReceiverActor - Received message: processing
[info] c.e.h.i.HelloJmsReceiverActor - Received JMS message: {"id":"World","message":"Hi"}
[info] c.e.h.i.HelloJmsReceiverActor - Updating entity 'World' with message 'Hi'.
[info] c.e.h.i.HelloJmsReceiverActor - Message processing finished: waiting for next message
```

## Stop the Lagom service and Docker

To stop the Lagom service, press `Enter` in the terminal.

To stop the Docker MQ container, type `Control-C` in its terminal.
