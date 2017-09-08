/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.concurrent.Immutable;

/**
 * This interface defines all the events that the Account entity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface AccountEvent extends Jsonable, AggregateEvent<AccountEvent> {

  AggregateEventTag<AccountEvent> TAG = AggregateEventTag.of(AccountEvent.class);

  String getNumber();
  Double getAmount();

  @Override
  default AggregateEventTag<AccountEvent> aggregateTag() {
    return TAG;
  }

  @SuppressWarnings("serial")
  @Immutable
  @JsonDeserialize
  public final class DepositExecuted implements AccountEvent {

    private final String number;
    private final Double amount;

    @Override
    public String getNumber() {
      return number;
    }

    @Override
    public Double getAmount() {
      return amount;
    }


    @JsonCreator
    public DepositExecuted(Double amount, String number) {
      this.amount = amount;
      this.number = number;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      DepositExecuted that = (DepositExecuted) o;

      if (number != null ? !number.equals(that.number) : that.number != null) return false;
      return amount != null ? amount.equals(that.amount) : that.amount == null;
    }

    @Override
    public int hashCode() {
      int result = number != null ? number.hashCode() : 0;
      result = 31 * result + (amount != null ? amount.hashCode() : 0);
      return result;
    }

  }

  @SuppressWarnings("serial")
  @Immutable
  @JsonDeserialize
  public final class WithdrawExecuted implements AccountEvent {

    private final String number;
    private final Double amount;

    @Override
    public String getNumber() {
      return number;
    }

    @Override
    public Double getAmount() {
      return amount;
    }
    @JsonCreator
    public WithdrawExecuted(Double amount, String number) {
      this.amount = amount;
      this.number = number;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      WithdrawExecuted that = (WithdrawExecuted) o;

      if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
      return number != null ? number.equals(that.number) : that.number == null;
    }

    @Override
    public int hashCode() {
      int result = amount != null ? amount.hashCode() : 0;
      result = 31 * result + (number != null ? number.hashCode() : 0);
      return result;
    }
  }
}
