/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lightbend.lagom.account.api.TransactionEntry;
import org.pcollections.TreePVector;

import java.util.List;
import java.util.stream.Collectors;

public class ExtractWriter {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());
  }



  static String write(Extract extract) {
    try {
      return mapper.writeValueAsString(toApi(extract));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


  private static com.lightbend.lagom.account.api.Extract toApi(Extract extract) {

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
      extract.archived ? com.lightbend.lagom.account.api.Extract.ARCHIVED : com.lightbend.lagom.account.api.Extract.IN_MEMORY,
      TreePVector.from(lines)
    );
  }

}
