/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;
import com.lightbend.lagom.account.api.Extract.Status;

import com.lightbend.lagom.account.api.TransactionEntry;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PCollection;
import org.pcollections.TreePVector;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class uses Project Lombok (www.projectlombok.org) to generate {@code with} methods for its properties.
 * You may want to add Lombok plugin to your IDE to remove compilation errors.
 * (note: maven will compile without errors)
 */
@Value
public class Extract {

  public final String accountNumber;

  @Wither(AccessLevel.PRIVATE)
  public final double startBalance;

  @Wither(AccessLevel.PRIVATE)
  public final double endBalance;

  @Wither(AccessLevel.PRIVATE)
  public final int extractNumber;

  @Wither(AccessLevel.PUBLIC)
  public final boolean archived;

  @Wither(AccessLevel.PUBLIC)
  private final PCollection<Transaction> transactions;

  public Extract(String accountNumber,
                 double startBalance,
                 double endBalance,
                 int extractNumber,
                 boolean archived,
                 PCollection<Transaction> transactions) {

    this.accountNumber = accountNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.extractNumber = extractNumber;
    this.archived = archived;
    this.transactions = transactions;
  }

  public int totalTransactions() {
    return transactions.size();
  }

  /**
   * Builds a new extract based on this one.
   *
   * New extract will have no transactions and its start balance equals the previous end balance.
   */
  public Extract newExtract() {
    return withExtractNumber(extractNumber + 1)
            // current endBalance becomes the startBalance in new extract
            .withStartBalance(endBalance) 
            // clear transactions
            .withTransactions(TreePVector.empty());
  }

  public static Extract newExtract(String accountNumber) {
    return new Extract(
            accountNumber,
            0.0,
            0.0,
            1, // extractNumber start with 1
            false,
            TreePVector.empty()
    );
  }

  public Extract newDeposit(double amount, OffsetDateTime dateTime) {
    return newDeposit(new Transaction.Deposit(amount, dateTime));
  }

  public Extract newDeposit(Transaction.Deposit deposit) {
    return withEndBalance(endBalance + deposit.getAmount())
            .withTransactions(transactions.plus(deposit));
  }


  public Extract newWithdraw(double amount, OffsetDateTime dateTime) {
    return newWithdraw(new Transaction.Withdraw(amount, dateTime));
  }

  public Extract newWithdraw(Transaction.Withdraw withdraw) {
    return withEndBalance(endBalance - withdraw.getAmount())
            .withTransactions(transactions.plus(withdraw));
  }

  public PCollection<Transaction> getTransactions() {
    return transactions;
  }

  public String getId() {
   return buildId(accountNumber, extractNumber);
  }

  public static String buildId(String accountNumber, int extractNumber) {
    return accountNumber + "#" + extractNumber;
  }

  public static com.lightbend.lagom.account.api.Extract toApi(Extract extract) {

    List<TransactionEntry> lines =
            extract.getTransactions()
                    .stream()
                    .map( tx -> new TransactionEntry(tx.getLabel(), tx.getDateTime(), tx.getAmount()))
                    .collect(Collectors.toList());

    return new com.lightbend.lagom.account.api.Extract(
            extract.accountNumber,
            extract.extractNumber,
            extract.startBalance,
            extract.endBalance,
            extract.archived ? Status.ARCHIVED : Status.CURRENT,
            lines
    );
  }
}
