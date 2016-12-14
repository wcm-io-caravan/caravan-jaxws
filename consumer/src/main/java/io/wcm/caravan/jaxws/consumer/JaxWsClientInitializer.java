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
package io.wcm.caravan.jaxws.consumer;

import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Initialize JAX-WS SOAP client factory and create SOAP client object instance.
 */
@ConsumerType
public interface JaxWsClientInitializer {

  /**
   * Initialize JAXWS proxy factory bean
   * @param factory JAXWS Proxy factory bean
   */
  void initializeFactory(JaxWsProxyFactoryBean factory);

  /**
   * Initialize JAX-WS SOAP client factory and create SOAP client object instance.
   * @param factory JAX-WS proxy factory bean
   * @return SOAP client object
   */
  Object createClient(JaxWsProxyFactoryBean factory);

  /**
   * Create JAX-WS client factory bean instance.
   * @return Client factory bean.
   */
  ClientFactoryBean createClientFactoryBean();

}
