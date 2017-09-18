/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.account.api.ExtractCodec;
import com.lightbend.lagom.account.impl.AccountEvent;
import com.lightbend.lagom.account.impl.AccountEvent.DepositExecuted;
import com.lightbend.lagom.account.impl.AccountEvent.WithdrawExecuted;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.Offset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This AccountExtractRepositoryImpl class is an in-memory implementation only used demo
 * purposes. Don't use this in non-demo code ever!
 */
@Singleton
public class AccountExtractRepositoryImpl implements AccountExtractRepository {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final Map<String, Extract> extracts = new HashMap<>();
  private final Storage storage;

  @Inject
  public AccountExtractRepositoryImpl(Storage storage) {
    this.storage = storage;
  }

  /**
   * Handle the post added event.
   */
  public CompletionStage<Done> handleEvent(AccountEvent evt, Offset offset) {

    // match events per type
    if (evt instanceof DepositExecuted) {
      Extract extract = extracts.getOrDefault(evt.getAccountNumber(), Extract.newExtract(evt.getAccountNumber()));
      return save(extract.newDeposit(evt.getAmount(), evt.getDateTime()));

    } else if (evt instanceof WithdrawExecuted) {
      Extract extract = Preconditions.checkNotNull(extracts.get(evt.getAccountNumber()), "Withdraw can't be the first generated event");
      return save(extract.newWithdraw(evt.getAmount(), evt.getDateTime()));

    } else {
      // keep going on unmatched events
      return CompletableFuture.completedFuture(Done.getInstance());
    }


  }


  private CompletionStage<Done> save(Extract extract) {

    logger.info("Extract " + extract.getId() + " has " + extract.totalTransactions() + " transactions.");

    if (extract.totalTransactions() == 5) {
      logger.info("Archiving extract: " + extract.getId());
      // generate payload
      String payload = ExtractCodec.encode(Extract.toApi(extract.withArchived(true)));

      // save to cloud storage
      return storage
              .save(extract.getId(), payload)
              .thenApply(
                      done -> {
                        // once file is uploaded, we can start a new extract
                        // note that this may fail, in which case no reset will be done
                        // and extract will be regenerated
                        saveInMemory(extract.newExtract());
                        return Done.getInstance();
                      }
              );

    } else {
      // save in memory only
      logger.info("Saving extract in-memory: " + extract.getId());
      saveInMemory(extract);
      return CompletableFuture.completedFuture(Done.getInstance());
    }

  }

  private void saveInMemory(Extract extract) {
    extracts.put(extract.accountNumber, extract);
  }

  @Override
  public CompletionStage<com.lightbend.lagom.account.api.Extract> findExtract(String accountNumber, int extractNumber) {
    Extract extract = extracts.get(accountNumber);
    if (extract != null && extract.extractNumber == extractNumber) {
      return CompletableFuture.completedFuture(Extract.toApi(extract));
    } else {
      String key = Extract.buildId(accountNumber, extractNumber);
      return storage
              .fetch(key)
//              .thenApply(ExtractCodec::decode)
              .thenApply(ExtractCodec::decode)
              .exceptionally( exp -> {
                logger.error("Error while fetching archived report", exp);
                throw new NotFound(key + " extract not found");
              } );
    }
  }
}
