package com.example.hello.impl.jms

import akka.stream.alpakka.jms.scaladsl.{JmsSink, JmsSource}
import akka.stream.alpakka.jms.{Credentials, JmsSinkSettings, JmsSourceSettings}
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.ibm.mq.jms.MQQueueConnectionFactory
import com.ibm.msg.client.wmq.common.CommonConstants
import play.api.{BuiltInComponents, Configuration}

/**
 * JMS components used by the Hello service and backed by IBM MQ.
 *
 * This trait is mixed into [[com.example.hello.impl.HelloApplication]]
 * to implement [[HelloJmsComponents]].
 */
trait MQHelloJmsComponents extends HelloJmsComponents {
  self: BuiltInComponents =>

  /**
   * The settings that are used when we create a source or sink. These
   * settings are read once and cached.
   *
   * Since they share a lot of logic we initialize them together.
   */
  private lazy val (sourceSettings, sinkSettings) = {

    // Read configuration
    val mqConfig = configuration.get[Configuration]("hello.mq")
    val queueManager = mqConfig.get[String]("queue-manager")
    val channel = mqConfig.get[String]("channel")
    val username = mqConfig.get[String]("username")
    val password = mqConfig.get[String]("password")
    val queue = mqConfig.get[String]("queue")

    // Create the connection factory
    val queueConnectionFactory: MQQueueConnectionFactory = new MQQueueConnectionFactory()
    queueConnectionFactory.setQueueManager(queueManager)
    queueConnectionFactory.setChannel(channel)
      // Try to use a native (shared memory) connection if possible,
      // otherwise fall back to using the TCP client connection.
    queueConnectionFactory.setTransportType(CommonConstants.WMQ_CM_BINDINGS_THEN_CLIENT)

    // Create the credentials
    val credentials = Credentials(username, password)

    // Create the source and sink settings
    (
        JmsSourceSettings(queueConnectionFactory)
            .withQueue(queue)
            .withCredential(credentials),
        JmsSinkSettings(queueConnectionFactory)
            .withQueue(queue).withCredential(credentials)
    )
  }

  override def helloJmsSource: HelloJmsSourceFactory = new HelloJmsSourceFactory {
    override def createJmsSource[T](toSink: Sink[String,T]): T = {
      val jmsSource = JmsSource.textSource(sourceSettings)
      jmsSource.toMat(toSink)(Keep.right).run()(materializer)
    }
  }
  override def helloJmsSink: HelloJmsSinkFactory = new HelloJmsSinkFactory {
    override def createJmsSink[T](fromSource: Source[String,T]): T = {
      val jmsSink = JmsSink.textSink(sinkSettings)
      fromSource.toMat(jmsSink)(Keep.left).run()(materializer)
    }
  }
}
