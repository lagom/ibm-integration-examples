package com.lightbend.lagom.hello.impl.readside;

import akka.Done;

import java.util.concurrent.CompletionStage;

public interface Storage {

  CompletionStage<Done> save(String payload);

}
