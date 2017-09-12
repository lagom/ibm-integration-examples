package com.lightbend.lagom.account.impl.readside;

import org.apache.commons.lang3.StringUtils;

public class ReportWriter {
  static String write(Report report) {

    StringBuilder sb = new StringBuilder();

    sb.append("ACCOUNT: ").append(report.accountNumber).append("\n");
    sb.append("REPORT: ").append(report.getId()).append("\n");

    sb.append("---------------------------------------------------------\n");
    sb.append("START BALANCE\t\t\t\t\t").append(report.startBalance).append("\n");

    for (Transaction tx: report.getTransactions()) {
      sb.append(StringUtils.rightPad(tx.getLabel(), 8))
        .append("\t")
        .append(tx.getDateTime())
        .append("\t")
        .append(tx.getAmount())
        .append("\n");

    }

    sb.append("END BALANCE\t\t\t\t\t").append(report.endBalance).append("\n");
    sb.append("---------------------------------------------------------\n");

    return sb.toString();

  }
}
