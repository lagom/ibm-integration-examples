package com.lightbend.lagom.account.impl.readside;


import com.lightbend.lagom.account.impl.Math;
import org.pcollections.PCollection;
import org.pcollections.TreePVector;

import java.time.OffsetDateTime;

public class Extract {

  public final String accountNumber;
  public final double startBalance;
  public final double endBalance;
  public final int extractNumber;
  private final PCollection<Transaction> transactions;

  public Extract(String accountNumber,
                 double startBalance,
                 double endBalance,
                 int extractNumber,
                 PCollection<Transaction> transactions) {

    this.accountNumber = accountNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.extractNumber = extractNumber;
    this.transactions = transactions;
  }

  public int totalTransactions() {
    return transactions.size();
  }

  /**
   * Builds a new extract based on this one.
   *
   * New Extract will have no transactions and its start balance equals the previous end balance.
   */
  public Extract newExtract() {
    return new Extract(
            accountNumber,
            endBalance, // current balance is start balance in new extract
            endBalance,
            extractNumber + 1, // increase extractNumber by 1
            TreePVector.empty()
    );
  }

  public static Extract newExtract(String accountNumber) {
    return new Extract(
            accountNumber,
            0.0,
            0.0,
            1, // extractNumber start with 1
            TreePVector.empty()
    );
  }

  public Extract newDeposit(double amount, OffsetDateTime dateTime) {
    return newDeposit(new Transaction.Deposit(amount, dateTime));
  }

  public Extract newDeposit(Transaction.Deposit deposit) {
    return new Extract(
            accountNumber,
            startBalance,
            Math.round2(endBalance + deposit.getAmount()),
            extractNumber,
            transactions.plus(deposit)
    );
  }


  public Extract newWithdraw(double amount, OffsetDateTime dateTime) {
    return newWithdraw(new Transaction.Withdraw(amount, dateTime));
  }

  public Extract newWithdraw(Transaction.Withdraw withdraw) {
    return new Extract(
            accountNumber,
            startBalance,
            Math.round2(endBalance - withdraw.getAmount()),
            extractNumber,
            transactions.plus(withdraw)
    );
  }

  public PCollection<Transaction> getTransactions() {
    return transactions;
  }
}
