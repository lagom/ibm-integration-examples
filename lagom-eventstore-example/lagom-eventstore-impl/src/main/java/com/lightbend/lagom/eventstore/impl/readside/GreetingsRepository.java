/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.readside;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.lightbend.lagom.eventstore.impl.writeside.HelloEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

// A singleton read-side (see also https://www.lagomframework.com/documentation/1.3.x/java/ReadSide.html#Update-the-Read-Side)
// The inner class LagomEventStreamProcessor is registered to process events of type HelloEvent.
@Singleton
public class GreetingsRepository {

    @Inject
    public GreetingsRepository(ReadSide readSide) {
        // Registers the LagomEventStreamProcessor as a read-side.
        // When registering, LagomEventStreamProcessor will specify it wants to consume all events tagged as
        // HelloEvent.HELLO_EVENT_TAG (see below)
        readSide.register(LagomEventStreamProcessor.class);
    }

    private static class LagomEventStreamProcessor extends ReadSideProcessor<HelloEvent> {

        private final EventStoreRepositoryImpl eventStoreRepository;

        // Any event stream consumer must know what was the last event it consumed in order to minimize the number
        // of duplicates it processes.
        // Because this example application is using Cassandra to store the Persistent Entities Journal of events,
        // in this Read Side we can use the CassandraReadSide that will allow keeping track of what events
        // have already been consumed.
        private CassandraReadSide readSide;

        @Inject
        LagomEventStreamProcessor(EventStoreRepositoryImpl eventStoreRepository, CassandraReadSide readSide) {
            this.eventStoreRepository = eventStoreRepository;
            this.readSide = readSide;
        }

        @Override
        public PSequence<AggregateEventTag<HelloEvent>> aggregateTags() {
            return HelloEvent.HELLO_EVENT_TAG.allTags();
        }


        @Override
        public ReadSideHandler<HelloEvent> buildHandler() {
            return readSide.<HelloEvent>builder("evetstore-persistence-processor")
                    // A global prepare is run once across all the cluster. If you start a cluster with n nodes
                    // the global prepare grants this lambda will only be invoked once. A globalPrepare should be
                    // used for context setup operations (e.g. ensuring a database schema or a table exist).
                    .setGlobalPrepare(this::globalPrepare)
                    // A prepare is invoked on each node in the cluster. The code on prepare should setup the local
                    // process (eg. setting up a database connection pool)
                    .setPrepare(this::prepare)
                    // We have to register a handler for each event type we may observe on the event stream. In
                    // this sample application the HelloEntity is kept extremely simple and only emits events of
                    // typs HelloEvent.Greeted.
                    .setEventHandler(HelloEvent.Greeted.class, this::greetingHandler)
                    .build();
        }

        /*
         * The global prepare callback runs at least once across the whole cluster. It is intended
         * for doing things like creating tables and preparing any data that needs to be available
         * before read side processing starts. Read side processors may be sharded across many nodes,
         * and so tasks like creating tables should usually only be done from one node.
         *
         * It may be run multiple times - every time the cluster is restarted the callback
         * will be run. Consequently, the task must be idempotent. If it fails, it will be run again using
         * an exponential backoff, and the read side processing of the whole cluster will not start until
         * it has run successfully.
         */
        private CompletionStage<Done> globalPrepare() {
            return eventStoreRepository.ensureSchema();
        }

        /*
         * This will be executed once per shard, when the read side processor starts up. It can be used
         * for preparing statements in order to optimize Cassandra’s handling of them.
         */
        private CompletionStage<Done> prepare(AggregateEventTag<HelloEvent> aggregateEventTag) {
            return eventStoreRepository.ensureTables();
        }

        private CompletionStage<List<BoundStatement>> greetingHandler(HelloEvent.Greeted event) {
            return eventStoreRepository.store(event).thenApply( done -> TreePVector.<BoundStatement>empty());
        }

    }
}
