/*
 * Copyright (C) 2016-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.hello.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.hello.api.HelloService;
import com.lightbend.lagom.hello.impl.readside.Greetings;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(HelloService.class, HelloServiceImpl.class);
        bind(Greetings.class).asEagerSingleton();
    }
}
