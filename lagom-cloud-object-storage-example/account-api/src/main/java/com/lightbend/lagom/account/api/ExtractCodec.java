package com.lightbend.lagom.account.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ExtractCodec {

  private static Logger logger = LoggerFactory.getLogger(ExtractCodec.class);

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JavaTimeModule());
  }



  public static String encode(Extract extract) {
    try {
      return mapper.writeValueAsString(extract);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Extract decode(String json)  {
    try {
      return mapper.readValue(json, Extract.class);
    } catch (IOException e) {
      logger.error("Error while parsing json: " + json, e);
      throw new SerializationException(e.getMessage());
    }
  }

}
