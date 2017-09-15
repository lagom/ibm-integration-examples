/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * The state for the {@link Account} entity.
 */
@Value
public final class Account implements Jsonable {

  public final Double balance;

  @JsonCreator
  public Account(Double balance) {
    this.balance = balance;
  }


  public Boolean withdrawAllowed(Double amount) {
    return balance - amount > 0.0;
  }

}
