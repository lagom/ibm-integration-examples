package com.lightbend.lagom.account.impl;

import com.lightbend.lagom.account.impl.readside.Report;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ReportTest {

  @Test
  public void testReport() {

    Report r1 = Report.newReport("abc");

    Report r2 = r1.newDeposit(10.0)
                  .newDeposit(20.0)
                  .newWithdraw(20.0)
                  .newDeposit(100.0);


    assertEquals(4, r2.totalTransactions());
    assertEquals(0.0, r2.startBalance, 0.0);
    assertEquals(110.0, r2.endBalance, 0.0);

    assertEquals("abc_1", r2.getId());
    assertEquals(Collections.singletonList("abc_1"), r2.getAllIds());


    Report r3 = r2.newReport();
    assertEquals(Arrays.asList("abc_1", "abc_2"), r3.getAllIds());

    // new reports has not transactions
    assertEquals(0, r3.totalTransactions());
    assertEquals(110.0, r3.startBalance, 0.0);
    assertEquals(0.0, r3.endBalance, 0.0);

  }
}
