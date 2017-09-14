/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.pcollections.PCollection;

public class Extract {


  public final String accountNumber;
  public final Integer extractNumber;
  public final double startBalance;
  public final double endBalance;
  public final String status;
  public final PCollection<TransactionEntry> txEntries;

  @JsonCreator
  public Extract(String accountNumber,
                 Integer extractNumber,
                 double startBalance,
                 double endBalance,
                 String status,
                 PCollection<TransactionEntry> txEntries) {

    this.accountNumber = accountNumber;
    this.extractNumber = extractNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.status = status;
    this.txEntries = txEntries;
  }

  public static String ARCHIVED = "ARCHIVED";
  public static String IN_MEMORY = "IN-MEMORY";
}
