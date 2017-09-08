/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import akka.Done;


public class AccountEntity extends PersistentEntity<AccountCommand, AccountEvent, Account> {

  @Override
  public Behavior initialBehavior(Optional<Account> snapshotState) {

    BehaviorBuilder builder = newBehaviorBuilder(snapshotState.orElse(new Account(0.0)));


    builder.setReadOnlyCommandHandler(
            AccountCommand.GetBalance.class,
            (cmd, ctx) -> ctx.reply(state().balance)
    );


    builder.setCommandHandler(
            AccountCommand.Deposit.class,
            (cmd, ctx) -> {
              return ctx.thenPersist(
                      new AccountEvent.DepositExecuted(cmd.amount, entityId()),
                      evt -> ctx.reply(Done.getInstance())
              );
            }
    );

    builder.setCommandHandler(
            AccountCommand.Withdraw.class,
            (cmd, ctx) -> {
              if (state().withdrawAllowed(cmd.amount)) {
                return ctx.thenPersist(
                        new AccountEvent.WithdrawExecuted(cmd.amount, entityId()),
                        evt -> ctx.reply(Done.getInstance())
                );
              } else {
                ctx.invalidCommand("Insufficient balance");
                return ctx.done();
              }
            }
    );

    builder.setEventHandler(
            AccountEvent.DepositExecuted.class,
            evt -> new Account(state().balance + evt.getAmount())
    );

    builder.setEventHandler(
            AccountEvent.WithdrawExecuted.class,
            evt -> new Account(state().balance - evt.getAmount())
    );

    return builder.build();
  }

}
