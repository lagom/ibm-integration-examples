/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class AccountEntityTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("AccountEntityTest");
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testDeposit() {

    PersistentEntityTestDriver<AccountCommand, AccountEvent, Account> driver =
            new PersistentEntityTestDriver<>(system, new AccountEntity(), "account-1");


    // make a deposit of 100
    {
      Outcome<AccountEvent, Account> outcome = driver.run(new AccountCommand.Deposit(100.0));
      assertEquals(1, outcome.events().size());
      assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    }


    // check startBalance
    {
      Outcome<AccountEvent, Account> outcome = driver.run(AccountCommand.GetBalance.INSTANCE);

      // no events are emitted for read-only command
      assertEquals(0, outcome.events().size());
      // startBalance should be 100.0
      assertEquals(100.0, outcome.getReplies().get(0));
    }

  }

  @Test
  public void testWithdraw() {

    PersistentEntityTestDriver<AccountCommand, AccountEvent, Account> driver =
            new PersistentEntityTestDriver<>(system, new AccountEntity(), "account-1");

    // make a deposit of 200
    {
      Outcome<AccountEvent, Account> outcome = driver.run(new AccountCommand.Deposit(200.0));
      assertEquals(1, outcome.events().size());
      assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    }

    // withdraw 50
    {
      Outcome<AccountEvent, Account> outcome = driver.run(new AccountCommand.Withdraw(50.0));
      assertEquals(1, outcome.events().size());
      assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    }


    // check startBalance
    {
      Outcome<AccountEvent, Account> outcome = driver.run(AccountCommand.GetBalance.INSTANCE);

      // no events are emitted for read-only command
      assertEquals(0, outcome.events().size());
      // startBalance should be 60.0
      assertEquals(150.0, outcome.getReplies().get(0));
    }

  }

  @Test
  public void testRejectedWithdraw() {

    PersistentEntityTestDriver<AccountCommand, AccountEvent, Account> driver =
            new PersistentEntityTestDriver<>(system, new AccountEntity(), "account-1");


    Outcome<AccountEvent, Account> outcome = driver.run(new AccountCommand.Withdraw(100.0));
    assertEquals(0, outcome.events().size());

    if (outcome.getReplies().get(0) instanceof PersistentEntity.InvalidCommandException) {
      PersistentEntity.InvalidCommandException exception = (PersistentEntity.InvalidCommandException) outcome.getReplies().get(0);
      assertEquals("Insufficient balance", exception.message());
    }
  }

}
