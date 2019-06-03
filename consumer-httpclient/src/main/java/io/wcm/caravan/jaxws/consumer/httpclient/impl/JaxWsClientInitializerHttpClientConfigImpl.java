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
package io.wcm.caravan.jaxws.consumer.httpclient.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.ServiceUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import io.wcm.caravan.commons.httpclient.HttpClientConfig;
import io.wcm.caravan.jaxws.consumer.JaxWsClientInitializer;
import io.wcm.caravan.jaxws.consumer.httpclient.JaxWsClientInitializerHttpClientConfig;

/**
 * Implementation of {@link JaxWsClientInitializerHttpClientConfig}-
 */
@Component(immediate = true, service = JaxWsClientInitializerHttpClientConfig.class)
public class JaxWsClientInitializerHttpClientConfigImpl implements JaxWsClientInitializerHttpClientConfig {

  @Reference(service = HttpClientConfig.class,
      cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
      bind = "bindHttpClientConfig", unbind = "unbindHttpClientConfig")
  private final Map<Comparable<Object>, HttpClientConfig> configItems = new ConcurrentSkipListMap<>();

  protected void bindHttpClientConfig(HttpClientConfig httpClientConfig, Map<String, Object> config) {
    configItems.put(ServiceUtil.getComparableForServiceRanking(config), httpClientConfig);
  }

  @SuppressWarnings("unused")
  protected void unbindHttpClientConfig(HttpClientConfig httpClientConfig, Map<String, Object> config) {
    configItems.remove(ServiceUtil.getComparableForServiceRanking(config));
  }

  @Override
  public JaxWsClientInitializer get(String url) {
    JaxWsClientInitializer jaxWsClientInitializer = new JaxWsClientInitializer();
    apply(jaxWsClientInitializer, url);
    return jaxWsClientInitializer;
  }

  @Override
  public void apply(JaxWsClientInitializer jaxWsClientInitializer, String url) {
    if (StringUtils.isBlank(url)) {
      throw new IllegalArgumentException("URL is missing");
    }
    URI uri;
    try {
      uri = new URI(url);
    }
    catch (URISyntaxException ex) {
      throw new IllegalArgumentException("Invalid URL: " + ex.getMessage());
    }

    for (HttpClientConfig configItem : configItems.values()) {
      if (configItem.matchesHost(uri.getHost()) && configItem.matchesPath(uri.getPath())
          && configItem.matchesWsAddressingToUri(jaxWsClientInitializer.getWSAddressingToUri())) {
        apply(jaxWsClientInitializer, configItem);
        break;
      }
    }
  }

  private void apply(JaxWsClientInitializer jaxws, HttpClientConfig config) {
    jaxws.setConnectTimeout(config.getConnectTimeout());
    jaxws.setSocketTimeout(config.getSocketTimeout());
    jaxws.setHttpUser(config.getHttpUser());
    jaxws.setHttpPassword(config.getHttpPassword());
    jaxws.setProxyHost(config.getProxyHost());
    jaxws.setProxyPort(config.getProxyPort());
    jaxws.setProxyUser(config.getProxyUser());
    jaxws.setProxyPassword(config.getProxyPassword());
    jaxws.setSslContextType(config.getSslContextType());
    jaxws.setKeyManagerType(config.getKeyManagerType());
    jaxws.setKeyStoreType(config.getKeyStoreType());
    jaxws.setKeyStoreProvider(config.getKeyStoreProvider());
    jaxws.setKeyStorePath(config.getKeyStorePath());
    jaxws.setKeyStorePassword(config.getKeyStorePassword());
    jaxws.setTrustManagerType(config.getTrustManagerType());
    jaxws.setTrustStoreType(config.getTrustStoreType());
    jaxws.setTrustStoreProvider(config.getTrustStoreProvider());
    jaxws.setTrustStorePath(config.getTrustStorePath());
    jaxws.setTrustStorePassword(config.getTrustStorePassword());
  }

}
