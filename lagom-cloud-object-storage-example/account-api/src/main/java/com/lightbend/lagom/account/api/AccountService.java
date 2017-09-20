/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

public interface AccountService extends Service {

  ServiceCall<Transaction, NotUsed> deposit(String accountNumber);

  ServiceCall<Transaction, NotUsed> withdraw(String accountNumber);

  ServiceCall<NotUsed, Double> balance(String accountNumber);

  ServiceCall<NotUsed, Extract> extract(String accountNumber, int extractNumber);

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("account").withCalls(
        pathCall("/api/account/:accountNumber/balance", this::balance),
        pathCall("/api/account/:accountNumber/deposit",  this::deposit),
        pathCall("/api/account/:accountNumber/withdraw", this::withdraw),
        pathCall("/api/account/:accountNumber/extract/:extractNumber", this::extract)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
