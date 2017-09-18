# Lagom integration with IBM Db2 and JPA

[IBM Db2](https://www.ibm.com/analytics/us/en/db2/) is a relational database management system (RDBMS) that supports JDBC access. You can use it with Lagom's [Relational Database Persistent Entities](https://www.lagomframework.com/documentation/current/java/PersistentEntityRDBMS.html), [JDBC Read-Side Support](https://www.lagomframework.com/documentation/current/java/ReadSideJDBC.html) and [JPA Read-Side Support](https://www.lagomframework.com/documentation/current/java/ReadSideJPA.html).

This example project implements a simple "hello, world" service that can store custom per-user greetings in Db2. It demonstrates use of a Lagom Persistent Entity and of the Command-Query Responsibility Segregation (CQRS) data persistence pattern.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Set up Db2](#set-up-db2)
3.  [Create the `HELLO` database](#create-the-hello-database)
3.  [Download and set up the Lagom service](#download-and-set-up-the-lagom-service)
4.  [Download and install the Db2 JDBC driver](#download-and-install-the-db2-jdbc-driver)
5.  [Start the Lagom service](#start-the-lagom-service)
6.  [Test the Lagom service](#test-the-lagom-service)
7.  [Stop the Lagom service](#stop-the-lagom-service)
8.  [Next steps](#next-steps)

## Prerequisites

To build and run this example, you need to have the following installed:

- [git](https://git-scm.com/)
- [Java SE 8 JDK](http://www.oracle.com/technetwork/java/javase/overview/index.html)
- [Maven 3.2.1+](https://maven.apache.org/) to build and run the Lagom project (3.5.0 recommended)

## Set up Db2

If you already have Db2 installed locally, you will need your credentials for:

1. The Data Server Manager account that you use to administer your database.
2.  The  Db2 instance user account that you use to connect to a Db2 database.

Skip to the instructions for creating the database.

If you need to install Db2 to run locally, the easiest way is to download [IBM Db2 Developer Community Edition](https://www.ibm.com/us-en/marketplace/ibm-db2-direct-and-developer-editions). The download page includes installers for macOS, Windows and Linux that use Docker to run Db2 and Data Server Manager. Also download the "IBM Db2 Developer Community Edition Get Started Guide" for detailed installation instructions.

The first time you run the IBM Db2 Developer Community Edition application, it prompts you for information including the two sets of user account credentials you will need for this example. Then, you will need to create a new database for the example.

## Create the `HELLO` database

Create a new database named "`HELLO`" for use in this example.

There are several ways to create a database, for example:

- Run the [`CREATE DATABASE HELLO`](https://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.admin.cmd.doc/doc/r0001941.html) command from a console or client application.
- Use the [IBM Data Server Manager](https://www.ibm.com/us-en/marketplace/data-server-manager) program that is installed with Db2.

For example, to create the "`HELLO`" database with IBM Data Server Manager:

1.  Open the Data Server Manager program.
2.  Log in with the administrative credentials you created when installing Db2.
3.  Go to Administer > Instance.
4.  Click "Create a new database".
5.  In the "Name" field, enter "HELLO".
6.  Click "Run"

## Download and set up the Lagom service

Follow these steps to get a local copy of this project and configure it with the Db2 credentials you saved in the previous step. You can supply the credentials in a configuration file or as environment variables.

1.  Open a command line shell and clone this repository:
    ```
    git clone https://github.com/lagom/ibm-integration-examples.git
    ```
2.  Change into the root directory for this example:
    ```
    cd lagom-jpa-db2-example
    ```
3.  Lagom needs to be configured with the Db2 instance username and password you configured when you installed Db2. To supply the configuration, do one of the following:
    1.  By default, this service is configured with the username and password both set to "db2inst1". If you used the default, continue to the next step.
    2.  To supply the configuration in a configuration file, open the `hello-impl/src/main/resources/application.conf` file in a text editor and change the `username` and `password` values to the credentials created when installing Db2.
    3.  If you prefer not to enter credentials into the file, you can also set them as environment variables named `DB2_USERNAME` and `DB2_PASSWORD`.

## Download and install the Db2 JDBC driver

To build and run the Lagom service, you will need to make the Db2 JDBC driver available to Maven:

- If your organization has an internal Maven artifact repository that already hosts the Db2 JDBC driver, you can use this. You might need to change the `groupId`, `artifactId`, and `version` for the dependency in this project's top-level `pom.xml` file to match the values used in your repository.
- Otherwise, download [Db2 JDBC Driver](http://www-01.ibm.com/support/docview.wss?uid=swg21363866) (`db2jcc4.jar`) version 4.23.42 from IBM to your current directory. Then, run the following command to install it to your local Maven repository:

    ```
    mvn install:install-file -Dfile=db2jcc4.jar -Dversion=4.23.42 -DgroupId=com.ibm.db2.jcc -DartifactId=db2jcc4 -Dpackaging=jar
    ```

## Start the Lagom service

In the command line shell where you downloaded the Lagom service, from the `lagom-jpa-db2-example` directory, start the Lagom development environment by running:

```
mvn lagom:runAll
```

You should see some console output, including these lines:

```
...
[INFO] Service gateway is running at http://localhost:9000
...
[INFO] Service hello-impl listening for HTTP on 0:0:0:0:0:0:0:0:57797
[INFO] (Service started, press enter to stop and go back to the console...)
```

These messages indicate that the service has started correctly.

## Test the Lagom service

To test the Lagom service, you will need to use an HTTP client such as [`curl`](https://curl.haxx.se/), [HTTPie](https://httpie.org/) or [Postman](https://www.getpostman.com/). If using a command line client, be sure to open a new command line shell to run the client, and keep the Lagom service running in the original shell.

This service defines three calls you can test:

1.  [Test the `hello` service call](#test-the-hello-service-call)
2.  [Test the `useGreeting` service call](#test-the-usegreeting-service-call)
3.  [Test the `allGreetings` service call](#test-the-allgreetings-service-call)

### Test the `hello` service call

The `hello` service call takes a user name at the end of the URL, and returns a greeting for that user.

Make an HTTP `GET` request to `http://localhost:9000/api/hello/Alice`:

`curl` example:

```
curl http://localhost:9000/api/hello/Alice
```

Result:

```
Hello, Alice!
```


### Test the `useGreeting` service call

The `useGreeting` service call uses a Lagom Persistent Entity to store an alternative greeting for a specific user name.

Make an HTTP `POST` request to `http://localhost:9000/api/hello/Bob` with a JSON request body containing `{"message": "Good day"}` to set the new message.

`curl` example:

```
curl -H "Content-Type: application/json" -X POST -d '{"message": "Good day"}' http://localhost:9000/api/hello/Bob
```

Result:

```
{ "done" : true }
```

Then, make a `GET` request to the same URL to see the new greeting in use.

`curl` example:

```
curl http://localhost:9000/api/hello/Bob
```

Result:

```
Good day, Bob!
```

### Test the `allGreetings` service call

As new greetings are set with the `useGreeting` service call, a Lagom read-side event processor asynchronously updates a custom table in Db2 with the set of all custom greetings.

The `allGreetings` service call uses JPA to query this table and return the list of all results.

This data access pattern is known as Command-Query Responsibility Segregation (or "CQRS"). For more background, see the Lagom documentation on [Managing data persistence](https://www.lagomframework.com/documentation/current/java/ES_CQRS.html).

Invoke the `useGreeting` service call several times, with different user names and greetings, to create data that can be queried with the `allGreetings` service call.

`curl` example:

```
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
curl -H "Content-Type: application/json" -X POST -d '{"message": "Good morning"}' http://localhost:9000/api/hello/Bob
curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Carol
curl -H "Content-Type: application/json" -X POST -d '{"message": "Howdy"}' http://localhost:9000/api/hello/David
```

Then, make a `GET` request to `http://localhost:9000/api/greetings` to query the list of all custom greetings.

`curl` example:

```
curl http://localhost:9000/api/greetings
```

Result:

```
[{"id":"Bob","message":"Good morning"},{"id":"Alice","message":"Hi"},{"id":"Carol","message":"Hi"},{"id":"David","message":"Howdy"}]
```

## Stop the Lagom service

Press "Enter" in the console running the Lagom development environment to stop the service.

## Next steps

To understand more about how the example was configured to work with Db2, review the following files in this project's source code:

- [`pom.xml`](pom.xml) and [`hello-impl/pom.xml`](hello-impl/pom.xml) — dependency configuration
- [`application.conf`](hello-impl/src/main/resources/application.conf) — database connection configuration
- [`persistence.xml`](hello-impl/src/main/resources/META-INF/persistence.xml) — JPA persistence context configuration
- [`HelloEntity.java`](hello-impl/src/main/java/com/lightbend/lagom/hello/impl/HelloEntity.java) — database-neutral persistent entity implementation
- [`Greetings.java`](hello-impl/src/main/java/com/lightbend/lagom/hello/impl/Greetings.java) — JPA read-side database implementation
