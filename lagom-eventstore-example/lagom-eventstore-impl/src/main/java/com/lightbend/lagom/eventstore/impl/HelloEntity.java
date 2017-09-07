/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

public class HelloEntity extends PersistentEntity<HelloCommand, HelloEvent, GreetingsState> {

    @Override
    public Behavior initialBehavior(Optional<GreetingsState> snapshotState) {
        String name = entityId();
        GreetingsState greetingsState = new GreetingsState(name);
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(greetingsState));

        b.setCommandHandler(HelloCommand.Greet.class, (cmd, ctx) ->
                ctx.thenPersist(new HelloEvent.Greeted(entityId()),
                        evt -> ctx.reply(Done.getInstance())));

        b.setEventHandler(HelloEvent.Greeted.class,
                evt -> state());

        return b.build();
    }

}
