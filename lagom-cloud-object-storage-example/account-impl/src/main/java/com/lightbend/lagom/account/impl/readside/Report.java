package com.lightbend.lagom.account.impl.readside;


import com.lightbend.lagom.account.impl.Math;
import org.pcollections.PCollection;
import org.pcollections.TreePVector;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Report {

  public final String accountNumber;
  public final double startBalance;
  public final double endBalance;
  public final int number;
  private final PCollection<Transaction> transactions;

  public Report(String accountNumber,
                double startBalance,
                double endBalance,
                int number,
                PCollection<Transaction> transactions) {

    this.accountNumber = accountNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.number = number;
    this.transactions = transactions;
  }

  public int totalTransactions() {
    return transactions.size();
  }

  public String getId() {
    return buildId(number);
  }

  private String buildId(int number) {
    return "account-report_" + accountNumber + "_" + number;
  }

  public List<String> getAllIds() {
    List<String> ids = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      ids.add(buildId(i));
    }
    return ids;
  }

  /**
   * Builds a new report based on this one.
   *
   * New Report will have no transactions and its start balance equals the previous end balance.
   */
  public Report newReport() {
    return new Report(
            accountNumber,
            endBalance, // current balance is start balance in new report
            endBalance,
            number + 1, // increase report number by 1
            TreePVector.empty()
    );
  }

  public static Report newReport(String accountNumber) {
    return new Report(
            accountNumber,
            0.0,
            0.0,
            1, // number start with 1
            TreePVector.empty()
    );
  }

  public Report newDeposit(double amount, OffsetDateTime dateTime) {
    return newDeposit(new Transaction.Deposit(amount, dateTime));
  }

  public Report newDeposit(Transaction.Deposit deposit) {
    return new Report(
            accountNumber,
            startBalance,
            Math.round2(endBalance + deposit.getAmount()),
            number,
            transactions.plus(deposit)
    );
  }


  public Report newWithdraw(double amount, OffsetDateTime dateTime) {
    return newWithdraw(new Transaction.Withdraw(amount, dateTime));
  }

  public Report newWithdraw(Transaction.Withdraw withdraw) {
    return new Report(
            accountNumber,
            startBalance,
            Math.round2(endBalance - withdraw.getAmount()),
            number,
            transactions.plus(withdraw)
    );
  }

  public PCollection<Transaction> getTransactions() {
    return transactions;
  }
}
