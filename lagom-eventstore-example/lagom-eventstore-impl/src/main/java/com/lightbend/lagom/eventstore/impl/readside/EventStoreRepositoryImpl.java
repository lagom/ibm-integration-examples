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

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Singleton
public class EventStoreRepositoryImpl {


    private static final String TABLE_NAME = "Greetings";
    private final EventStoreConfig eventStoreConfig;
    private ResolvedTableSchema greetingsTable;
    private EventContext eventCtx;

    public EventStoreRepositoryImpl(com.typesafe.config.Config config) {
        eventStoreConfig = new EventStoreConfig(config);

    }

    public CompletionStage<Done> ensureSchema() {
        return CompletableFuture.supplyAsync(() -> {

            ConfigurationReader.setConnectionEndpoints(eventStoreConfig.endpoints);

            String databaseName = eventStoreConfig.databaseName;
            Option<EventError> maybeErrors = EventContext$.MODULE$.openDatabase(databaseName);
            // if there was no error opening the DB it means there's a DB
            if (maybeErrors.isEmpty()) {
                // drop the DB
                EventContext.dropDatabase(databaseName);
            }
            EventContext.createDatabase(databaseName);
            return Done.getInstance();
        });
    }

    public CompletionStage<Done> ensureTables() {
        this.eventCtx = EventContext.getEventContext();
        return CompletableFuture.supplyAsync(() -> {
            Seq<String> shardingColumns = JavaConversions.asScalaBuffer(Collections.EMPTY_LIST).toSeq();
            Seq<String> pkColumns = JavaConversions.asScalaBuffer(Arrays.asList("instant", "name")).toSeq();

            StructField[] fields = new StructField[]{
                    DataTypes.createStructField("name", DataTypes.StringType, false),
                    DataTypes.createStructField("instant", DataTypes.IntegerType, false)
            };

            TableSchema greetingsSchema =
                    new TableSchema(
                            TABLE_NAME,
                            new StructType(fields),
                            shardingColumns,
                            pkColumns,
                            Option.<Seq<String>>empty());

            // create the table
            Optional<EventError> maybeError =
                    OptionConverters.toJava(eventCtx.createTable(greetingsSchema));
            if (maybeError.isPresent()) {
                throw new RuntimeException(maybeError.get().errStr());
            } else {
                greetingsTable = eventCtx.getTable(TABLE_NAME);
                return Done.getInstance();
            }
        });
    }


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

            IndexedSeq<Row> batch =
                    JavaConversions.asScalaBuffer(
                            buffer
                                    .stream()
                                    .map(ev -> (HelloEvent.Greeted) ev)
                                    .map(greet -> RowFactory.create(greet.getName(), greet.getInstant()))
                                    .collect(Collectors.toList())
                    ).toIndexedSeq();

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
