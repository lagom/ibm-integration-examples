# Lagom integration with IBM Db2 Event Store

[IBM Db2 Event Store](https://www.ibm.com/products/db2-event-store) is an in-memory database designed for massive structured data volumes and real-time analytics built on Apache SPARK and Apache Parquet Data Format. 

This project demonstrates a simple Lagom service that includes a [Read-Side](https://www.lagomframework.com/documentation/current/java/ReadSide.html) processor which stores events of the [Persistent Entities](https://www.lagomframework.com/documentation/1.3.x/java/PersistentEntity.html) into Project EventStore for further analysis.

The Lagom service exposes a single HTTP endpoint to simulate events.

## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- IBM Db2 Event Store [downloaded](https://github.com/IBMProjectEventStore/EventStore-DeveloperPreview/releases/tag/1.1.1), [installed](https://www.ibm.com/support/knowledgecenter/SSGNPV/eventstore/desktop/welcome.html) and running on your local machine. (This process requires 12+Gb of downloads)

Once you have an IBM Db2 Event Store running in your machine, the main steps to run this example are:

1.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
2.  [Start the Lagom sample application](#start-the-lagom-sample-application)
3.  [Generate some traffic on the Lagom service](#generate-some-traffic-on-the-lagom-service)
4.  [Analyse the traffic using IBM Db2 Event Store Notebooks](#analyse-the-traffic-using-ibm-db2-event-store-notebooks)

## Download and set up the Lagom service

(Make sure you completed all the steps on the Pre-Requisites section and IBM Db2 Event Store is installed and running)

Follow these steps to get a local copy of this project and configure it to connect it to the IBM Db2 Event Store running on your machine

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/lagom/ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-eventstore-example
    ```
3.  To supply the configuration, perform the following steps:
    1. Open the `lagom-eventstore-impl/src/main/resources/ibm-event-store.conf` file in a text editor and fill in the empty value of the `endpoints` setting.
    2. In the same file, provide the value of the `db.name` setting. If this is the first time you use IBM Db2 Event Store the default value is fine. If you already have a database and its name is different then the default you will have to either remove the existing database from your Event Store instance or change the value of `db.name` in the settings. Note that if you have existing data on your local instance of IBM Db2 Event Store it may be deleted.
    3. This example application may rebuild the EventStore from scratch on every reboot so you can test from a clean slate: use the `clear-schema` config to rebuild the database from scratch on every run.


## Start the Lagom sample application

In the command line shell where you downloaded the Lagom service, from the `lagom-eventstore-example` directory, start the Lagom development environment by running:

```
mvn lagom:runAll
```

You should see some console output, including these lines:

```
...
[INFO] Service gateway is running at http://localhost:9000
...
[INFO] (Service started, press enter to stop and go back to the console...)
```

These messages indicate that the service has started correctly.


## Generate some traffic on the Lagom service

From a new terminal, you should now generate some traffic on the Lagom service. This is a dummy service with a very simple `GET` operation where users may say hello to other users. Each time a request to say hello to `user123` an event is emitted and stored on IBM Db2 Event Store. For efficiency, events are not stored immediately, instead they are batched and stored every 5th greeting a user receives.

To send some greetings you should:

1.  Open a new terminal
2.  `curl http://localhost:9000/api/hello/Alice` 
3.  (repeat the previous step several times)

You may also want to send greetings to other users:

1.  `curl http://localhost:9000/api/hello/Bob` 
2.  (repeat the previous step several times)
3.  `curl http://localhost:9000/api/hello/Steve` 
4.  (repeat the previous step several times)


Here you should use a traffic generation tool like [JMeter](http://jmeter.apache.org/), [Gatling](http://gatling.io/), [ab](https://httpd.apache.org/docs/2.4/programs/ab.html) or [wrk](https://github.com/wg/wrk) to generate massive amounts of requests.

For example:

```
wrk http://localhost:9000/api/hello/Janine
wrk http://localhost:9000/api/hello/Albert
wrk http://localhost:9000/api/hello/Billy
wrk http://localhost:9000/api/hello/Samantha
```

## Analyse the traffic using IBM Db2 Event Store Notebooks

Once you have generated some data you should use the UI provided by IBM Db2 Event Store to analyse it:

1.  Bring the UI of IBM Db2 Event Store to the foreground.
2.  On the Top left corner, click on the menu Icon and select `My Notebooks`.
3.  Click the `(+) Add notebooks` action in the top right section of the UI.
4.  Select `From File` and fill the form fields `Name` and `Description`. In the `Notebook File` field, select the file `/lagom-ibm-integration-examples/lagom-eventstore-example/resources/lagom-event-store-example-greetings.ipynb` from your repository and click the `Create Notebook` button.
5.  Review the values on the `Setup` cell and make sure the endpoints and database name match the values you configured in `ibm-event-store.conf` when setting up the lagom service in previous steps.
6.  Once opened, use the `Run Cell` button (![](docs/imgs/run-cells.png)) to step forward executing each piece of your Notebook.



## Stop the Lagom service and the IBM Db2 Event Store

To stop running the service:

1.  Press "Enter" in the console running the Lagom development environment to stop the service.
2.  Bring the UI of IBM Db2 Event Store to the foreground, click on the cog at the top right corner and select `Quit`.


## Next steps

To understand more about how Lagom can be configured to work with IBM Db2 Event Store, review the following files in this project's source code:

- [`pom.xml`](pom.xml) and [`lagom-eventstore-impl/pom.xml`](lagom-eventstore-impl/pom.xml) — dependency configuration
- [`ibm-event-store.conf`](lagom-eventstore-impl/src/main/resources/ibm-event-store.conf) — database connection configuration

The relevant code in this example is located on the `com.lightbend.lagom.eventstore.impl.readside`package:

- [`GreetingsRepository.java`](lagom-eventstore-impl/src/main/java/com/lightbend/lagom/eventstore/impl/readside/GreetingsRepository.java) — a Lagom Read-Side that processes Persistent Entity events and stores them into IBM Db2 Event Store.
- [`EventStoreRepositoryImpl.java`](lagom-eventstore-impl/src/main/java/com/lightbend/lagom/eventstore/impl/readside/EventStoreRepositoryImpl.java) — A Facade encapsulating the IBM Event API.

The code on the `com.lightbend.lagom.eventstore.impl.writeside` package is the minimum required to have a Persistent Entity which produces an eventstream. This example uses a Cassandra storage for the Persistent Entity Journal. Check out the [Lagom integration with IBM Db2 and JPA](../lagom-jpa-db2-example/README.md) for an example on using a different backend for your Persistent Entities.
