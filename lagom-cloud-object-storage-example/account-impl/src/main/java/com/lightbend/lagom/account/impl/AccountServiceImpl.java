/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import akka.NotUsed;
import com.lightbend.lagom.account.api.Transaction;
import com.lightbend.lagom.account.impl.readside.AccountBalanceReportProcessor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

import com.lightbend.lagom.account.api.AccountService;
import com.lightbend.lagom.javadsl.persistence.ReadSide;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of the AccountService.
 */
public class AccountServiceImpl implements AccountService {

  private final PersistentEntityRegistry persistentEntityRegistry;

  @Inject
  public AccountServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ReadSide readSide) {
    this.persistentEntityRegistry = persistentEntityRegistry;
    persistentEntityRegistry.register(AccountEntity.class);
    readSide.register(AccountBalanceReportProcessor.class);
  }

  @Override
  public ServiceCall<Transaction, NotUsed> deposit(String number) {
    return request -> {
      // look up account by account number
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, number);
      // forward command to entity
      return ref
              .ask(new AccountCommand.Deposit(Math.round2(request.amount)))
              .thenApply(s -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<Transaction, NotUsed> withdraw(String number) {
    return request -> {
      // look up account by account number
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, number);
      // forward command to entity
      return ref
              .ask(new AccountCommand.Withdraw(Math.round2(request.amount)))
              .thenApply(s -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<NotUsed, Double> balance(String number) {
    return request -> {
      // look up account by account number
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, number);
      // forward command to entity
      return ref.ask(AccountCommand.GetBalance.INSTANCE).thenApply(d -> Math.round2(d));
    };
  }

}
