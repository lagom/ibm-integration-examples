package com.example.hello.impl.jms

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.pattern.pipe
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.{Done, NotUsed}
import com.example.hello.impl._
import com.example.hello.impl.jms.HelloJmsSourceFactory.RunSource
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
 * This actor is responsible for listening for JMS messages and processing
 * them.
 *
 * @param jmsSourceFactory Used to create the source for sending JMS messages.
 * @param materializer This is used to materialize the stream.
 */
class HelloJmsReceiverActor(
    jmsSourceFactory: HelloJmsSourceFactory,
    persistentEntityRegistry: PersistentEntityRegistry)(
    implicit materializer: Materializer) extends Actor {

  import HelloJmsReceiverActor._

  private val logger = LoggerFactory.getLogger(getClass)

  // Implicitly use the dispatcher to execute concurrent operations (e.g. futures)
  import context.dispatcher

  /**
   * This method is called by the [[ActorSystem]] when this actor
   * is first started or whenever it is restarted.
   */
  override def preStart(): Unit = {
    logger.info("preStart called: creating MQ JmsSource")

    jmsSourceFactory.createJmsSource(new RunSource[Unit] {
      override def apply[Mat](source: Source[String, Mat]): (Mat, Unit) = {
        logger.info("preStart called: creating Sink.actorRefWithAck")

        val actorSink: Sink[String, NotUsed] = Sink.actorRefWithAck[String](self,
          onInitMessage = StreamInit,
          ackMessage = StreamAck,
          onCompleteMessage = StreamComplete,
          onFailureMessage = StreamFailure
        )

        logger.info("preStart called: creating stream from JmsSource to Sink.actorRefWithAck")
        val sourceMat: Mat = source.to(actorSink).run()
        (sourceMat, ())
      }
    })
  }

  /**
   * This method is called by the [[ActorSystem]] when this actor
   * stops or whenever it is stopped just before being restarted.
   */
  override def postStop(): Unit = {
    logger.info("postStop called: stream with JmsSource will be shut down by Sink.actorRefWithAck")
  }

  /**
   * The starting receive handler for this actor.
   *
   * This receive handler is used until the actor receives [[StreamInit]]
   * message at which point the actor changes to use the [[waitingForMessage]]
   * receive handler.
   */
  override def receive: Receive = {
    // Messages sent by Sink.actorRefWithAck
    case StreamInit =>
      logger.info("Upstream is ready: changing actor to listen for messages")
      sender ! StreamAck // Tell upstream that we've finished initializing
      context.become(waitingForMessage, discardOld = true) // Change to listen for messages

    // Messages sent by ClusterSingletonManager
    case ClusterSingletonTerminate =>
      logger.info("Terminating at request of ClusterSingletonManager")
      context.stop(self)
  }

  /**
   * The receive handler used when waiting for JMS messages from upstream.
   */
  private def waitingForMessage: Receive = {
    // Messages sent by Sink.actorRefWithAck
    case message: String =>
      logger.info("Received message: processing")
      context.become(processingMessage(sender()), discardOld = true)
      pipe(handleMessage(message)).pipeTo(self)
    case StreamComplete =>
      logger.error(s"Terminating because upstream was closed")
      context.stop(self)
    case StreamFailure(throwable) =>
      logger.error(s"Terminating due to upstream failure", throwable)
      context.stop(self)

    // Messages sent by ClusterSingletonManager
    case ClusterSingletonTerminate => // Sent by ClusterManager
      logger.info("Terminating at request of ClusterSingletonManager")
      context.stop(self)
  }

  /**
   * The receive handler used when processing a JMS message received
   * from upstream.
   *
   * @param streamSender The upstream sender that originally sent the
   *                   JMS message. We pass this parameter because
   *                   the sender that sends the next (non-MQ) message,
   *                   e.g. [[Done]], might not be the original sender.
   */
  private def processingMessage(streamSender: ActorRef): Receive = {
    // Messages sent by pipeTo
    case Done =>
      logger.info("Message processing finished: waiting for next message")
      streamSender ! StreamAck // Acknowledge that we've finished processing
      context.become(waitingForMessage, discardOld = true)
    case t: Throwable => // Sent by pipe(ref.ask).pipeTo
      logger.error("Message processing failed: terminating", t)
      context.stop(self)

    // Messages sent by Sink.actorRefWithAck
    case StreamComplete =>
      logger.error(s"Upstream was closed: will terminate once the current message is processed")
      context.become(processingMessageThenStopping(stopReason = "upstream was closed"), discardOld = true)
    case StreamFailure(throwable) =>
      logger.error(s"Upstream failed: will terminate once the current message is processed", throwable)
      context.become(processingMessageThenStopping(stopReason = "upstream failed"), discardOld = true)

    // Messages sent by ClusterSingletonManager
    case ClusterSingletonTerminate =>
      logger.info("ClusterSingletonManager asked us to terminate: will terminate once the current message is processed")
      context.become(processingMessageThenStopping(stopReason = "ClusterSingletonManager asked us to terminate"))
  }

  /**
   * The receive handler used when processing a JMS message and then
   * stopping. This might happen if we receive a JMS message but then
   * we're given a reason to stop, e.g. if the upstream stream is closed
   * or if we're asked to stop by the [[ClusterSingletonManager]].
   *
   * @param stopReason A message explaining why we're stopping. Used for log messages.
   */
  private def processingMessageThenStopping(stopReason: String): Receive = {
    // Messages sent by pipeTo
    case Done =>
      logger.info(s"Message processing finished: terminating because $stopReason")
      context.stop(self)
    case t: Throwable =>
      logger.error(s"Message processing failed: terminating because $stopReason", t)
      context.stop(self)

    // Messages sent by Sink.actorRefWithAck
    case StreamComplete =>
      logger.error(s"Upstream was closed: already terminating after the current message is processed: $stopReason")
    case StreamFailure(throwable) =>
      logger.error(s"Upstream failed: already terminating after the current message is processed: $stopReason", throwable)

    // Messages sent by ClusterSingletonManager
    case ClusterSingletonTerminate =>
      logger.info(s"ClusterSingletonManager asked us to terminate: already terminating after the current message is processed: $stopReason")
  }

  /**
   * Handle a message that is received. This decodes the raw JMS message string,
   * getting JSON [[JsValue]], decoding it to an [[UpdateGreetingMessage]]
   * then updating the persistent entity.
   */
  private def handleMessage(messageString: String): Future[Done] = {
    try {
      logger.info(s"Received JMS message: $messageString")
      val updateJson: JsValue = Json.parse(messageString)
      Json.fromJson[UpdateGreetingMessage](updateJson) match {
        case JsSuccess(UpdateGreetingMessage(id, newMessage), _) =>
          logger.info(s"Updating entity '$id' with message '$newMessage'.")
          val ref = persistentEntityRegistry.refFor[HelloEntity](id)
          ref.ask(UseGreetingMessage(newMessage))
        case error: JsError =>
          throw new Exception(s"Failed to parse JavaScript: $error")
      }
    } catch {
      case NonFatal(t) =>
        logger.error("Error handling message", t)
        Future.failed(t)
    }
  }
}

object HelloJmsReceiverActor {

  /**
   * This message is sent by the [[ClusterSingletonManager]] to the [[HelloJmsReceiverActor]]
   * when it is time to terminate.
   */
  case object ClusterSingletonTerminate

  /**
   * This message is sent by [[Sink.actorRefWithAck()]] to the [[HelloJmsReceiverActor]]
   * when the stream is being initialized.
   */
  case object StreamInit

  /**
   * This message is a reply from the [[HelloJmsReceiverActor]] to acknowledge a message
   * sent by [[Sink.actorRefWithAck()]]. This message is used to control backpressure;
   * the [[Sink]] will wait until it gets acknowledgement before proceeding.
   */
  case object StreamAck

  /**
   * This message is sent by [[Sink.actorRefWithAck()]] to the [[HelloJmsReceiverActor]]
   * when the stream is completed successfully.
   */
  case object StreamComplete

  /**
   * This message is sent by [[Sink.actorRefWithAck()]] to the [[HelloJmsReceiverActor]]
   * when the stream is completed with failure.
   */
  case class StreamFailure(t: Throwable)

  /**
   * Creates a ClusterSingletonManager actor and asks it to manage the
   * [[HelloJmsReceiverActor]] as a cluster singleton.
   */
  def startWithClusterSingletonManager(
      jmsSourceFactory: HelloJmsSourceFactory,
      persistentEntityRegistry: PersistentEntityRegistry,
      actorSystem: ActorSystem,
      materializer: Materializer) {

    val logger = LoggerFactory.getLogger(getClass)

    logger.info(s"Starting ClusterSingletonManager to run ${classOf[HelloJmsReceiverActor].getSimpleName} as a cluster singleton")

    // Start a ClusterSingletonManager actor. This actor will communicate with
    // its peers on the cluster and decide on one cluster member to run the
    // MQListenerActor. If cluster membership changes (e.g. a node dies) then
    // the node running the MQListenerActor may change.
    actorSystem.actorOf(
      ClusterSingletonManager.props(
        singletonProps = Props(classOf[HelloJmsReceiverActor], jmsSourceFactory, persistentEntityRegistry, materializer),
        terminationMessage = ClusterSingletonTerminate,
        settings = ClusterSingletonManagerSettings(actorSystem)),
      name = "hello-jms-receiver-cluster-singleton-manager")
  }
}