/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;

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
  @SuppressWarnings("serial")
  @Immutable
  @JsonDeserialize
  public final class Deposit implements AccountCommand, Jsonable, PersistentEntity.ReplyType<Done> {
    public final Double amount;

    @JsonCreator
    public Deposit(Double amount) {
      this.amount = Preconditions.checkNotNull(amount, "amount");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Deposit deposit = (Deposit) o;

      return amount != null ? amount.equals(deposit.amount) : deposit.amount == null;
    }

    @Override
    public int hashCode() {
      return amount != null ? amount.hashCode() : 0;
    }

  }


  /**
   * A command to withdraw an amount.
   * <p>
   * It has a reply type of {@link akka.Done}, which is sent back to the caller
   * when all the events emitted by this command are successfully persisted.
   */
  @SuppressWarnings("serial")
  @Immutable
  @JsonDeserialize
  public final class Withdraw implements AccountCommand, Jsonable, PersistentEntity.ReplyType<Done> {
    public final Double amount;

    @JsonCreator
    public Withdraw(Double amount) {
      this.amount = Preconditions.checkNotNull(amount, "amount");
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Withdraw withdraw = (Withdraw) o;

      return amount != null ? amount.equals(withdraw.amount) : withdraw.amount == null;
    }

    @Override
    public int hashCode() {
      return amount != null ? amount.hashCode() : 0;
    }
  }

}
