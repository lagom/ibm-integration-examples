# Deploy the Lagom Message Hub Liberty integration example with Minikube

Lagom has the flexibility to be deployed to a variety of production environments. For detailed information, see the documentation on [Running Lagom in Production](https://www.lagomframework.com/documentation/1.3.x/java/ProductionOverview.html).

This guide demonstrates how to deploy the Lagom service to a Kubernetes cluster running in [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube/). Minikube provides an easy way for you to run a Kubernetes cluster on your local system.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Create a Kubernetes cluster in Minikube](#create-a-kubernetes-cluster-in-minikube)
3.  [Build the Docker image for Minikube](#build-the-docker-image-for-minikube)
4.  [Deploy Cassandra to Minikube](#deploy-cassandra-to-minikube)
5.  [Deploy the Lagom service to Minikube](#deploy-the-lagom-service-to-minikube)
6.  [Test the Lagom service in Minikube](#test-the-lagom-service-in-minikube)
    1.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
    2.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
    3.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)
7.  [Stop the Minikube cluster](#stop-the-minikube-cluster)
8.  [Next steps](#next-steps)

## Prerequisites

Before performing the following steps, follow the instructions in [`README.md`](../README.md).

If this is not the first time you have run the Lagom Message Hub Liberty integration example service, ensure that you have stopped all other running copies that are configured with the same Message Hub service. Only one instance of the Lagom service can read from your sample application topic in the Message Hub service at one time, due to the way Kafka assigns partitions to consumers. In a realistic production application, you can create partitioned topics to allow multiple instances of a consumer to balance the load of processing a topic. See the [Kafka documentation](http://kafka.apache.org/documentation/) for detailed information on how topic partitions are assigned to consumers.

In addition to the [prerequisites outlined in `README.md`](../README.md#prerequisites), you will need to install the following software to deploy using Minikube:

- [Docker](https://www.docker.com/)
- [Kubernetes CLI (`kubectl`)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)

## Create a Kubernetes cluster in Minikube

1.  Open a new command line shell to clear you environment, and change to the `lagom-message-hub-liberty-integration-example` directory.
2.  If you have an existing Minikube Kubernetes cluster, you should delete it:
    ```
    minikube delete
    ```
    If this returns errors, this might indicate that there was no existing cluster to delete, and you can proceed to the next step.
3.  Start a new Kubernetes cluster using Minikube:
    ```
    minikube start
    ```
    This might take a few minutes.
4.  Verify that the Kubernetes CLI can access your cluster with the following command:
    ```
    kubectl get nodes
    ```
    It will print output like the following:
    ```
    NAME       STATUS    AGE       VERSION
    minikube   Ready     5m        v1.7.0
    ```
4.  Configure your Docker CLI to use Minikube:
    - On macOS or Linux:
      ```
      eval $(minikube docker-env)
      ```
    - On Windows:
      ```
      minikube docker-env
      ```
      This prints instructions to the console on how to configure your environment.
    - Verify that Docker can access the Minikube cluster:
      ```
      docker info -f '{{ .Name }}'
      ```
      This command should print: `minikube`.

## Build the Docker image for Minikube

This project has configured the [Docker Maven Plugin](https://dmp.fabric8.io/) to automate building a Docker image for the service and publishing it to the local registry running in Minikube.

From this project directory, run:

```
mvn clean package docker:build
```

Note that if you see a `[ERROR] DOCKER> Unable to pull...` error then you’ll need to update your Java version due to a [known issue with Java TLS](https://github.com/fabric8io/docker-maven-plugin/issues/845#issuecomment-324249997).

## Deploy Cassandra to Minikube

1.  Create the Cassandra pod in Kubernetes:
    ```
    kubectl create -f kubernetes/cassandra
    ```
2.  Wait for the Cassandra pod to become available:
    ```
    kubectl get -w pod cassandra-0
    ```
    This will print the current state of the Cassandra pod and update on changes:
    ```
    NAME          READY     STATUS              RESTARTS   AGE
    cassandra-0   0/1       ContainerCreating   0          10s
    cassandra-0   0/1       Running   0         1m
    cassandra-0   1/1       Running   0         2m
    ```
    Your output might vary, but once you see a line with "1/1" and "Running", you can press control-C to exit and continue to the next step.
3.  Verify the Cassandra deployment:
    ```
    kubectl exec cassandra-0 -- nodetool status
    ```
    This runs a Cassandra status check, and should print output like the following:
    ```
    Datacenter: DC1-K8Demo
    ======================
    Status=Up/Down
    |/ State=Normal/Leaving/Joining/Moving
    --  Address     Load       Tokens       Owns (effective)  Host ID                               Rack
    UN  172.17.0.4  99.47 KiB  32           100.0%            f4d1adaa-89d7-4726-8081-f7a15be676ee  Rack1-K8Demo
    ```

## Deploy the Lagom service to Minikube

1.  Create the Lagom service pod in Kubernetes:
    ```
    kubectl create -f kubernetes/lagom-message-hub-liberty-integration/minikube
    ```
2.  Wait for the Lagom service pod to become available:
    ```
    kubectl get -w pod lagom-message-hub-liberty-integration-0
    ```
    This will print the current state of the Lagom service pod and update on changes:
    ```
    NAME                                      READY     STATUS    RESTARTS   AGE
    lagom-message-hub-liberty-integration-0   1/1       Running   0          14s
    ```
    As above, once you see a line with "1/1" and "Running", you can press control-C to exit and continue to the next step.
3.  Make the service available to your local system:
    ```
    kubectl port-forward lagom-message-hub-liberty-integration-0 9000:9000
    ```
    This will print this output:
    ```
    Forwarding from 127.0.0.1:9000 -> 9000
    Forwarding from [::1]:9000 -> 9000
    ```
    At this point, the service is ready for testing.

## Test the Lagom service

You can test the running Lagom service by following these three steps:

1.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
2.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
3.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)

### Connect to the Lagom message stream

From a WebSocket client, you can monitor the stream of messages that the Lagom service is consuming from Message Hub, and send messages to Lagom to produce to Message Hub, by connecting to the service URI as follows:

1.  Go to http://www.websocket.org/echo.html.
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

## Stop the Minikube cluster

1.  If the `kubectl port-forward ...` command is still running, press control-C to exit it.
2.  Stop the Minikube cluster:
    ```
    minikube stop
    ```
    You can resume it later to return it to the previous state:
    ```
    minikube start
    ```
3.  (Optional) Delete the Minikube cluster:
    ```
    minikube delete
    ```
    This removes all resources and prepares you for a clean start.

## Next steps

From here, you can try another option for running the example:

- [Run in development mode](run-in-development-mode.md)
- [Deploy with Bluemix](deploy-with-bluemix.md)
