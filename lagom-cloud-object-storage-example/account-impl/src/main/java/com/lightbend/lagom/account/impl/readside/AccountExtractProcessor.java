/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import com.lightbend.lagom.account.impl.AccountEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A read-side processor that subscribes to AccountEntity events and forward them to AccountExtractRepository.
 * 
 * This processor does not save offsets and consumes the full event stream from scratch each time the application is restarted.
 * This is done on purpose. The repository does not save to any database, but keeps accumulating Account Extracts in-memory before
 * archiving it in Cloud Object Storage. 
 */
public class AccountExtractProcessor extends ReadSideProcessor<AccountEvent> {

  final private AccountExtractRepositoryImpl repository;

  @Inject
  public AccountExtractProcessor(AccountExtractRepositoryImpl repository) {
    this.repository = repository;
  }


  @Override
  public PSequence<AggregateEventTag<AccountEvent>> aggregateTags() {
    return TreePVector.singleton(AccountEvent.TAG);
  }


  @Override
  public ReadSideHandler<AccountEvent> buildHandler() {

    return new ReadSideHandler<AccountEvent>() {


      @Override
      public CompletionStage<Done> globalPrepare() {
        // no global prepare, nothing to store in DB
        return CompletableFuture.completedFuture(Done.getInstance());
      }

      @Override
      public CompletionStage<Offset> prepare(AggregateEventTag<AccountEvent> tag) {
        // always start reading from first event, not offset storage
        return CompletableFuture.completedFuture(Offset.NONE);
      }

      @Override
      public Flow<Pair<AccountEvent, Offset>, Done, ?> handle() {
        return Flow.<Pair<AccountEvent, Offset>>create()
                .mapAsync(
                        1,
                        eventAndOffset -> repository.handleEvent(eventAndOffset.first(), eventAndOffset.second())
                );
      }
    };
  }


}
