/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import akka.NotUsed;
import com.lightbend.lagom.account.api.*;
import com.lightbend.lagom.account.impl.readside.AccountBalanceReportProcessor;
import com.lightbend.lagom.account.impl.readside.AccountReportRepository;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.persistence.ReadSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Implementation of the AccountService.
 */
public class AccountServiceImpl implements AccountService {

  private final PersistentEntityRegistry persistentEntityRegistry;
  private final AccountReportRepository reportRepository;

  @Inject
  public AccountServiceImpl(AccountReportRepository reportRepository,
                            PersistentEntityRegistry persistentEntityRegistry,
                            ReadSide readSide) {

    this.persistentEntityRegistry = persistentEntityRegistry;
    this.reportRepository = reportRepository;
    persistentEntityRegistry.register(AccountEntity.class);
    readSide.register(AccountBalanceReportProcessor.class);
  }

  @Override
  public ServiceCall<Transaction, NotUsed> deposit(String accountNumber) {
    return request -> {
      // look up account by account reportNumber
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, accountNumber);
      // forward command to entity
      return ref
              .ask(new AccountCommand.Deposit(Math.round2(request.amount)))
              .thenApply(s -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<Transaction, NotUsed> withdraw(String accountNumber) {
    return request -> {
      // look up account by account reportNumber
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, accountNumber);
      // forward command to entity
      return ref
              .ask(new AccountCommand.Withdraw(Math.round2(request.amount)))
              .thenApply(s -> NotUsed.getInstance());
    };
  }

  @Override
  public ServiceCall<NotUsed, Double> balance(String accountNumber) {
    return request -> {
      // look up account by account reportNumber
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, accountNumber);
      // forward command to entity
      return ref.ask(AccountCommand.GetBalance.INSTANCE).thenApply(d -> Math.round2(d));
    };
  }

  @Override
  public ServiceCall<NotUsed, String> report(String accountNumber, int reportNumber) {
    return request -> {
      return reportRepository.findReportByNumber(accountNumber, reportNumber);
    };
  }



}
