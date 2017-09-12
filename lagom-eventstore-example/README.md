# Lagom integration with IBM Project EventStore

[IBM Project EventStore](https://www.ibm.com/us-en/marketplace/project-eventstore) is an in-memory database designed for massive structured data volumes and real-time analytics built on Apache SPARK and Apache Parquet Data Format. 

This project demonstrates a simple Lagom service that includes a [Read-Side](https://www.lagomframework.com/documentation/current/java/ReadSide.html) processor which stores events of the [Persistent Entities](https://www.lagomframework.com/documentation/current/java/ReadSide.html) into Project EventStore for further analysis.

The Lagom service exposes a single HTTP endpoint to simulate events.

## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- IBM Project EventStore [downloaded](https://github.com/IBMProjectEventStore/EventStore-DeveloperPreview/releases/tag/1.1.1), [installed](https://www.ibm.com/support/knowledgecenter/SSGNPV/eventstore/desktop/welcome.html) and running on your local machine. (This process requires 12+Gb of downloads)

Once you have an IBM Project EventStore running in your machine, the main steps to run this example are:

1.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
2.  [Start the Lagom sample application](#start-the-lagom-sample-application)
3.  [Generate some traffic on the Lagom service](#generate-some-traffic-on-the-lagom-service)
4.  [Analyse the traffic using IBM Project EventStore Notebooks](#analyse-the-traffic-using-ibm-project-eventstore-notebooks)

## Download and set up the Lagom service

Follow these steps to get a local copy of this project and configure it with the Message Hub credentials you saved in the previous step. You can supply the credentials in a configuration file or as environment variables.

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/typesafehub/lagom-ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-eventstore-example
    ```
3.  To supply the configuration, do one of the following:
    1. Open the `lagom-eventstore-impl/src/main/resources/ibm-event-store.conf` file in a text editor and fill in the empty values of the `endpoints` and `db.name`. The `db.name` is not hardcoded because IBM Project EventStore can only hold one database at the same time so if you already have a database you may reuse it and add the example tables on that schema.
    2. If you prefer not to enter credentials into the file, you can also set them as environment variables named `IBM_EVENTSTORE_ENDPOINTS`,  and `IBM_EVENTSTORE_DATABASE_NAME`.

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

From a new terminal, you should now generate some traffic on the Lagom service. This is a dummy service with a very simple `GET` operation where users may say hello to other users. Each time a request to say hello to `user123` an event is emitted and stored on IBM Project EventStore. For efficiency, events are not stored immediately, instead they are batched and stored every 5th greeting a user receives.

To send some greetings you should:

1.  Open a new terminal
2.  `curl http://localhost:9000/api/hello/Alice` 
3.  (repeat the previous step several times)

You may also want to send greetings to other users:

1.  `curl http://localhost:9000/api/hello/Bob` 
2.  (repeat the previous step several times)
3.  `curl http://localhost:9000/api/hello/Steve` 
4.  (repeat the previous step several times)


You may even use traffic generation tools like [JMeter](http://jmeter.apache.org/), [Gatling](http://gatling.io/), [ab](https://httpd.apache.org/docs/2.4/programs/ab.html) or [wrk](https://github.com/wg/wrk) to generate massive amounts of requests.

## Analyse the traffic using IBM Project EventStore Notebooks

Once you have generated some data you should use the UI provided by IBM Project EventStore to analyse it:

1.  Bring the UI of IBM Project EventStore to the foreground.
2.  On the Top left corner, click on the menu Icon and select `My Notebooks`.
3.  Click the `(+) Add notebooks` action in the top right section of the UI.
4.  Select `From File` and fill the form fields `Name` and `Description`. In the `Notebook File` field, select the file `/lagom-ibm-integration-examples/lagom-eventstore-example/resources/lagom-event-store-example-greetings.ipynb` from your repository and click the `Create Notebook` button.
5.  Once opened, use the `Run Cell` button (![](docs/imgs/run-cells.png)) to step forward executing each piece of your Notebook.



## Stop the Lagom service and the IBM Project EventStore

To stop running the service:

1.  Press "Enter" in the console running the Lagom development environment to stop the service.
2.  Bring the UI of IBM Project EventStore to the foreground, click on the cog at the top right corner and select `Quit`.

