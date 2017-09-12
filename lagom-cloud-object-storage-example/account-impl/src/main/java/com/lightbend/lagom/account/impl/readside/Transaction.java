/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import java.time.OffsetDateTime;

public interface Transaction {

  double getAmount();

  String getLabel();

  OffsetDateTime getDateTime();

  public final class Withdraw implements Transaction {

    private final Double amount;
    private final OffsetDateTime dateTime;

    public Withdraw(double amount, OffsetDateTime dateTime) {
      this.amount = amount;
      this.dateTime = dateTime;
    }

    @Override
    public double getAmount() {
      return amount;
    }

    @Override
    public String getLabel() {
      return "WITHDRAW";
    }

    @Override
    public OffsetDateTime getDateTime() {
      return dateTime;
    }
  }

  public final class Deposit implements Transaction {

    private final double amount;
    private final OffsetDateTime dateTime;

    public Deposit(double amount, OffsetDateTime dateTime) {
      this.amount = amount;
      this.dateTime = dateTime;
    }

    @Override
    public double getAmount() {
      return amount;
    }

    @Override
    public String getLabel() {
      return "DEPOSIT";
    }

    @Override
    public OffsetDateTime getDateTime() {
      return dateTime;
    }
  }
}
