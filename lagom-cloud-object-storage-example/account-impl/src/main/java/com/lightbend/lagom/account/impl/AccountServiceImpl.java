/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import akka.NotUsed;
import com.lightbend.lagom.account.api.*;
import com.lightbend.lagom.account.impl.readside.AccountExtractProcessor;
import com.lightbend.lagom.account.impl.readside.AccountExtractRepository;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

import com.lightbend.lagom.javadsl.persistence.ReadSide;

/**
 * Implementation of the AccountService.
 */
public class AccountServiceImpl implements AccountService {

  private final PersistentEntityRegistry persistentEntityRegistry;
  private final AccountExtractRepository extractRepository;

  @Inject
  public AccountServiceImpl(AccountExtractRepository extractRepository,
                            PersistentEntityRegistry persistentEntityRegistry,
                            ReadSide readSide) {

    this.persistentEntityRegistry = persistentEntityRegistry;
    this.extractRepository = extractRepository;
    persistentEntityRegistry.register(AccountEntity.class);
    readSide.register(AccountExtractProcessor.class);
  }

  @Override
  public ServiceCall<Transaction, NotUsed> deposit(String accountNumber) {
    return request -> {
      // look up account by account extractNumber
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
      // look up account by account extractNumber
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
      // look up account by account extractNumber
      PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, accountNumber);
      // forward command to entity
      return ref.ask(AccountCommand.GetBalance.INSTANCE).thenApply(d -> Math.round2(d));
    };
  }

  @Override
  public ServiceCall<NotUsed, Extract> extract(String accountNumber, int extractNumber) {
    return request -> {
      return extractRepository.findExtract(accountNumber, extractNumber);
    };
  }



}
