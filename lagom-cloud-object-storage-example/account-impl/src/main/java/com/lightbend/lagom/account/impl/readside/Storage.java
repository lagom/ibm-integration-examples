/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.account.impl.readside;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.s3.MemoryBufferType;
import akka.stream.alpakka.s3.Proxy;
import akka.stream.alpakka.s3.S3Settings;
import com.amazonaws.auth.*;
import akka.stream.alpakka.s3.javadsl.MultipartUploadResult;
import akka.stream.alpakka.s3.javadsl.S3Client;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Option;
import scala.Some;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Facade component for Alpakka S3 storage. Provides two basic methods: save() and fetch().  
 */
public class Storage {

  private final S3Client client;
  private final String bucket;
  private final ActorMaterializer materializer;


  @Inject
  public Storage(ActorSystem actorSystem) {

    Config config = ConfigFactory.load().getConfig("cloud-object-storage");

    AWSStaticCredentialsProvider provider =
            new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                            config.getString("credentials.access-key-id"),
                            config.getString("credentials.secret-access-key")
                    ));


    String host = config.getString("host");
    Option<Proxy> proxy = Some.apply(new Proxy(host, 443, "https"));


    S3Settings settings = new S3Settings(MemoryBufferType.getInstance(), proxy, provider, "", true);
    materializer = ActorMaterializer.create(actorSystem);

    bucket = config.getString("bucket");

    client = new S3Client(settings, actorSystem, materializer);

  }


  CompletionStage<Done> save(String key, String payload) {

    Sink<ByteString, CompletionStage<MultipartUploadResult>> sink =
            client.multipartUpload(bucket, key);

    return Source
            .single(ByteString.fromString(payload))
            .runWith(sink, materializer)
            .thenApply(d -> Done.getInstance());
  }

  CompletionStage<String> fetch(String key) {
    return client
            .download(bucket, key)
            .runFold(
                    new StringBuilder(),
                    (builder, byteStr) -> builder.append(byteStr.decodeString("UTF-8")),
                    materializer
            ).thenApply(StringBuilder::toString);
  }

}
