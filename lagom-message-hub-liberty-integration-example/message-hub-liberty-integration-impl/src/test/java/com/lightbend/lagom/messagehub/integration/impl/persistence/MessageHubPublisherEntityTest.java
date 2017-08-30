package com.lightbend.lagom.messagehub.integration.impl.persistence;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import com.lightbend.lagom.messagehub.integration.impl.persistence.MessageHubPublisherCommand.SendMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

// This shows an example of Lagom's PersistentEntityTestDriver
// for more details, see https://www.lagomframework.com/documentation/1.3.x/java/PersistentEntity.html#Unit-Testing
public class MessageHubPublisherEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testSendMessage() {
        PersistentEntityTestDriver<MessageHubPublisherCommand, MessageHubPublisherEvent, MessageHubPublisherState> driver =
                new PersistentEntityTestDriver<>(
                        system,
                        new MessageHubPublisherEntity(),
                        "MessageHubPublisherEntityTest"
                );

        Outcome<MessageHubPublisherEvent, MessageHubPublisherState> outcome =
                driver.run(new SendMessage("Test message"));

        assertEquals(1, outcome.events().size());
        assertEquals(new MessageHubPublisherEvent.MessageReceived(0, "Test message"), outcome.events().get(0));
        assertEquals(1, outcome.getReplies().size());
        assertEquals(Done.getInstance(), outcome.getReplies().get(0));
        assertEquals(new MessageHubPublisherState(1), outcome.state());
        assertEquals(emptyList(), outcome.issues());
    }
}
