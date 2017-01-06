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
package io.wcm.caravan.jaxws.consumer.impl;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;

/**
 * Factory bean that creates {@link OsgiAwareClientImpl} objects instead of {@link org.apache.cxf.endpoint.ClientImpl} objects to ensure correct classloader
 * usage in OSGI context.
 */
public final class OsgiAwareJaxWsClientFactoryBean extends JaxWsClientFactoryBean {

  @Override
  protected Client createClient(Endpoint endpoint) {
    // use osgi-aware client impl instead of {@link org.apache.cxf.endpoint.ClientImpl}
    return new OsgiAwareClientImpl(getBus(), endpoint, getConduitSelector());
  }

}
