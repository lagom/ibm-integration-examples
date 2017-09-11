/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;

import javax.annotation.concurrent.Immutable;

/**
 * The state for the {@link Account} entity.
 */
@SuppressWarnings("serial")
@Immutable
@JsonDeserialize
public final class Account implements Jsonable {

  public final Double balance;

  @JsonCreator
  public Account(Double balance) {
    this.balance = balance;
  }


  public Boolean withdrawAllowed(Double amount) {
    return balance - amount > 0.0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Account account = (Account) o;

    return balance != null ? balance.equals(account.balance) : account.balance == null;
  }

  @Override
  public int hashCode() {
    return balance != null ? balance.hashCode() : 0;
  }
}
