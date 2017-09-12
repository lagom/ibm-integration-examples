/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.OffsetDateTime;

public class TransactionEntry {

  public final String label;
  public final OffsetDateTime dateTime;
  public final double amount;

  @JsonCreator
  public TransactionEntry(String label, OffsetDateTime dateTime, double amount) {
    this.label = label;
    this.dateTime = dateTime;
    this.amount = amount;
  }

}
