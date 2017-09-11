package com.lightbend.lagom.account.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Math {

  public static Double round2(Double value) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(2, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }
}
