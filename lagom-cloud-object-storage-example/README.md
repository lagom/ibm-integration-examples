# Lagom integration with IBM Cloud Object Storage

[IBM Cloud Object Storage](https://www.ibm.com/cloud-computing/bluemix/cloud-object-storage) is a web-scale platform that stores unstructured data — from petabyte to exabyte — with reliability, security, availability and disaster recovery without replication.

This project demonstrates a simple Lagom service that includes a [Read-Side](https://www.lagomframework.com/documentation/current/java/ReadSide.html) processor which stores events of the [Persistent Entities](https://www.lagomframework.com/documentation/1.3.x/java/PersistentEntity.html) into Project EventStore for further analysis.


## Prerequisites

To build and run this example, you need:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)
- [IBM Cloud Object Storage](https://www.ibm.com/cloud-computing/bluemix/cloud-object-storage) setup in [IBM Bluemix](https://www.ibm.com/cloud-computing/bluemix/)


Once you have an IBM Cloud Object Storage setup, the main steps to run this example are:

1.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
2.  [Start the Lagom sample application](#start-the-lagom-sample-application)
3.  [Generate some events on the Lagom service](#generate-some-events-on-the-lagom-service)
4.  [Check uploaded files on Cloud Object Storage GUI](#check-cloud-object-storage)


## Download and set up the Lagom service

Follow these steps to get a local copy of this project and configure it with the Cloud Object Storage credentials and settings.

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/lagom/ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-cloud-object-storage-example
    ```
3.  Supply the configuration:
    1. Copy the `account-impl/src/main/resources/cloud-object-storage.conf.template` file to `account-impl/src/main/resources/cloud-object-storage.conf`.
    2. Open `account-impl/src/main/resources/cloud-object-storage.conf` in a text editor and fill in the necessary information. Details are provided in the file itself.

## Start the Lagom sample application

In the command line shell where you downloaded the Lagom service, from the `lagom-cloud-object-storage-example` directory, start the Lagom development environment by running:

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

This application simulates a bank account application where deposits and withdraws can be executed on an account. On every 5 transactions an account extract is generated and uploaded to Cloud Object Storage. Extracts can be downloaded for local visualization. 

Account transactions are propagated from the write-side (`AccountEntity`) to the read-side (`AccountExtractProcessor`) as events stored in a Cassandra database. 

Extracts are kept in-memory until they reach 5 transactions when they are uploaded to Cloud Object Storage. For the sake of simplicity, the `AccountExtractRepository`, that holds extracts in-memory, is not thread-safe and therefore its code is only suitable for demonstrations.

This application doesn't have a GUI. Only a REST API. Any REST client or http tool can be used to interact with it. We provide a simple `api.sh` that can be used as a shell client that uses [HTTPie](https://httpie.org/).  

The rest of this guide will use `curl` syntax to document the calls. You can adapt it to your REST client of choice or use the provided `api.sh` script. You will find detailed information about the calls for `HTTPie` in the file itself.

  1. Source `api.sh` in your console (eg: `. api.sh`) - optional 
  2. Call   
     ```
     `curl -H "Content-Type: application/json" -XPOST http://localhost:9000/api/account/123-4567-890/deposit --data '{ "amount": 100 }'
     ```
  3. Call   
     ```
     curl -H "Content-Type: application/json" -XPOST http://localhost:9000/api/account/123-4567-890/withdraw --data '{ "amount": 100 }'
     ```
  4. Repeat step 2 and 3 a couple of times. Watch out to not withdraw more than your current balance.  
     You can check the balance by calling.  
     ```
     curl http://localhost:9000/api/account/123-4567-890/balance
     ```
  5. After the 5th operations you should see a INFO logging similar to:
  ```
  14:04:39.293 [info] com.lightbend.lagom.account.impl.readside.AccountExtractRepositoryImpl [] - Extract 123-4567-890#1 has 5 transactions.
  14:04:39.293 [info] com.lightbend.lagom.account.impl.readside.AccountExtractRepositoryImpl [] - Uploading extract: 123-4567-890#1
  ```
  6. Check your Cloud Object Storage bucket in Bluemix. You should see an entry named 123-4567-890#1. 
  7. You can retrieve the account extract by calling:  
  ```
  curl http://localhost:9000/api/account/123-4567-890/extract/1
  ```

  You will notice that an extract has a status, `IN-MEMORY` or `ARCHIVED`.  
  Archived means that it was upload to Cloud Object Storage and its being retrieved from there.
