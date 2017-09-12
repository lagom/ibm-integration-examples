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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class GreetingsRepository {


    @Inject
    public GreetingsRepository(ReadSide readSide, EventStoreRepositoryImpl eventStoreRepository) {
        readSide.register(LagomEventStreamProcessor.class);
    }

    private static class LagomEventStreamProcessor extends ReadSideProcessor<HelloEvent> {

        private final EventStoreRepositoryImpl eventStoreRepository;
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
                    .setGlobalPrepare(this::globalPrepare)
                    .setPrepare(this::prepare)
                    .setEventHandler(HelloEvent.Greeted.class, this::greetingHandler)
                    .build();
        }

        /*
         * The global prepare callback runs at least once across the whole cluster. It is intended
         * for doing things like creating tables and preparing any data that needs to be available
         * before read side processing starts. Read side processors may be sharded across many nodes,
         * and so tasks like creating tables should usually only be done from one node.
         *
         * It may be run multiple times - every time a new node becomes the new singleton, the callback
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

        private CompletionStage<List<BoundStatement>> greetingHandler(HelloEvent event) {
            return eventStoreRepository.store(event).thenApply( done -> TreePVector.<BoundStatement>empty());
        }


        public <T> Function<T, Done> accept(Consumer<T> f) {
            return t -> {
                f.accept(t);
                return Done.getInstance();
            };
        }
    }
}
