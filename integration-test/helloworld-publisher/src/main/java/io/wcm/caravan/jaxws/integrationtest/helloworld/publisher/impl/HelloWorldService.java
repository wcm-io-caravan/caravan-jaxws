/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2016 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.caravan.jaxws.integrationtest.helloworld.publisher.impl;

import javax.servlet.Servlet;

import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.PingMeFault;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.caravan.jaxws.publisher.AbstractJaxWsServer;

/**
 * Hello World SOAP Service implementation.
 */
@Component(service = Servlet.class, immediate = true,
    property = "alias=/helloWorldService")
public class HelloWorldService extends AbstractJaxWsServer implements Greeter {
  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(HelloWorldService.class);

  @Override
  protected Class getServerInterfaceType() {
    return Greeter.class;
  }

  @Override
  public void pingMe() throws PingMeFault {
    log.info("pingMe called");
  }

  @Override
  public String greetMe(String requestType) {
    log.info("greetMe called: {}", requestType);
    return "Hello World Publisher: " + requestType;
  }

  @Override
  public void greetMeOneWay(String requestType) {
    log.info("greetMeOneWay called: {}", requestType);
  }

  @Override
  public String sayHi() {
    log.info("sayHi called");
    return "Hello World Publisher";
  }

}
