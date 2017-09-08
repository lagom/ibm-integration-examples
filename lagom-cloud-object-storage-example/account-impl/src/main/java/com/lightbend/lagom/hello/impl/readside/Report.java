package com.lightbend.lagom.hello.impl.readside;


import java.util.ArrayList;
import java.util.List;

public class Report {

  public final String accountNumber;
  public final Double startBalance;
  public final Double endBalance;
  private final List<Transaction> transactions;

  public Report(String accountNumber,
                Double startBalance,
                Double endBalance,
                List<Transaction> transactions) {

    this.accountNumber = accountNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.transactions = transactions;
  }

  public int totalTransactions() {
    return transactions.size();
  }

  /**
   * Builds a new report based on this one.
   *
   * New Report will have no transactions and its start balance equals the previous end balance.
   * Finally, a newly created Raport does not have a end balance because it has no transactions.
   */
  public Report newReport() {
    // when removing transactions,
    return new Report(accountNumber, endBalance, 0.0, new ArrayList<>());
  }

  public static Report newReport(String accountNumber) {
    return new Report(accountNumber, 0.0, 0.0, new ArrayList<>());
  }


  public Report newDeposit(Transaction.Deposit deposit) {
    return new Report(
            accountNumber,
            startBalance,
            endBalance + deposit.getAmount(),
            newTransactions(deposit)
    );
  }

  public Report newWithdraw(Transaction.Withdraw withdraw) {
    return new Report(
            accountNumber,
            startBalance,
            endBalance - withdraw.getAmount(),
            newTransactions(withdraw)
    );
  }

  private List<Transaction> newTransactions(Transaction tx) {
    List<Transaction> newTx = new ArrayList<>(transactions.size() + 1);
    newTx.addAll(transactions);
    newTx.add(tx);
    return newTx;
  }
}
