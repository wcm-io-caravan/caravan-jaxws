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

import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caravan.jaxws.consumer.impl.OsgiAwareClientImpl;

/**
 * Factory for creating initializing JAX-WS SOAP clients.
 */
@ProviderType
public interface JaxWsClientFactory {

  /**
   * Create webservice port via JAXWS proxy factory.
   * This method fixes numerous problems with 3rdparty libs used by CXF and CXF itself and classloader issues with OSGI.
   * Using this method the initialization phase of JAXB mapping is wrapped in an OSGI-aware classloader.
   * Furthermore each client instances is wrapped in an OSGI-aware subclass (see {@link OsgiAwareClientImpl}), which
   * ensures that each invoke call on a webservice method is itself executed within an OSGI-aware classloader context.
   * @param <T> Port class
   * @param clazz Port class with JAXWS annotation
   * @param portUrl Port url (this is not the WSDL location)
   * @return Port object
   */
  <T> T create(Class<T> clazz, String portUrl);

  /**
   * Create webservice port via JAXWS proxy factory.
   * This method fixes numerous problems with 3rdparty libs used by CXF and CXF itself and classloader issues with OSGI.
   * Using this method the initialization phase of JAXB mapping is wrapped in an OSGI-aware classloader.
   * Furthermore each client instances is wrapped in an OSGI-aware subclass (see {@link OsgiAwareClientImpl}), which
   * ensures that each invoke call on a webservice method is itself executed within an OSGI-aware classloader context.
   * @param <T> Port class
   * @param clazz Port class with JAXWS annotation
   * @param portUrl Port url (this is not the WSDL location)
   * @param initializer Client proxy initializer
   * @return Port object
   */
  <T> T create(Class<T> clazz, String portUrl, JaxWsClientInitializer initializer);

  /**
   * Create webservice port via JAXWS proxy factory.
   * This method fixes numerous problems with 3rdparty libs used by CXF and CXF itself and classloader issues with OSGI.
   * Using this method the initialization phase of JAXB mapping is wrapped in an OSGI-aware classloader.
   * Furthermore each client instances is wrapped in an OSGI-aware subclass (see {@link OsgiAwareClientImpl}), which
   * ensures that each invoke call on a webservice method is itself executed within an OSGI-aware classloader context.
   * @param <T> Port class
   * @param clazz Port class with JAXWS annotation
   * @param portUrl Port url (this is not the WSDL location)
   * @param initializer Client proxy initializer
   * @param cacheKeySuffix Optional: If different configurations are required for the port class/port url combination
   *          it is possible to specify a cache key suffix (e.g. pool entry name). This is appended to the cache key.
   * @return Port object
   */
  <T> T create(Class<T> clazz, String portUrl, JaxWsClientInitializer initializer, String cacheKeySuffix);

}
