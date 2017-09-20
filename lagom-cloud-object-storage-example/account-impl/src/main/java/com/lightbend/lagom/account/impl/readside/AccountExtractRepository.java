/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import com.lightbend.lagom.account.api.Extract;
import com.lightbend.lagom.account.impl.AccountEvent;
import com.lightbend.lagom.javadsl.persistence.Offset;

import java.util.concurrent.CompletionStage;

public interface AccountExtractRepository {

  CompletionStage<Done> handleEvent(AccountEvent evt, Offset offset);

  CompletionStage<Extract> findExtract(String accountNumber, int extractNumber);
}
