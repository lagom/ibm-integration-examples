package com.lightbend.lagom.account.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.pcollections.PCollection;

public class Report {


  public final String accountNumber;
  public final Integer extractNumber;
  public final double startBalance;
  public final double endBalance;
  public final PCollection<TransactionEntry> transactionEntries;

  @JsonCreator
  public Report(String accountNumber,
                Integer extractNumber,
                double startBalance,
                double endBalance,
                PCollection<TransactionEntry> transactionEntries) {

    this.accountNumber = accountNumber;
    this.extractNumber = extractNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.transactionEntries = transactionEntries;
  }

}
