package com.example.hello.impl

import akka.Done
import com.example.hello.api
import com.example.hello.api.HelloService
import com.example.hello.impl.jms.HelloJmsSender
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.ExecutionContext

/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl(
    persistentEntityRegistry: PersistentEntityRegistry,
    jmsSender: HelloJmsSender)(
    implicit ec: ExecutionContext) extends HelloService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the Hello entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // We've been asked to change the greeting. Send the update information
    // over JMS. (The update will eventually be processed by the
    // HelloJmsReceiverActor.)
    jmsSender.sendGreetingUpdate(id, request.message)
  }
  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(HelloEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[HelloEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
