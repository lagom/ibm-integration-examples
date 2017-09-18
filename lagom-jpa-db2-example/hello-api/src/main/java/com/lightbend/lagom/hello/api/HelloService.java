/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PMap;
import org.pcollections.PSequence;

/**
 * The Hello service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the Hello service.
 */
public interface HelloService extends Service {

    /**
     * Example: curl http://localhost:9000/api/hello/Alice
     */
    ServiceCall<NotUsed, String> hello(String id);


    /**
     * Examples:
     * curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Alice
     * curl -H "Content-Type: application/json" -X POST -d '{"message": "Good day"}' http://localhost:9000/api/hello/Bob
     * curl -H "Content-Type: application/json" -X POST -d '{"message": "Hi"}' http://localhost:9000/api/hello/Carol
     * curl -H "Content-Type: application/json" -X POST -d '{"message": "Howdy"}' http://localhost:9000/api/hello/David
     */
    ServiceCall<GreetingMessage, Done> useGreeting(String id);

    /**
     * Example: curl http://localhost:9000/api/greetings
     * Response (pretty-printed):
     * <pre>
     * [
     *   {
     *     "id": "Alice",
     *     "message": "Hi"
     *   },
     *   {
     *     "id": "Bob",
     *     "message": "Good day"
     *   },
     *   {
     *     "id": "Carol",
     *     "message": "Hi"
     *   },
     *   {
     *     "id": "David",
     *     "message": "Howdy"
     *   }
     * ]
     * </pre>
     */
    ServiceCall<NotUsed, PSequence<UserGreeting>> allGreetings();

    @Override
    default Descriptor descriptor() {
        return named("hello").withCalls(
                pathCall("/api/hello/:id", this::hello),
                pathCall("/api/hello/:id", this::useGreeting),
                pathCall("/api/greetings", this::allGreetings)
        ).withAutoAcl(true);
    }
}
