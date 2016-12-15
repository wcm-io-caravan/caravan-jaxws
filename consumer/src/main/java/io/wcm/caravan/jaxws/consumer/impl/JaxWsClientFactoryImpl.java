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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import io.wcm.caravan.jaxws.consumer.JaxWsClientFactory;
import io.wcm.caravan.jaxws.consumer.JaxWsClientInitializer;

/**
 * Factory for creating initializing JAX-WS SOAP clients.
 */
@Component(immediate = true, service = JaxWsClientFactory.class)
public final class JaxWsClientFactoryImpl implements JaxWsClientFactory, BundleListener {

  // cache proxy factor beans per port class/url and initializer class
  private ConcurrentHashMap<String, ConcurrentHashMap<JaxWsClientInitializer, Object>> cacheMap;

  @Activate
  protected void activate(BundleContext bundleContext) {
    bundleContext.addBundleListener(this);
    cacheMap = new ConcurrentHashMap<String, ConcurrentHashMap<JaxWsClientInitializer, Object>>();
  }

  @Deactivate
  protected void deactivate(BundleContext bundleContext) {
    bundleContext.removeBundleListener(this);
    cacheMap = null;
  }

  @Override
  public <T> T create(Class<T> clazz, String portUrl) {
    return create(clazz, portUrl, new JaxWsClientInitializer());
  }

  @Override
  public <T> T create(Class<T> clazz, String portUrl, JaxWsClientInitializer initializer) {
    return create(clazz, portUrl, initializer, null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> clazz, String portUrl, JaxWsClientInitializer initializer, String cacheKeySuffix) {
    if (StringUtils.isEmpty(portUrl)) {
      throw new IllegalArgumentException("Missing port url");
    }
    if (initializer == null) {
      throw new IllegalArgumentException("Missing initialize instance.");
    }

    // build key for port class and port url for cache map access
    String key = clazz.getName() + "#" + portUrl
        + (StringUtils.isNotEmpty(cacheKeySuffix) ? "#" + cacheKeySuffix : "");

    // try to get existing port object from cache
    T portObject = null;
    ConcurrentHashMap<JaxWsClientInitializer, Object> innerCacheMap = cacheMap.get(key);
    if (innerCacheMap != null) {
      portObject = (T)innerCacheMap.get(initializer);
    }

    // if no port object found create new factory and port object instance and put it to cache
    if (portObject == null) {
      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        // set classloader to CXF bundle class loader to avoid OSGI classloader problems
        Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

        // initialize factory and crate port object
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean(initializer.createClientFactoryBean());
        factory.setServiceClass(clazz);
        factory.setAddress(portUrl);
        initializer.initializeFactory(factory);
        portObject = (T)initializer.createClient(factory);

        // put to cache
        if (innerCacheMap == null) {
          innerCacheMap = new ConcurrentHashMap<JaxWsClientInitializer, Object>();
          cacheMap.put(key, innerCacheMap);
        }
        innerCacheMap.put(initializer, portObject);

      }
      finally {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
    }

    // return port object/proxy client
    return portObject;
  }

  /**
   * Bundle changed
   */
  @Override
  public void bundleChanged(BundleEvent event) {
    // clear all cache if any bundle changes
    if (cacheMap != null) {
      cacheMap.clear();
    }
  }

}
