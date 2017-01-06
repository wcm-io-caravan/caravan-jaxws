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
package io.wcm.caravan.jaxws.consumer.httpclient;

import org.osgi.annotation.versioning.ProviderType;

import io.wcm.caravan.jaxws.consumer.JaxWsClientInitializer;

/**
 * Apply HTTP client/transport configuration settings defined for Caravan Commons HTTP Client
 * to {@link JaxWsClientInitializer} objects for SOAP Consumers.
 */
@ProviderType
public interface JaxWsClientInitializerHttpClientConfig {

  /**
   * Get {@link JaxWsClientInitializer} with HTTP client config matching for URL and (if set) wsAddressingToUri.
   * @param url SOAP service URL
   * @return {@link JaxWsClientInitializer} instance
   */
  JaxWsClientInitializer get(String url);

  /**
   * Apply HTTP client config to {@link JaxWsClientInitializer} matching for URL and (if set) wsAddressingToUri.
   * @param jaxWsClientInitializer {@link JaxWsClientInitializer} instance
   * @param url SOAP service URL
   */
  void apply(JaxWsClientInitializer jaxWsClientInitializer, String url);

}
