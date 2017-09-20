/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.pcollections.PCollection;

import java.io.IOException;
import java.util.List;

public class Extract {


  public final String accountNumber;
  public final Integer extractNumber;
  public final double startBalance;
  public final double endBalance;
  public final Status status;
  public final List<TransactionEntry> txEntries;

  @JsonCreator
  public Extract(@JsonProperty("accountNumber") String accountNumber,
                 @JsonProperty("extractNumber") Integer extractNumber,
                 @JsonProperty("startBalance") double startBalance,
                 @JsonProperty("endBalance") double endBalance,
                 @JsonProperty("status") Status status,
                 @JsonProperty("txEntries") List<TransactionEntry> txEntries) {

    this.accountNumber = accountNumber;
    this.extractNumber = extractNumber;
    this.startBalance = startBalance;
    this.endBalance = endBalance;
    this.status = status;
    this.txEntries = txEntries;
  }

  public enum Status {
    ARCHIVED, CURRENT;
  }
}

