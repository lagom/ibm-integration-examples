/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.readside;

import akka.Done;
import com.ibm.event.catalog.ResolvedTableSchema;
import com.ibm.event.catalog.TableSchema;
import com.ibm.event.common.ConfigurationReader;
import com.ibm.event.oltp.EventContext;
import com.ibm.event.oltp.EventContext$;
import com.ibm.event.oltp.EventError;
import com.ibm.event.oltp.InsertResult;
import com.lightbend.lagom.eventstore.impl.writeside.HelloEvent;
import com.typesafe.config.Config;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Option;
import scala.collection.IndexedSeq;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.compat.java8.FutureConverters;
import scala.compat.java8.OptionConverters;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

// This facade encapsulates the required to setup the EventStore database, create its tables
// and store data on them.
public class EventStoreRepositoryImpl {

    private static final String TABLE_NAME = "Greetings";
    private final EventStoreConfig eventStoreConfig;
    private ResolvedTableSchema greetingsTable;
    private EventContext eventCtx;

    @Inject
    public EventStoreRepositoryImpl(com.typesafe.config.Config config) {
        eventStoreConfig = new EventStoreConfig(config);

    }

    // Idempotently ensures the database we want exists. This may continually fail if there's a
    // preexisting database in IBM Project EventStore and is not named like the value setup on
    // EventStoreConfig.databaseName
    // IBM Project EventStore can only hold one database at once so there are three possible
    // situations:
    //   a) there's not DB on EventStore
    //          --> this methid will create a new one
    //   b) there's a DB on EventStore and it's named like the settings on EventStoreConfig.databaseName
    //          --> that database will be used (and no data will be erased
    //   c) there's a DB on EventStore with a different name
    //          --> this method will fail. The existing database should be removed manually
    // This method may be invoked once across the cluster.
    public CompletionStage<Done> ensureSchema() {
        return CompletableFuture.supplyAsync(() -> {

            ConfigurationReader.setConnectionEndpoints(eventStoreConfig.endpoints);

            String databaseName = eventStoreConfig.databaseName;

            Optional<EventError> maybeErrors =
                    OptionConverters.toJava(EventContext$.MODULE$.openDatabase(databaseName));
            // if there was an error opening the DB it probably means the database is missing.
            if (maybeErrors.isPresent()) {
                // try to create the database. If this operation fails it's probably because there's already
                // a DB with a different name. We'll just let the exception flow to upper layers.
                EventContext.createDatabase(databaseName);
            }
            return Done.getInstance();
        });
    }

    // This method ensures that the table we need are created and also that all the necessary
    // infrastructure for this process to use those tables is properly initialized.
    // This method must be invoked at least once on each node in the Lagom cluster.
    public CompletionStage<Done> ensureTables() {
        // This assignment must run on every Lagom process because each node on the cluster
        // needs to hold a reference to the Event Context.
        this.eventCtx = EventContext.getEventContext();
        return CompletableFuture.supplyAsync(() -> {

            // We create the Table Schema. This API uses a combination of IBM-Event's Scala API and
            // Spark's Java API.
            // When Scala is required we're using JavaConversions that allow converting Java Types
            // into Scala types (e.g. java.util.List<T> into scala.collection.Seq<T> )
            StructField[] fields = new StructField[]{
                    DataTypes.createStructField("name", DataTypes.StringType, false),
                    DataTypes.createStructField("instant", DataTypes.IntegerType, false)
            };
            Seq<String> shardingColumns = JavaConversions.asScalaBuffer(Collections.EMPTY_LIST).toSeq();
            Seq<String> pkColumns = JavaConversions.asScalaBuffer(Arrays.asList("instant", "name")).toSeq();
            TableSchema greetingsSchema =
                    new TableSchema(
                            TABLE_NAME,
                            new StructType(fields),
                            shardingColumns,
                            pkColumns,
                            Option.<Seq<String>>empty());

            // Request the creation of the table. This operation uses OptionConverters provided
            // by the scala-java8-compat library to convert from Scala's Option[T] into Java's
            // Optional<T>.
            Optional<EventError> maybeError =
                    OptionConverters.toJava(eventCtx.createTable(greetingsSchema));

            // TODO: This code may need a review. This method should be idempotent and it's not 100% clear
            // what's the return value of `eventCtx.createTable` if it already exists or if it already
            // exists but it has a different structure.
            if (maybeError.isPresent()) {
                throw new RuntimeException(maybeError.get().errStr());
            } else {
                // Each process in the Lagom cluster must keep a reference to the table. If/when the
                // connectivity to the EventStore is lost, this reference may become unusable. When
                // that happens, a new attempt to use will cause an error or an exception. That error
                // will crash the Read Side processor. When the read Side processor crashed, Lagom will
                // take over and recreate it. During recreation this class will be rebuilt from scratch
                // and properly reinitialized becoming usable again.
                greetingsTable = eventCtx.getTable(TABLE_NAME);
                return Done.getInstance();
            }
        });
    }


    // Instead of storing every event as it is emitted, we're batching events in memory in
    // batches of BUFFER_SIZE. This could lead to data loss. Do not do this in production, this
    // is a simplifiation for demo pruposes.

    // This is an in-memory buffer to build batches of 5 items when storing data into EventStore.
    // This in-memory buffer will be lost when this process crashes so the data in EventStore
    // could contains gaps. So the complete PersistentEntity eventStream -to- EventStore delivery
    // uses `at-most-once` semantics.
    // This in-memory implementation is only meant for demo purposes where `at-most-once` is desired.
    private int BUFFER_SIZE = 5;
    private List<HelloEvent> buffer = new ArrayList<>(BUFFER_SIZE);

    public CompletionStage<Done> store(HelloEvent event) {
        buffer.add(event);

        if (buffer.size() >= BUFFER_SIZE) {

            // IndexedSeq is a type of Scala immutable collection equivalent to an ArrayList.
            IndexedSeq<Row> batch =
                    JavaConversions.asScalaBuffer(
                            buffer
                                    .stream()
                                    .map(ev -> (HelloEvent.Greeted) ev)
                                    .map(greet -> RowFactory.create(greet.getName(), greet.getInstant()))
                                    .collect(Collectors.toList())
                    ).toIndexedSeq();

            // IBM's EventStore Scala API returns a Future[T]  which is equivalent to a Java's
            // CompletionStage. We use the scala-java8-compat library to convert from one to the other.
            CompletionStage<InsertResult> insertResultCompletionStage =
                    FutureConverters.toJava(eventCtx.batchInsertAsync(greetingsTable, batch, false));
            return insertResultCompletionStage.thenApply(ignored -> {
                        buffer = new ArrayList<>(BUFFER_SIZE);
                        return Done.getInstance();
                    }
            );
        } else {
            return CompletableFuture.completedFuture(Done.getInstance());
        }

    }


    // This utility class encapsulates the access to the config.
    // See ./lagom-eventstore-impl/src/main/resources/ibm-event-store.conf for more details
    // on the settings used here.
    static class EventStoreConfig {
        private final String endpoints;
        private final String databaseName;

        EventStoreConfig(com.typesafe.config.Config config) {
            Config esConfig = config.getConfig("ibm.eventstore");
            endpoints = esConfig.getString("endpoints");
            databaseName = esConfig.getString("db.name");
        }
    }
}
