package com.lightbend.lagom.hello.impl.readside;

public interface Transaction {

  Double getAmount();

  public final class Withdraw implements Transaction {

    private final Double amount;

    public Withdraw(Double amount) {
      this.amount = amount;
    }

    @Override
    public Double getAmount() {
      return amount;
    }
  }

  public final class Deposit implements Transaction {

    private final Double amount;

    public Deposit(Double amount) {
      this.amount = amount;
    }

    @Override
    public Double getAmount() {
      return amount;
    }
  }
}
