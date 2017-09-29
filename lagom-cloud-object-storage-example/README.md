# Lagom integration with IBM Cloud Object Storage

[IBM Cloud Object Storage](https://www.ibm.com/cloud-computing/bluemix/cloud-object-storage) is a web-scale platform that stores unstructured data — from petabyte to exabyte — with reliability, security, availability and disaster recovery without replication.

This project demonstrates a simple Lagom service that includes a [Read-Side](https://www.lagomframework.com/documentation/current/java/ReadSide.html) processor that publishes Account Extracts into [IBM Cloud Object Storage](https://www.ibm.com/cloud-computing/bluemix/cloud-object-storage).


## How the application works

This example is a simple banking application that allows you to simulate depositing and withdrawing money from one account. The example propagates account transactions from the write-side (`AccountEntity`) to the read-side (`AccountExtractProcessor`) as events stored in a Cassandra database. On every 5 transactions, the service generates an account extract and uploads it to a Cloud Object Storage bucket. Extracts can be downloaded for local visualization.

Note: the`AccountExtractRepository`, that holds extracts in-memory, is not thread-safe and therefore its code is only suitable for demonstrations.

## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- [IBM Cloud Object Storage](https://www.ibm.com/cloud-computing/bluemix/cloud-object-storage) setup in [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/)


Once you have an IBM Cloud Object Storage setup, the main steps to run this example are:

1.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
2.  [Start the Lagom sample application](#start-the-lagom-sample-application)
3.  [Generate some traffic on the Lagom service](#generate-some-traffic-on-the-lagom-service)
4.  [Stop Lagom and clean IBM Cloud Object Storage](#stop-lagom-and-clean-ibm-cloud-object-storage)
5.  [Next steps](#next-steps)


## Download and set up the Lagom service

Follow these steps to get a local copy of this project and configure it with the Cloud Object Storage credentials and settings.

1.  Open a command line shell and clone the example repository:
    ```
    git clone https://github.com/lagom/ibm-integration-examples.git
    ```
2.  Change into the example's root directory:
    ```
    cd ibm-integration-examples/lagom-cloud-object-storage-example
    ```
3.  Supply the configuration:
    1. Copy the `account-impl/src/main/resources/cloud-object-storage.conf.template` file to `account-impl/src/main/resources/cloud-object-storage.conf`.
    2. Open `account-impl/src/main/resources/cloud-object-storage.conf` in a text editor and fill in the necessary information. Details are provided in the file itself.

## Start the Lagom sample application

 In the command line shell where you downloaded the Lagom service, start the Lagom development environment from the `lagom-cloud-object-storage-example` directory:

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

To keep things simple, the example does not have a GUI but exposes a REST API. You can use any REST client or http tool to interact with the application. The rest of this guide will use curl syntax to document the calls. You can adapt it to your REST client of choice.

The example account number is 123-456-890. The Lagom service provides APIs to check the balance and to deposit or withdraw money. Use the REST calls below to create transactions. Be sure not to withdraw more money than the account balance. Then, retrieve the extract from the Cloud Object Storage bucket.

To check the balance and generate transactions, use calls to the following endpoints:

1. To check the account balance:
```
curl http://localhost:9000/api/account/123-4567-890/balance
```
2.  To deposit money:
```
curl -H "Content-Type: application/json" -XPOST http://localhost:9000/api/account/123-4567-890/deposit --data '{ "amount": 100 }'
```
3. To withdraw money:
```
curl -H "Content-Type: application/json" -XPOST http://localhost:9000/api/account/123-4567-890/withdraw --data '{ "amount": 100 }'
```
4. To retrieve an extract:
```
curl http://localhost:9000/api/account/123-4567-890/extract/1
```
Extract are retrived by number (#1 in above example). The extract has a status: `ARCHIVED` meaning it is uploaded to Cloud Object Storage and is being retrieved from there or `CURRENT`indicating that this is currently being built in-memory and it's not yet uploaded.

4. After the 5th operations you should see a INFO logging similar to:
```
14:04:39.293 [info] com.lightbend.lagom.account.impl.readside.AccountExtractRepositoryImpl [] - Extract 123-4567-890#1 has 5 transactions.
14:04:39.293 [info] com.lightbend.lagom.account.impl.readside.AccountExtractRepositoryImpl [] - Archiving extract: 123-4567-890#1
```
At this point, Extract `123-4567-890#1` has been archived to Cloud Object Storage bucket. You can retrieve it by calling:
```
curl http://localhost:9000/api/account/123-4567-890/extract/1
```
You can also navigate to the  Cloud Object Storage bucket in Bluemix and verify the presence of the file.

## Stop Lagom and clean IBM Cloud Object Storage

To stop running the service:

1.  Press "Enter" in the console running the Lagom development environment to stop the service.
2.  At this point you may want to remove the uploaded files from you Cloud Object Storage or simply delete the bucket or account if there were only created for running this demo.

## Next steps

To understand more about how the example was configured to work with Cloud Object Storage, review the following files in this project's source code:

- [`pom.xml`](pom.xml) and [`account-impl/pom.xml`](account-impl/pom.xml) — dependency configuration
- [`application.conf`](account-impl/src/main/resources/application.conf) — database connection configuration
- [`AccountEntity.java`](account-impl/src/main/java/com/lightbend/lagom/account/impl/AccountEntity.java) — database-neutral persistent entity implementation
- [`AccountExtractProcessor.java`](account-impl/src/main/java/com/lightbend/lagom/account/impl/readside/AccountExtractProcessor.java) — the read-side processor that consumes events from `AccountEntity` and forward to `AccountExtractRepository`.
- [`AccountExtractRepositoryImpl.java`](account-impl/src/main/java/com/lightbend/lagom/account/impl/readside/AccountExtractRepositoryImpl.java) - the repository that accumulates `Account Extracts` in before archiving in Cloud Object Storage.
- [`Storage.java`](account-impl/src/main/java/com/lightbend/lagom/account/impl/readside/Storage.java) - the `Storage` component is a facade to Alpakka S3 connector that is used to communicate with the Cloud Object Storage endpoint.
