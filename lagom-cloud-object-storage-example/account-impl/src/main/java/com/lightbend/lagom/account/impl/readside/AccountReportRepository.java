package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.account.impl.AccountEvent;
import com.lightbend.lagom.account.impl.AccountEvent.DepositExecuted;
import com.lightbend.lagom.account.impl.AccountEvent.WithdrawExecuted;
import com.lightbend.lagom.javadsl.persistence.Offset;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AccountReportRepository {

  private final Map<String, Report> reports = new HashMap<>();
  private final Storage storage;

  @Inject
  public AccountReportRepository(Storage storage) {
    this.storage = storage;
  }

  /**
   * Handle the post added event.
   */
  CompletionStage<Done> handleEvent(AccountEvent evt, Offset offset) {

    // match events per type
    if (evt instanceof DepositExecuted) {
      Report report = reports.getOrDefault(evt.getNumber(), Report.newReport(evt.getNumber()));
      return save(report.newDeposit(evt.getAmount(), evt.getDateTime()));

    } else if (evt instanceof WithdrawExecuted) {
      Report report = Preconditions.checkNotNull(reports.get(evt.getNumber()), "Withdraw can't be the first generated event");
      return save(report.newWithdraw(evt.getAmount(), evt.getDateTime()));

    } else {
      // keep going on unmatched events
      return CompletableFuture.completedFuture(Done.getInstance());
    }


  }


  private CompletionStage<Done> save(Report report) {

    if (report.totalTransactions() == 5) {
      // generate payload
      String payload = ReportWriter.write(report);

      // save to cloud storage
      return storage
              .save(report.getId(), payload)
              .thenApply(
                      done -> {
                        // once file is uploaded, we can start a new report
                        // note that this may fail, in which case no reset will be done
                        // and report will be regenerated
                        saveInMemory(report.newReport());
                        return Done.getInstance();
                      }
              );

    } else {
      // save in memory only
      saveInMemory(report);
      return CompletableFuture.completedFuture(Done.getInstance());
    }

  }

  private void saveInMemory(Report report) {
    reports.put(report.accountNumber, report);
  }
}
