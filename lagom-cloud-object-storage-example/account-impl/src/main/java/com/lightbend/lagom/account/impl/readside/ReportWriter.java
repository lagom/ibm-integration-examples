package com.lightbend.lagom.account.impl.readside;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lightbend.lagom.account.api.TransactionEntry;
import org.pcollections.TreePVector;

import java.util.List;
import java.util.stream.Collectors;

public class ReportWriter {

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());
  }



  static String write(Report report) {
    try {
      return mapper.writeValueAsString(toApi(report));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


  private static com.lightbend.lagom.account.api.Report toApi(Report report) {

    List<TransactionEntry> lines =
            report.getTransactions()
              .stream()
              .map( tx -> new TransactionEntry(tx.getLabel(), tx.getDateTime(), tx.getAmount()))
              .collect(Collectors.toList());

    return new com.lightbend.lagom.account.api.Report(
      report.accountNumber,
      report.reportNumber,
      report.startBalance,
      report.endBalance,
      TreePVector.from(lines)
    );
  }

}
