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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
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

import io.wcm.caravan.jaxws.consumer.impl.CertificateLoader;
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
      "trustStorePassword"
  };

  private int connectTimeout;
  private int socketTimeout;
  private String httpUser;
  private String httpPassword;
  private String proxyHost;
  private int proxyPort;
  private String proxyUser;
  private String proxyPassword;
  private String sslContextType = CertificateLoader.SSL_CONTEXT_TYPE_DEFAULT;
  private String keyManagerType = CertificateLoader.KEY_MANAGER_TYPE_DEFAULT;
  private String keyStoreType = CertificateLoader.KEY_STORE_TYPE_DEFAULT;
  private String keyStorePath;
  private String keyStorePassword;
  private String trustManagerType = CertificateLoader.TRUST_MANAGER_TYPE_DEFAULT;
  private String trustStoreType = CertificateLoader.TRUST_STORE_TYPE_DEFAULT;
  private String trustStorePath;
  private String trustStorePassword;
  private String wsAddressingToUri;
  private boolean ignoreUnexpectedElements;
  private boolean allowChunking = true;

  private transient TLSClientParameters tlsClientParameters;

  /**
   * Initialize JAXWS proxy factory bean
   * @param factory JAXWS Proxy factory bean
   */
  public void initializeFactory(JaxWsProxyFactoryBean factory) {

    // set outgoing security (username/password)
    if (StringUtils.isNotEmpty(getHttpUser())) {
      factory.setUsername(getHttpUser());
      factory.setPassword(getHttpPassword());
    }

    // enable WS-addressing
    if (StringUtils.isNotEmpty(getWSAddressingToUri())) {
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

      // ignore unexpected elements and attributes on data binding
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
    if (StringUtils.isNotEmpty(getWSAddressingToUri())) {
      endpointClient.getOutInterceptors().add(new AbstractPhaseInterceptor<Message>(Phase.SETUP) {

        @Override
        public void handleMessage(Message pMessage) throws Fault {
          AttributedURIType uri = new AttributedURIType();
          uri.setValue(getWSAddressingToUri());

          AddressingProperties maps = new AddressingProperties();
          maps.setTo(uri);

          pMessage.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, maps);
        }

      });
    }

    // add interceptors
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
    if (getConnectTimeout() > 0) {
      clientPolicy.setConnectionTimeout(getConnectTimeout());
    }
    if (getSocketTimeout() > 0) {
      clientPolicy.setReceiveTimeout(getSocketTimeout());
    }

    // optionally enable proxy server
    if (StringUtils.isNotEmpty(getProxyHost()) && getProxyPort() > 0) {
      clientPolicy.setProxyServerType(ProxyServerType.HTTP);
      clientPolicy.setProxyServer(getProxyHost());
      clientPolicy.setProxyServerPort(getProxyPort());

      // optionally define proxy authentication
      if (StringUtils.isNotEmpty(getProxyUser())) {
        ProxyAuthorizationPolicy proxyAuthentication = new ProxyAuthorizationPolicy();
        proxyAuthentication.setUserName(getProxyUser());
        proxyAuthentication.setPassword(getProxyPassword());
        httpConduit.setProxyAuthorization(proxyAuthentication);
      }
    }

    // setup TLS - enable certificate for WS access
    if (CertificateLoader.isSslKeyManagerEnabled(this) || CertificateLoader.isSslTrustStoreEnbaled(this)) {
      httpConduit.setTlsClientParameters(getTLSClientParameters());
    }

  }

  /**
   * Get JAX-WS client factory bean instance.
   * @return Client factory bean.
   */
  public ClientFactoryBean createClientFactoryBean() {
    return new OsgiAwareJaxWsClientFactoryBean();
  }

  /**
   * @return Connection timeout in ms.
   */
  public final int getConnectTimeout() {
    return this.connectTimeout;
  }

  /**
   * @param value Connection timeout in ms.
   */
  public final void setConnectTimeout(int value) {
    this.connectTimeout = value;
  }

  /**
   * @return Response timeout in ms.
   */
  public final int getSocketTimeout() {
    return this.socketTimeout;
  }

  /**
   * @param value Response timeout in ms.
   */
  public final void setSocketTimeout(int value) {
    this.socketTimeout = value;
  }

  /**
   * @return Http basic authentication user.
   */
  public final String getHttpUser() {
    return this.httpUser;
  }

  /**
   * @param value Http basic authentication user.
   */
  public final void setHttpUser(String value) {
    this.httpUser = value;
  }

  /**
   * @return Http basic authentication password
   */
  public final String getHttpPassword() {
    return this.httpPassword;
  }

  /**
   * @param value Http basic authentication password
   */
  public final void setHttpPassword(String value) {
    this.httpPassword = value;
  }

  /**
   * @return Proxy host name
   */
  public final String getProxyHost() {
    return this.proxyHost;
  }

  /**
   * @param value Proxy host name
   */
  public final void setProxyHost(String value) {
    this.proxyHost = value;
  }

  /**
   * @return Proxy port
   */
  public final int getProxyPort() {
    return this.proxyPort;
  }

  /**
   * @param value Proxy port
   */
  public final void setProxyPort(int value) {
    this.proxyPort = value;
  }

  /**
   * @return Proxy user name
   */
  public final String getProxyUser() {
    return this.proxyUser;
  }

  /**
   * @param value Proxy user name
   */
  public final void setProxyUser(String value) {
    this.proxyUser = value;
  }

  /**
   * @return Proxy password
   */
  public final String getProxyPassword() {
    return this.proxyPassword;
  }

  /**
   * @param value Proxy password
   */
  public final void setProxyPassword(String value) {
    this.proxyPassword = value;
  }

  /**
   * @return SSL context type (default: TLS)
   */
  public final String getSslContextType() {
    return this.sslContextType;
  }

  /**
   * @param value SSL context type (default: TLS)
   */
  public final void setSslContextType(String value) {
    this.sslContextType = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Key manager type (default: SunX509)
   */
  public final String getKeyManagerType() {
    return this.keyManagerType;
  }

  /**
   * @param value Key manager type (default: SunX509)
   */
  public final void setKeyManagerType(String value) {
    this.keyManagerType = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Key store type (default: PKCS12)
   */
  public final String getKeyStoreType() {
    return this.keyStoreType;
  }

  /**
   * @param value Key store type (default: PKCS12)
   */
  public final void setKeyStoreType(String value) {
    this.keyStoreType = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Key store file path
   */
  public final String getKeyStorePath() {
    return this.keyStorePath;
  }

  /**
   * @param value Key store file path
   */
  public final void setKeyStorePath(String value) {
    this.keyStorePath = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Key store password
   */
  public final String getKeyStorePassword() {
    return this.keyStorePassword;
  }

  /**
   * @param value Key store password
   */
  public final void setKeyStorePassword(String value) {
    this.keyStorePassword = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Trust manager type (default: SunX509)
   */
  public final String getTrustManagerType() {
    return this.trustManagerType;
  }

  /**
   * @param value Trust manager type (default: SunX509)
   */
  public final void setTrustManagerType(String value) {
    this.trustManagerType = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Trust store type (default: JKS)
   */
  public final String getTrustStoreType() {
    return this.trustStoreType;
  }

  /**
   * @param value Trust store type (default: JKS)
   */
  public final void setTrustStoreType(String value) {
    this.trustStoreType = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Trust store file path
   */
  public final String getTrustStorePath() {
    return this.trustStorePath;
  }

  /**
   * @param value Trust store file path
   */
  public final void setTrustStorePath(String value) {
    this.trustStorePath = value;
    this.tlsClientParameters = null;
  }

  /**
   * @return Trust store password
   */
  public final String getTrustStorePassword() {
    return this.trustStorePassword;
  }

  /**
   * @param vaule Trust store password
   */
  public final void setTrustStorePassword(String vaule) {
    this.trustStorePassword = vaule;
    this.tlsClientParameters = null;
  }

  /**
   * Create TLS client parameters based on given certstore path/password parameters.
   * Caches the parameter in member variable of this factory.
   * @return TLS client parameters
   * @throws IOException I/O exception
   * @throws GeneralSecurityException General security exception
   */
  public final TLSClientParameters getTLSClientParameters() throws IOException, GeneralSecurityException {
    if (tlsClientParameters == null) {
      TLSClientParameters tlsCP = new TLSClientParameters();

      // initialize certstore
      if (CertificateLoader.isSslTrustStoreEnbaled(this)) {
        try {
          KeyManagerFactory keyManagerFactory = CertificateLoader.getKeyManagerFactory(this);
          tlsCP.setKeyManagers(keyManagerFactory.getKeyManagers());
        }
        catch (Throwable ex) {
          throw new RuntimeException("Unable to initialize certificate store for SOAP endpoint.\n"
              + "Please check configuration parameters 'certstorePath' and 'certstorePassword' in this config:\n"
              + this.toString(), ex);
        }
      }

      // initialize trustStore
      if (CertificateLoader.isSslTrustStoreEnbaled(this)) {
        try {
          TrustManagerFactory trustManagerFactory = CertificateLoader.getTrustManagerFactory(this);
          tlsCP.setTrustManagers(trustManagerFactory.getTrustManagers());
        }
        catch (Throwable ex) {
          throw new RuntimeException("Unable to initialize trust store for SOAP endpoint.\n"
              + "Please check configuration parameters 'trustStorePath' and 'trustStorePassword' in this config:\n"
              + this.toString(), ex);
        }
      }

      tlsClientParameters = tlsCP;
    }
    return tlsClientParameters;
  }

  /**
   * @param value CXF TSL client parameters
   */
  public final void setTLSClientParameters(TLSClientParameters value) {
    this.tlsClientParameters = value;
  }

  /**
   * @return Addressing-To URI to be sent as WS-Adressing header
   */
  public final String getWSAddressingToUri() {
    return this.wsAddressingToUri;
  }

  /**
   * @param value Addressing-To URI to be sent as WS-Adressing header
   */
  public final void setWSAddressingToUri(String value) {
    this.wsAddressingToUri = value;
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

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

  @Override
  public boolean equals(Object pObj) {
    return EqualsBuilder.reflectionEquals(this, pObj, false);
  }

}
