package com.example.hello.impl.jms

import akka.stream.scaladsl.{Sink, Source}

/**
 * These components are implemented by [[com.example.hello.impl.jms.MQHelloJmsComponents]]
 * in production, but it's helpful to abstract them out so we can mock
 * the for testing.
 */
trait HelloJmsComponents {
  /**
   * Lets us create a [[Source]] from which we can receive Hello service
   * JMS messages.
   */
  def helloJmsSource: HelloJmsSourceFactory

  /**
   * Lets us create a [[Sink]] to which we can send Hello service JMS messages.
   */
  def helloJmsSink: HelloJmsSinkFactory
}

/**
 * Lets us create a [[Source]] which we can use to receive Hello service
 * JMS messages.
 */
trait HelloJmsSourceFactory {
  /**
   * Create a new JMS source. When called, this method should call join
   * the source to the provided sink and materialize both.
   */
  def createJmsSource[T](toSink: Sink[String,T]): T
}

/**
 * Lets us create a [[Sink]] to which we can send Hello service
 * JMS messages.
 */
trait HelloJmsSinkFactory {
  /**
   * Create a new JMS sink. When called, this method should join the
   * provided source to the JMS sink and materialize both.
   */
  def createJmsSink[T](fromSource: Source[String,T]): T
}