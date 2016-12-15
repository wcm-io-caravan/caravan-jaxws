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

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.xml.ws.BindingProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.caravan.jaxws.consumer.impl.OsgiAwareJaxWsClientFactoryBean;

/**
 * Default JAX-WS SOAP client initializer
 */
@ConsumerType
public class JaxWsClientInitializer {

  // Use to disable jaxb default validation for CXF
  private static final String JAXB_VALIDATION = "set-jaxb-validation-event-handler";
  private static final String SCHEMA_VALIDATION = "schema-validation-enabled";

  /**
   * List of properties of this class that contain sensitive information which should not be logged.
   */
  public static final String[] SENSITIVE_PROPERTY_NAMES = new String[] {
      "proxyPassword",
      "certstorePassword",
      "truststorePassword"
  };

  private String username;
  private String password;
  private boolean ignoreUnexpectedElements;
  private boolean allowChunking = true;
  private String wsAdressingUrl;
  private int timeoutConnection;
  private int timeoutReceive;
  private String proxyHost;
  private int proxyPort;
  private String proxyUsername;
  private String proxyPassword;
  private String certstorePath;
  private String certstorePassword;
  private String truststorePath;
  private String truststorePassword;

  private transient TLSClientParameters tlsClientParameters;

  /**
   * Initialize JAXWS proxy factory bean
   * @param factory JAXWS Proxy factory bean
   */
  public void initializeFactory(JaxWsProxyFactoryBean factory) {

    // set outgoing security (username/password)
    if (StringUtils.isNotEmpty(getUsername())) {
      factory.setUsername(getUsername());
      factory.setPassword(getPassword());
    }

    // enable WS-addressing
    if (StringUtils.isNotEmpty(getWSAdressingUrl())) {
      factory.getFeatures().add(new WSAddressingFeature());
    }

  }

  /**
   * Initialize SOAP client pFactory and create SOAP client object instance.
   * @param factory JAXWS proxy pFactory bean (has to be already initialized)
   * @return SOAP client object
   */
  public Object createClient(JaxWsProxyFactoryBean factory) {
    try {

      // create port object
      Object portObject = createPortObject(factory);

      // initialize endpiont client
      Client endpointClient = ClientProxy.getClient(portObject);
      initializeEndpointClient(endpointClient);

      // set http settings
      HTTPConduit httpConduit = (HTTPConduit)endpointClient.getConduit();
      initializeHttpConduit(httpConduit);

      // make sure request context is per thread local
      // see http://cxf.apache.org/faq.html#FAQ-AreJAXWSclientproxiesthreadsafe%3F
      ((BindingProvider)portObject).getRequestContext().put(JaxWsClientProxy.THREAD_LOCAL_REQUEST_CONTEXT, Boolean.TRUE);

      // ignore unexpected elements and attributes on data binding (DVD-963)
      if (isIgnoreUnexpectedElements()) {
        // disable schema validation to be upward-compatible to future IA schema changes
        ((BindingProvider)portObject).getRequestContext().put(JAXB_VALIDATION, false);
        ((BindingProvider)portObject).getRequestContext().put(SCHEMA_VALIDATION, false);
      }

      return portObject;
    }
    catch (Throwable ex) {
      throw new JaxWsClientInitializeException("SOAP client initialization failed "
          + "(" + factory.getServiceClass().getName() + ").", ex);
    }
  }

  /**
   * Create port object
   * @param factory JAXWS proxy factory bean
   * @return Port object
   */
  protected Object createPortObject(JaxWsProxyFactoryBean factory) {
    return factory.create();
  }

  /**
   * Initialize endpoint client
   * @param endpointClient Endpoint client
   */
  protected void initializeEndpointClient(Client endpointClient) {

    // set destination address for WS-addressing
    if (StringUtils.isNotEmpty(getWSAdressingUrl())) {
      endpointClient.getOutInterceptors().add(new AbstractPhaseInterceptor<Message>(Phase.SETUP) {

        @Override
        public void handleMessage(Message pMessage) throws Fault {
          AttributedURIType uri = new AttributedURIType();
          uri.setValue(getWSAdressingUrl());

          AddressingProperties maps = new AddressingProperties();
          maps.setTo(uri);

          pMessage.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, maps);
        }

      });
    }

    // add intercepteros
    addInterceptors(endpointClient);

  }

  /**
   * Add interceptors (e.g. for request/response logging)
   * @param endpointClient Endpoint client
   */
  protected void addInterceptors(Client endpointClient) {
    // can be overridden by subclasses
  }

  /**
   * Initialize HTTP conduit
   * @param httpConduit HTTP conduit
   * @throws GeneralSecurityException security exception
   * @throws IOException I/O exception
   */
  protected void initializeHttpConduit(HTTPConduit httpConduit) throws IOException, GeneralSecurityException {

    HTTPClientPolicy clientPolicy = httpConduit.getClient();
    clientPolicy.setAllowChunking(isAllowChunking());
    if (getTimeoutConnection() > 0) {
      clientPolicy.setConnectionTimeout(getTimeoutConnection());
    }
    if (getTimeoutReceive() > 0) {
      clientPolicy.setReceiveTimeout(getTimeoutReceive());
    }

    // optionally enable proxy server
    if (StringUtils.isNotEmpty(getProxyHost()) && getProxyPort() > 0) {
      clientPolicy.setProxyServerType(ProxyServerType.HTTP);
      clientPolicy.setProxyServer(getProxyHost());
      clientPolicy.setProxyServerPort(getProxyPort());

      // optionally define proxy authentication
      if (StringUtils.isNotEmpty(getProxyUsername())) {
        ProxyAuthorizationPolicy proxyAuthentication = new ProxyAuthorizationPolicy();
        proxyAuthentication.setUserName(getProxyUsername());
        proxyAuthentication.setPassword(getProxyPassword());
        httpConduit.setProxyAuthorization(proxyAuthentication);
      }
    }

    // setup TLS - enable certificate for WS access
    if (StringUtils.isNotEmpty(getCertstorePath()) || StringUtils.isNotEmpty(getTruststorePath())) {
      httpConduit.setTlsClientParameters(getTLSClientParameters());
    }

  }

  /**
   * Create TLS client parameters based on given certstore path/password parameters.
   * Caches the parameter in member variable of this factory.
   * @return TLS client parameters
   * @throws IOException
   * @throws GeneralSecurityException
   */
  protected TLSClientParameters getTLSClientParameters() throws IOException, GeneralSecurityException {
    if (tlsClientParameters == null) {
      TLSClientParameters tlsCP = new TLSClientParameters();

      /* TODO: integrate with certloader from commons.httpclient

      // initialize certstore
      if (StringUtils.isNotEmpty(getCertstorePath())) {
        try {
          KeyManagerFactory keyManagerFactory = CertificateLoader.getKeyManagerFactory(
              getCertstorePath(),
              getCertstorePassword(),
              CertificateLoader.KEY_MANAGER_TYPE_DEFAULT,
              CertificateLoader.KEY_STORE_TYPE_DEFAULT
              );
          tlsCP.setKeyManagers(keyManagerFactory.getKeyManagers());
        }
        catch (Throwable ex) {
          throw new RuntimeException("Unable to initialize certificate store for SOAP endpoint.\n"
              + "Please check configuration parameters 'certstorePath' and 'certstorePassword' in this config:\n"
              + this.toString(), ex);
        }
      }

      // initialize truststore
      if (StringUtils.isNotEmpty(getTruststorePath())) {
        try {
          TrustManagerFactory trustManagerFactory = CertificateLoader.getTrustManagerFactory(
              getTruststorePath(),
              getTruststorePassword(),
              CertificateLoader.TRUST_MANAGER_TYPE_DEFAULT,
              CertificateLoader.TRUST_STORE_TYPE_DEFAULT
              );
          tlsCP.setTrustManagers(trustManagerFactory.getTrustManagers());
        }
        catch (Throwable ex) {
          throw new RuntimeException("Unable to initialize trust store for SOAP endpoint.\n"
              + "Please check configuration parameters 'truststorePath' and 'truststorePassword' in this config:\n"
              + this.toString(), ex);
        }
      }

       */

      tlsClientParameters = tlsCP;
    }
    return tlsClientParameters;
  }

  /**
   * Get JAX-WS client factory bean instance.
   * @return Client factory bean.
   */
  public ClientFactoryBean createClientFactoryBean() {
    return new OsgiAwareJaxWsClientFactoryBean();
  }

  /**
   * @return Outgoing authentication: username
   */
  public final String getUsername() {
    return this.username;
  }

  /**
   * @param value Outgoing authentication: username
   */
  public final void setUsername(String value) {
    this.username = value;
  }

  /**
   * @return Outgoing authentication: password
   */
  public final String getPassword() {
    return this.password;
  }

  /**
   * @param value Outgoing authentication: password
   */
  public final void setPassword(String value) {
    this.password = value;
  }

  /**
   * @return If true compatibility mode for WSDL/schema changes is activated.
   *         If the SOAP server returns unknown XML element they are ignored during validation.
   */
  public final boolean isIgnoreUnexpectedElements() {
    return this.ignoreUnexpectedElements;
  }

  /**
   * @param value If true compatibility mode for WSDL/schema changes is activated.
   *          If the SOAP server returns unknown XML element they are ignored during validation.
   */
  public final void setIgnoreUnexpectedElements(boolean value) {
    this.ignoreUnexpectedElements = value;
  }

  /**
   * @return Allow HTTP 1.1 chunking
   */
  public final boolean isAllowChunking() {
    return this.allowChunking;
  }

  /**
   * @param value Allow HTTP 1.1 chunking
   */
  public final void setAllowChunking(boolean value) {
    this.allowChunking = value;
  }

  /**
   * @return Adressing URL if WS-Adressing feature should be used
   */
  public final String getWSAdressingUrl() {
    return this.wsAdressingUrl;
  }

  /**
   * @param value Adressing URL if WS-Adressing feature should be used
   */
  public final void setWSAdressingUrl(String value) {
    this.wsAdressingUrl = value;
  }

  /**
   * @return HTTP connection timeout
   */
  public final int getTimeoutConnection() {
    return this.timeoutConnection;
  }

  /**
   * @param value HTTP connection timeout
   */
  public final void setTimeoutConnection(int value) {
    this.timeoutConnection = value;
  }

  /**
   * @return HTTP data receive timout
   */
  public final int getTimeoutReceive() {
    return this.timeoutReceive;
  }

  /**
   * @param value HTTP data receive timout
   */
  public final void setTimeoutReceive(int value) {
    this.timeoutReceive = value;
  }

  /**
   * @return Proxy host name
   */
  public String getProxyHost() {
    return this.proxyHost;
  }

  /**
   * @param value Proxy host name
   */
  public void setProxyHost(String value) {
    this.proxyHost = value;
  }

  /**
   * @return Proxy port
   */
  public int getProxyPort() {
    return this.proxyPort;
  }

  /**
   * @param value Proxy port
   */
  public void setProxyPort(int value) {
    this.proxyPort = value;
  }

  /**
   * @return Proxy user name
   */
  public String getProxyUsername() {
    return this.proxyUsername;
  }

  /**
   * @param value Proxy user name
   */
  public void setProxyUsername(String value) {
    this.proxyUsername = value;
  }

  /**
   * @return Proxy password
   */
  public String getProxyPassword() {
    return this.proxyPassword;
  }

  /**
   * @param value Proxy password
   */
  public void setProxyPassword(String value) {
    this.proxyPassword = value;
  }

  /**
   * @return Certificate store path
   */
  public String getCertstorePath() {
    return this.certstorePath;
  }

  /**
   * @param value Certificate store path
   */
  public final void setCertstorePath(String value) {
    this.certstorePath = value;
  }

  /**
   * @return Certificate store password
   */
  public final String getCertstorePassword() {
    return this.certstorePassword;
  }

  /**
   * @param value Certificate store password
   */
  public final void setCertstorePassword(String value) {
    this.certstorePassword = value;
  }

  /**
   * @return Trust store path
   */
  public final String getTruststorePath() {
    return this.truststorePath;
  }

  /**
   * @param value Trust store path
   */
  public final void setTruststorePath(String value) {
    this.truststorePath = value;
  }

  /**
   * @return Trust store password
   */
  public final String getTruststorePassword() {
    return this.truststorePassword;
  }

  /**
   * @param value Trust store password
   */
  public final void setTruststorePassword(String value) {
    this.truststorePassword = value;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object pObj) {
    return EqualsBuilder.reflectionEquals(this, pObj, false);
  }

}
