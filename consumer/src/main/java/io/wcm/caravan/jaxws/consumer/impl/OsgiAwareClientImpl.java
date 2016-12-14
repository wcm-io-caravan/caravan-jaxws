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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImplFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Conduit;

/**
 * Enhances {@link ClientImpl} by ensuring that each webservice method invoke is called within the context of
 * an OSGI aware classloader.
 */
public final class OsgiAwareClientImpl extends ClientImpl {

  OsgiAwareClientImpl(Bus b, Endpoint e, Conduit c) {
    super(b, e, c);
  }

  OsgiAwareClientImpl(Bus b, Endpoint e, ConduitSelector sc) {
    super(b, e, sc);
  }

  OsgiAwareClientImpl(Bus b, Endpoint e) {
    super(b, e);
  }

  OsgiAwareClientImpl(Bus bus, Service svc, QName port, EndpointImplFactory endpointImplFactory) {
    super(bus, svc, port, endpointImplFactory);
  }

  @Override
  public Object[] invoke(BindingOperationInfo oi, Object[] params, Map<String, Object> context, Exchange exchange) throws Exception {
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // set classloader to CXF bundle class loader to avoid OSGI classloader problems
      Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

      return super.invoke(oi, params, context, exchange);
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

}
