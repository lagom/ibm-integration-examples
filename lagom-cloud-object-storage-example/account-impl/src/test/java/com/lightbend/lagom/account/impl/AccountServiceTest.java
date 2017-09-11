/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import com.lightbend.lagom.account.api.AccountService;
import org.junit.Test;

public class AccountServiceTest {

//  @Test
//  public void shouldStorePersonalizedGreeting() throws Exception {
//    withServer(defaultSetup().withCassandra(true), server -> {
//      AccountService service = server.client(AccountService.class);
//
//      String msg1 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Account, Alice!", msg1); // default greeting
//
//      service.useGreeting("Alice").invoke(new GreetingMessage("Hi")).toCompletableFuture().get(5, SECONDS);
//      String msg2 = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Hi, Alice!", msg2);
//
//      String msg3 = service.hello("Bob").invoke().toCompletableFuture().get(5, SECONDS);
//      assertEquals("Account, Bob!", msg3); // default greeting
//    });
//  }

}
