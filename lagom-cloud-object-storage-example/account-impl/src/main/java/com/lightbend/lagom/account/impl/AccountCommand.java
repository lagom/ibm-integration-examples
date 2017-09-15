/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * This interface defines all the commands that the Account entity supports.
 * 
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface AccountCommand extends Jsonable {

  enum GetBalance implements AccountCommand, PersistentEntity.ReplyType<Double> {
    INSTANCE
  }

  /**
   * A command to add a deposit.
   * <p>
   * It has a reply type of {@link akka.Done}, which is sent back to the caller
   * when all the events emitted by this command are successfully persisted.
   */
  @Value
  public final class Deposit implements AccountCommand, Jsonable, PersistentEntity.ReplyType<Done> {
    public final double amount;

    @JsonCreator
    public Deposit(double amount) {
      assert amount >= 0.0;
      this.amount = amount;
    }

  }


  /**
   * A command to withdraw an amount.
   * <p>
   * It has a reply type of {@link akka.Done}, which is sent back to the caller
   * when all the events emitted by this command are successfully persisted.
   */
  @Value
  public final class Withdraw implements AccountCommand, Jsonable, PersistentEntity.ReplyType<Done> {
    public final double amount;

    @JsonCreator
    public Withdraw(double amount) {
      assert amount >= 0.0;
      this.amount = amount;
    }

  }

}
