# Deploy the Lagom Message Hub Liberty integration example with IBM Bluemix Container Service

Lagom has the flexibility to be deployed to a variety of production environments. For detailed information, see the documentation on [Running Lagom in Production](https://www.lagomframework.com/documentation/1.3.x/java/ProductionOverview.html).

This guide demonstrates how to deploy the Lagom service to a Kubernetes cluster running in the cloud using [IBM Bluemix Container Service](https://www.ibm.com/cloud-computing/bluemix/containers). Bluemix offers Kubernetes clusters that can be used in production environments. It provides two options: Lite and Standard clusters. These instructions are designed to work with either type.

## Table of Contents

1.  [Prerequisites](#prerequisites)
2.  [Create a Kubernetes cluster in Bluemix](#create-a-kubernetes-cluster-in-bluemix)
3.  [Create a container registry namespace in Bluemix](#create-a-container-registry-namespace-in-bluemix)
4.  [Build the Docker image for Bluemix](#build-the-docker-image-for-bluemix)
5.  [Deploy Cassandra to Bluemix](#deploy-cassandra-to-bluemix)
6.  [Deploy the Lagom service to Bluemix](#deploy-the-lagom-service-to-bluemix)
7.  [Test the Lagom service in Minikube](#test-the-lagom-service-in-minikube)
    1.  [Connect to the Lagom message stream](#connect-to-the-lagom-message-stream)
    2.  [Test producing a message from the Liberty sample application](#test-producing-a-message-from-the-liberty-sample-application)
    3.  [Test producing a message from the Lagom service](#test-producing-a-message-from-the-lagom-service)
8.  [Delete the Lagom service from Bluemix](#delete-the-lagom-service-from-bluemix)
9.  [Next steps](#next-steps)


## Prerequisites

Before performing the following steps, follow the instructions in [`README.md`](../README.md).

If this is not the first time you have run the Lagom Message Hub Liberty integration example service, ensure that you have stopped all other running copies that are configured with the same Message Hub service. Only one instance of the Lagom service can read from your sample application topic in the Message Hub service at one time, due to the way Kafka assigns partitions to consumers. In a realistic production application, you can create partitioned topics to allow multiple instances of a consumer to balance the load of processing a topic. See the [Kafka documentation](http://kafka.apache.org/documentation/) for detailed information on how topic partitions are assigned to consumers.

In addition to the [prerequisites outlined in `README.md`](../README.md#prerequisites), you will need to install the following software to deploy to Bluemix:

- [Docker](https://www.docker.com/)
- [Kubernetes CLI (`kubectl`)](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
- [Bluemix CLI](https://clis.ng.bluemix.net/ui/home.html)
- [Bluemix Container Service plug-in](https://console.bluemix.net/docs/containers/cs_cli_install.html#cs_cli_install)
- [Bluemix Container Registry plug-in](https://console.bluemix.net/docs/services/Registry/registry_setup_cli_namespace.html#registry_setup_cli_namespace)

## Create a Kubernetes cluster in Bluemix

These instructions assume you have already installed, configured and logged in with the Bluemix CLI tools. If you already have a cluster in Bluemix, you can reuse it and skip to the next section. If this is your first time using Bluemix Container Service, please see [the official documentation](https://console.bluemix.net/docs/containers/cs_cluster.html#cs_cluster_cli) for detailed instructions on setting up clusters.

1.  Open a new command line shell to clear you environment, and change to the `lagom-message-hub-liberty-integration-example` directory.
2.  Create a Lite Kubernetes cluster in Bluemix.
    ```
    bx cs cluster-create --name lagom-test
    ```
3.  Wait for the cluster to be deployed and its worker to be provisioned:
    ```
    bx cs clusters
    bx cs workers lagom-test
    ```
    This can take from several minutes up to an hour to complete.
4.  Configure your environment to use the cluster:
    ```
    bx cs cluster-config lagom-test
    ```
    This command prints another command to run to set your `KUBECONFIG` environment variable. Copy and run that command.

## Create a container registry namespace in Bluemix

You will need to use a private container registry to provide the Docker image to your Kubernetes cluster. These instructions assume you have already installed, configured and logged in with the Bluemix CLI tools. If this is your first time using Bluemix Container Registry, please see [the official documentation](https://console.bluemix.net/docs/services/Registry/index.html) for detailed instructions on creating registry namespaces.

1.  Choose a namespace to use. Container registry namespaces must be globally unique, so it's best to choose a name that is unlikely to conflict with others, such as one that includes the name of your organization. In the steps below, replace all occurrences of the text "`<registry-namespace>`" with your chosen namespace.
2.  Create the namespace in Bluemix:
    ```
    bx cr login
    bx cr namespace-add <registry-namespace>
    ```
3.  In a text editor, open the file at `kubernetes/lagom-message-hub-liberty-integration/bluemix/lagom-message-hub-liberty-integration-statefulset.json`
4.  In that file, find the `"image"` key and update the value to use your chosen namespace: `"registry.ng.bluemix.net/<registry-namespace>/lagom/message-hub-liberty-integration-impl"`

## Build the Docker image for Bluemix

1.  Build the Docker image locally:
    ```
    mvn clean package docker:build
    ```
2.  Upload it to your private registry:
    ```
    docker tag lagom/message-hub-liberty-integration-impl registry.ng.bluemix.net/<registry-namespace>/lagom/message-hub-liberty-integration-impl
    docker push registry.ng.bluemix.net/<registry-namespace>/lagom/message-hub-liberty-integration-impl
    ```

## Deploy Cassandra to Bluemix

1.  If you are using an existing Kubernetes cluster, check if the Cassandra service has already been deployed:
    ```
    kubectl get service cassandra
    ```
    If this prints "`services "cassandra" not found`" then proceed. If there is an existing service, you can skip this section and move on to [Deploy the Lagom service to Bluemix](#deploy-the-lagom-service-to-bluemix).
2.  Create the Cassandra pod in Kubernetes:
    ```
    kubectl create -f kubernetes/cassandra
    ```
3.  Wait for the Cassandra pod to become available:
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
4.  Verify the Cassandra deployment:
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

## Deploy the Lagom service to Bluemix

1.  Create the Lagom service pod in Kubernetes:
    ```
    kubectl create -f kubernetes/lagom-message-hub-liberty-integration/bluemix
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

## Delete the Lagom service from Bluemix

When you are finished testing the service in Bluemix, you can delete the service from the Kubernetes cluster.

1.  If the `kubectl port-forward ...` command is still running, press control-C to exit it.
1.  Delete the Lagom service resources from the Kubernetes cluster in Bluemix:
    ```
    kubectl delete statefulsets lagom-message-hub-liberty-integration --cascade=false
    kubectl delete --ignore-not-found=true -f kubernetes/lagom-message-hub-liberty-integration/bluemix
    ```
    As noted above, only one instance of the Lagom service can consume messages from the Message Hub topic at a time, so if the service is left running in Bluemix, you will not be able to test it successfully in Minikube or the Lagom development environment.
2.  (Optional) Delete the Cassandra service from the Kubernetes cluster:
    ```
    kubectl delete -f kubernetes/cassandra
    ```
3.  (Optional) Delete the Kubernetes cluster from Bluemix Container Service:
    ```
    bx cs cluster-rm lagom-test
    ```
    Keep in mind that this will remove *all* resources in the cluster, and that clusters can take a long time to recreate. Only delete the cluster when you are sure you won't need it again.
4.  (Optional) Delete the namespace you created from Bluemix Container Registry:
    ```
    bx cr namespace-rm <registry-namespace>
    ```

## Next steps

From here, you can try another option for running the example:

- [Run in development mode](docs/run-in-development-mode.md)
- [Deploy with Minikube](deploy-with-minikube.md)
