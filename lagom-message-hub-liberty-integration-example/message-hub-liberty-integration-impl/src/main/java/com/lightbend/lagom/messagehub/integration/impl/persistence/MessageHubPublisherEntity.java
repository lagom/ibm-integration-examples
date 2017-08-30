package com.lightbend.lagom.messagehub.integration.impl.persistence;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.messagehub.integration.impl.persistence.MessageHubPublisherCommand.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// Accumulates messages for publishing to Kafka.
//
// For more information, see https://www.lagomframework.com/documentation/1.3.x/java/PersistentEntity.html
// and https://www.lagomframework.com/documentation/1.3.x/java/MessageBrokerApi.html
public class MessageHubPublisherEntity
        extends PersistentEntity<MessageHubPublisherCommand, MessageHubPublisherEvent, MessageHubPublisherState> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Behavior initialBehavior(Optional<MessageHubPublisherState> snapshotState) {
        // Each entity has an in-memory state that can be used when processing commands.
        // In this case, the state contains a counter with the next message ID.
        // The state can be transformed by event handlers, and it can be recovered when an entity is reloaded into
        // memory by replaying all stored events through their handlers.
        // As an optimization, Lagom periodically "snapshots" the state into a separate database table, allowing faster
        // recovery from the latest snapshot.
        // We initialize the state with either the latest stored state snapshot, if there is any, or the initial state.
        BehaviorBuilder behavior = newBehaviorBuilder(snapshotState.orElse(MessageHubPublisherState.INITIAL));

        // Commands are requests to perform some action, resulting in persisted events.
        // Events are later acted upon asynchronously.
        // Each entity handles only one command at a time, and subsequent commands are queued to handle in sequence.
        behavior.setCommandHandler(SendMessage.class,
                // When we receive the "SendMessage" command
                (command, context) -> {
                    // First, ask the current state to create the event data.
                    MessageHubPublisherEvent.MessageReceived event = state().sendMessage(command.getMessage());
                    // Then, persist the event.
                    return context.thenPersist(event,
                            // This callback is invoked after the event is persisted.
                            // It replies to the original caller with the "Done" message,
                            // indicating that the command was accepted.
                            _persistedEvent -> context.reply(Done.getInstance()));
                }
        );

        // Event handlers are invoked after events are persisted, but before further commands are accepted.
        // Event handlers will also be invoked when an entity is reloaded into memory, before processing any commands,
        // to bring the latest snapshot state (or initial state, if there are no snapshots) up to date with any events
        // that were persisted later.
        behavior.setEventHandler(MessageHubPublisherEvent.MessageReceived.class, event -> {
            // For each "MessageReceived" event, update the internal state, incrementing the message ID counter
            MessageHubPublisherState newState = state().nextState();
            log.info("Got event: [{}], returning state: [{}]", event, newState);
            return newState;
        });

        return behavior.build();
    }
}
