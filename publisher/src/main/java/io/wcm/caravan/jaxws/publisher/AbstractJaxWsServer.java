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
package io.wcm.caravan.jaxws.publisher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.caravan.jaxws.publisher.impl.RequestContext;
import io.wcm.caravan.jaxws.publisher.impl.RequestWrapper;
import io.wcm.caravan.jaxws.publisher.impl.ResponseWrapper;

/**
 * Abstract servlet-based implementation for CXF-based JAX-WS SOAP server.
 * Ensures that correct class loader is used is during initialization and invoking phases.
 * Via getCurrentRequest() and getCurrentResponse() it is possible to access these objects from SOAP method implementations.
 */
@ConsumerType
public abstract class AbstractJaxWsServer extends CXFNonSpringServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Extension for SOAP requests
   */
  public static final String SOAP_EXTENSION = "soap";

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // set classloader to CXF bundle class loader to avoid OSGI classloader problems
      Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

      super.init(servletConfig);

      // initialize SOAP server factory
      ServerFactoryBean factory = initServerFactory();
      factory.create();
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  /**
   * Initialize SOAP server factory (but does not call create)
   * @return SOAP server factory
   */
  protected ServerFactoryBean initServerFactory() {
    // register this class as SOAP service to a virtual path
    ServerFactoryBean factory = new JaxWsServerFactoryBean();
    factory.setBus(getBus());
    factory.setAddress(RequestWrapper.VIRTUAL_PATH);
    factory.setServiceClass(getServerInterfaceType());
    factory.setServiceBean(this);
    return factory;
  }

  @Override
  protected void invoke(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    RequestContext.getThreadLocal().set(new RequestContext(request, response));
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      // set classloader to CXF bundle class loader to avoid OSGI classloader problems
      Thread.currentThread().setContextClassLoader(BusFactory.class.getClassLoader());

      super.invoke(new RequestWrapper(request), new ResponseWrapper(response));
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
      RequestContext.getThreadLocal().remove();
    }
  }

  /**
   * @return Servlet request for current threads SOAP request
   */
  protected HttpServletRequest getCurrentRequest() {
    RequestContext requestContext = RequestContext.getRequestContext();
    if (requestContext == null) {
      throw new IllegalStateException("No current soap request context available.");
    }
    return requestContext.getRequest();
  }

  /**
   * @return Servlet response for current threads SOAP request
   */
  protected HttpServletResponse getCurrentResponse() {
    RequestContext requestContext = RequestContext.getRequestContext();
    if (requestContext == null) {
      throw new IllegalStateException("No current soap request context available.");
    }
    return requestContext.getResponse();
  }

  /**
   * @return Interface of SOAP service
   */
  protected abstract Class getServerInterfaceType();

}
