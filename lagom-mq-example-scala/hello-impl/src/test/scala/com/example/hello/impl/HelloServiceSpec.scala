package com.example.hello.impl

import java.util.concurrent.{Executors, LinkedBlockingDeque, TimeUnit}

import akka.stream.scaladsl.{Keep, Sink, SinkQueueWithCancel, Source, SourceQueueWithComplete}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.example.hello.api._
import com.example.hello.impl.jms.{HelloJmsSinkFactory, HelloJmsSourceFactory, UpdateGreetingMessage}
import com.lightbend.lagom.scaladsl.server.{LagomApplicationContext, LocalServiceLocator}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.ServiceTest.TestServer
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.{AsyncWordSpec, BeforeAndAfterEach, Matchers}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterEach with PatienceConfiguration {

  /**
   * An application with mocked JMS endpoints.
   */
  class TestHelloApplication(context: LagomApplicationContext)
      extends HelloApplication(context) with LocalServiceLocator {

    /**
     * Everytime a JMS Source is created, it is added to this queue.
     * In normal usage, this will normally only happen once.
     */
    lazy val jmsSourceQueues = new LinkedBlockingDeque[SourceQueueWithComplete[String]]

    /**
     * Everytime a JMS Sink is created, it is added to this queue.
     * In normal usage, this will normally only happen once.
     */
    lazy val jmsSinkQueues = new LinkedBlockingDeque[SinkQueueWithCancel[String]]

    override def helloJmsSource: HelloJmsSourceFactory = new HelloJmsSourceFactory {
      override def createJmsSource[T](toSink: Sink[String,T]): T = {
        val jmsSource = Source.queue[String](0, OverflowStrategy.backpressure)
        val (queue, sinkMat) = jmsSource.toMat(toSink)(Keep.both).run()(materializer)
        jmsSourceQueues.putLast(queue) // Make the materialized queue available to tests
        sinkMat
      }
    }

    override def helloJmsSink: HelloJmsSinkFactory = new HelloJmsSinkFactory {
      override def createJmsSink[T](fromSource: Source[String,T]): T = {
        val jmsSink = Sink.queue[String]()
        val (sourceMat, queue) = fromSource.toMat(jmsSink)(Keep.both).run()(materializer)
        jmsSinkQueues.putLast(queue) // Make the materialized queue available to tests
        sourceMat
      }
    }
  }

  // State used by each test

  private var server: TestServer[TestHelloApplication] = _
  private var testThreadPool: ExecutionContextExecutorService = _

  override protected def beforeEach(): Unit = {
    assert(server == null) // Sanity check that we're not running these tests concurrently
    testThreadPool = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
    server = ServiceTest.startServer(
      ServiceTest.defaultSetup
          .withCassandra(true)
    )(new TestHelloApplication(_))
  }

  override protected def afterEach(): Unit = {
    server.stop()
    testThreadPool.shutdown()
    server = null
  }

  // Helpers to access test state

  private def application: TestHelloApplication = server.application
  private def client = server.serviceClient.implement[HelloService]

  /** Helper to get a message that has been sent to the JMS Sink */
  def getSentMessage(): Future[String] = {
    for {
      sinkQueue <- Future {
        // Get the queue by waiting up to a certain timeout
        val patience = implicitly[PatienceConfig]
        val queue: SinkQueueWithCancel[String] =
          application.jmsSinkQueues.pollFirst(patience.timeout.totalNanos, TimeUnit.NANOSECONDS)
        // Put the queue back so we can get it again if we want
        application.jmsSinkQueues.putFirst(queue)
        queue
      }
      optMessage <- sinkQueue.pull()
    } yield optMessage.get
  }

  /** Helper to put a message onto the JMS Source, simulating a receive */
  def putReceivedMessage(message: String): Future[Unit] = {
    for {
      sourceQueue <- Future {
        // Get the queue by waiting up to a certain timeout
        val patience = implicitly[PatienceConfig]
        val queue: SourceQueueWithComplete[String] =
          application.jmsSourceQueues.pollFirst(patience.timeout.totalNanos, TimeUnit.NANOSECONDS)
        // Put the queue back so we can get it again if we want
        application.jmsSourceQueues.putFirst(queue)
        queue
      }
      offerResult <- sourceQueue.offer(message)
    } yield offerResult match {
      case QueueOfferResult.Enqueued => ()
      case otherResult => throw new Exception(s"Unexpected queue result: $otherResult")
    }
  }

  "Hello service" should {

    "say hello" in {
      // Check that the service call to read a greeting works properly
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    // Test the code up to the point of sending a JMS message
    "send a JMS message when updating with a custom greeting" in {
      for {
        // Make a service call
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        // Capture the message that the service call sends
        updateString <- getSentMessage()
      } yield {
        // Check that the message parses to the correct value
        val updateJson = Json.parse(updateString)
        val errorOrUpdate = updateJson.validate[UpdateGreetingMessage].asEither
        errorOrUpdate should ===(Right(UpdateGreetingMessage("Bob", "Hi")))
      }
    }

    // Test the code from the point of receiving a JMS message
    "update the greeting when receiving a JMS message" in {
      def updateString(id: String, greeting: String): String = {
        val update = UpdateGreetingMessage(id, greeting)
        val updateJson: JsValue = Json.toJson(update)
        Json.stringify(updateJson)
      }
      for {
        origGreeting <- client.hello("Bob").invoke()
        // Simulate the act of receiving a message from the JmsSource.
        _ <- putReceivedMessage(updateString("Bob", "Hi"))
        // Send and wait for a second message. This will prove that the first
        // one has been processed, because the actor only accepts a new message
        // once it has processed any previous messages.
        _ <- putReceivedMessage(updateString("Sally", "Howdy"))
        // Now we know the greeting has been updated.
        newGreeting <- client.hello("Bob").invoke()
      } yield {
        // Confirm the greeting change has occurred.
        origGreeting should ===("Hello, Bob!")
        newGreeting should ===("Hi, Bob!")
      }
    }
  }
}