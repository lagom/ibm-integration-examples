package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import com.lightbend.lagom.account.impl.AccountEvent;
import com.lightbend.lagom.javadsl.persistence.Offset;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AccountReportRepository {

  CompletionStage<Done> handleEvent(AccountEvent evt, Offset offset);

  Optional<Report> findByAccountNumber(String accountNumber);

  CompletionStage<String> findReportByNumber(String accountNumber, int reportNumber);
}
