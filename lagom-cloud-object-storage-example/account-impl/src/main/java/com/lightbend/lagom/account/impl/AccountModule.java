/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.account.impl.readside.AccountExtractRepository;
import com.lightbend.lagom.account.impl.readside.AccountExtractRepositoryImpl;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.account.api.AccountService;

/**
 * The module that binds the AccountService so that it can be served.
 */
public class AccountModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(AccountService.class, AccountServiceImpl.class);
    bind(AccountExtractRepository.class).to(AccountExtractRepositoryImpl.class);
  }
}
