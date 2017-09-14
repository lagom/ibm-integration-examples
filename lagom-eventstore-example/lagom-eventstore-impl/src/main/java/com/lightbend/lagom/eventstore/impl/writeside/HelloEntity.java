/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.eventstore.impl.writeside;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

// A simple entoty that will emit an event on every command. This is a made example for demo purposes.
public class HelloEntity extends PersistentEntity<HelloCommand, HelloEvent, GreetingsState> {

    @Override
    public Behavior initialBehavior(Optional<GreetingsState> snapshotState) {
        String name = entityId();
        GreetingsState greetingsState = new GreetingsState(name);
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(greetingsState));

        b.setCommandHandler(HelloCommand.Greet.class,
                (cmd, ctx) -> ctx.thenPersist(
                        new HelloEvent.Greeted(entityId(), System.currentTimeMillis()),
                        evt -> ctx.reply(Done.getInstance()))
        );

        b.setEventHandler(HelloEvent.Greeted.class,
                evt -> state()
        );

        return b.build();
    }

}
