package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Storage {

  private Logger logger = LoggerFactory.getLogger(getClass());

  CompletionStage<Done> save(String id, String payload) {
    logger.debug("Uploading report for " + id);
    logger.debug("\n\n" + payload);
    return CompletableFuture.completedFuture(Done.getInstance());
  }

}
