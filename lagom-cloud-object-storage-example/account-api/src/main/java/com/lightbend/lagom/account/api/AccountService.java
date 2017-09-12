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

  ServiceCall<NotUsed, String> report(String accountNumber, int reportNumber);

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("account").withCalls(
        pathCall("/api/account/:number/balance", this::balance),
        pathCall("/api/account/:number/deposit",  this::deposit),
        pathCall("/api/account/:number/withdraw", this::withdraw),
        pathCall("/api/account/:number/report/:report", this::report)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
