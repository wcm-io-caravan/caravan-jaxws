/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.caravan.jaxws.publisher.it;

import org.apache.sling.junit.rules.TeleporterRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.caravan.jaxws.integrationtest.helloworld.consumer.HelloWorldConsumer;

public class PublisherConsumerIT {

  @Rule
  public TeleporterRule teleporter = TeleporterRule.forClass(getClass(), "IT");

  private HelloWorldConsumer underTest;

  @Before
  public void setUp() {
    underTest = teleporter.getService(HelloWorldConsumer.class);
  }

  @Test
  public void testPing() {
    underTest.doPing();
  }

}
