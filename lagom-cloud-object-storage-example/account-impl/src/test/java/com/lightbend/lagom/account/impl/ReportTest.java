package com.lightbend.lagom.account.impl;

import com.lightbend.lagom.account.impl.readside.Report;
import org.junit.Test;

import java.time.OffsetDateTime;

import static org.junit.Assert.*;

public class ReportTest {

  @Test
  public void testReport() {

    Report r1 = Report.newReport("abc");

    Report r2 = r1.newDeposit(10.0, OffsetDateTime.now())
                  .newDeposit(20.0, OffsetDateTime.now())
                  .newWithdraw(20.0, OffsetDateTime.now())
                  .newDeposit(100.0, OffsetDateTime.now());


    assertEquals(4, r2.totalTransactions());
    assertEquals(0.0, r2.startBalance, 0.0);
    assertEquals(110.0, r2.endBalance, 0.0);

    assertEquals(1, r2.reportNumber);



    Report r3 = r2.newReport();
    assertEquals(2, r3.reportNumber);

    // new reports has no transactions
    assertEquals(0, r3.totalTransactions());
    assertEquals(110.0, r3.startBalance, 0.0);
    assertEquals(110.0, r3.endBalance, 0.0);

  }
}
