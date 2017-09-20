package com.lightbend.lagom.account.impl;

import com.lightbend.lagom.account.impl.readside.Extract;
import org.junit.Test;

import java.time.OffsetDateTime;

import static org.junit.Assert.*;

public class ExtractTest {

  @Test
  public void testExtract() {

    Extract r1 = Extract.newExtract("abc");

    Extract r2 = r1.newDeposit(10.0, OffsetDateTime.now())
            .newDeposit(20.0, OffsetDateTime.now())
            .newWithdraw(20.0, OffsetDateTime.now())
            .newDeposit(100.0, OffsetDateTime.now());


    assertEquals(4, r2.totalTransactions());
    assertEquals(0.0, r2.startBalance, 0.0);
    assertEquals(110.0, r2.endBalance, 0.0);

    assertEquals(1, r2.extractNumber);


    Extract r3 = r2.newExtract();
    assertEquals(2, r3.extractNumber);

    // new extracts has no transactions
    assertEquals(0, r3.totalTransactions());
    assertEquals(110.0, r3.startBalance, 0.0);
    assertEquals(110.0, r3.endBalance, 0.0);

  }
}
