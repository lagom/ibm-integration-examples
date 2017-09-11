package com.lightbend.lagom.account.impl.readside;

import java.time.OffsetDateTime;

public interface Transaction {

  Double getAmount();

  String getLabel();

  OffsetDateTime getDateTime();

  public final class Withdraw implements Transaction {

    private final Double amount;
    private final OffsetDateTime dateTime;

    public Withdraw(Double amount, OffsetDateTime dateTime) {
      this.amount = amount;
      this.dateTime = dateTime;
    }

    @Override
    public Double getAmount() {
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

    private final Double amount;
    private final OffsetDateTime dateTime;

    public Deposit(Double amount, OffsetDateTime dateTime) {
      this.amount = amount;
      this.dateTime = dateTime;
    }

    @Override
    public Double getAmount() {
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
