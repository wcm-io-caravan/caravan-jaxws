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
package io.wcm.caravan.jaxws.integrationtest.helloworld.consumer;

import org.apache.hello_world_soap_http.Greeter;
import org.apache.hello_world_soap_http.PingMeFault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.wcm.caravan.jaxws.consumer.JaxWsClientFactory;

/**
 * Hello World SOAP Client.
 */
@Component(service = HelloWorldConsumer.class)
public class HelloWorldConsumer {

  @Reference
  private JaxWsClientFactory jaxWsClientFactory;

  private Greeter greeterClient;

  @Activate
  private void activate() {
    String url = System.getProperty("launchpad.http.server.url") + "/helloWorldService";
    greeterClient = jaxWsClientFactory.create(Greeter.class, url);
  }

  /**
   * pingMe
   */
  public void doPing() {
    try {
      greeterClient.pingMe();
    }
    catch (PingMeFault ex) {
      throw new RuntimeException("Ping failed.", ex);
    }
  }

  /**
   * greetMe
   * @param greeting Greeting
   * @return Answer
   */
  public String doGreet(String greeting) {
    return greeterClient.greetMe(greeting);
  }

  /**
   * greetMeOneWay
   * @param greeting Greeting
   */
  public void doGreetOneWay(String greeting) {
    greeterClient.greetMeOneWay(greeting);
  }

  /**
   * sayHi
   * @return Answer
   */
  public String doSayHi() {
    return greeterClient.sayHi();
  }

}
