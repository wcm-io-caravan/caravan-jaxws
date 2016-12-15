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

import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.CONNECT_TIMEOUT_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.HOST_PATTERNS_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.HTTP_PASSWORD_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.HTTP_USER_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.KEYMANAGER_TYPE_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.KEYSTORE_PASSWORD_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.KEYSTORE_PATH_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.KEYSTORE_TYPE_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.PATH_PATTERNS_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.PROXY_HOST_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.PROXY_PASSWORD_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.PROXY_PORT_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.PROXY_USER_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.SOCKET_TIMEOUT_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.SSL_CONTEXT_TYPE_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.TRUSTMANAGER_TYPE_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.TRUSTSTORE_PASSWORD_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.TRUSTSTORE_PATH_PROPERTY;
import static io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl.TRUSTSTORE_TYPE_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.caravan.commons.httpclient.impl.HttpClientConfigImpl;
import io.wcm.caravan.jaxws.consumer.JaxWsClientInitializer;
import io.wcm.caravan.jaxws.consumer.httpclient.JaxWsClientInitializerHttpClientConfig;

public class JaxWsClientInitializerHttpClientConfigImplTest {

  private static final String TEST_URI = "http://server1:4523/mypath";

  @Rule
  public OsgiContext context = new OsgiContext();

  private JaxWsClientInitializerHttpClientConfig underTest;

  @Before
  public void setUp() {
    underTest = context.registerInjectActivateService(new JaxWsClientInitializerHttpClientConfigImpl());
  }

  @Test
  public void testNoConfigs() {
    JaxWsClientInitializer jaxws = underTest.get(TEST_URI);

    assertNull(jaxws.getHttpUser());
    assertNull(jaxws.getHttpPassword());
  }

  @Test
  public void testWithConfig() {
    context.registerInjectActivateService(new HttpClientConfigImpl(),
        HOST_PATTERNS_PROPERTY, "^server.*$",
        PATH_PATTERNS_PROPERTY, ".*path.*",
        CONNECT_TIMEOUT_PROPERTY, 123,
        SOCKET_TIMEOUT_PROPERTY, 456,
        HTTP_USER_PROPERTY, "user1",
        HTTP_PASSWORD_PROPERTY, "pwd1",
        PROXY_HOST_PROPERTY, "host1",
        PROXY_PORT_PROPERTY, 789,
        PROXY_USER_PROPERTY, "proxyUser1",
        PROXY_PASSWORD_PROPERTY, "proxyPwd1",
        SSL_CONTEXT_TYPE_PROPERTY, "sslType1",
        KEYMANAGER_TYPE_PROPERTY, "keyManager1",
        KEYSTORE_TYPE_PROPERTY, "keyStore1",
        KEYSTORE_PATH_PROPERTY, "keyPath1",
        KEYSTORE_PASSWORD_PROPERTY, "keyPwd1",
        TRUSTMANAGER_TYPE_PROPERTY, "trustManager1",
        TRUSTSTORE_TYPE_PROPERTY, "trustStore1",
        TRUSTSTORE_PATH_PROPERTY, "trustPath1",
        TRUSTSTORE_PASSWORD_PROPERTY, "trustPwd1");

    JaxWsClientInitializer jaxws = underTest.get(TEST_URI);
    assertNotNull(jaxws);

    assertEquals(123, jaxws.getConnectTimeout());
    assertEquals(456, jaxws.getSocketTimeout());
    assertEquals("user1", jaxws.getHttpUser());
    assertEquals("pwd1", jaxws.getHttpPassword());
    assertEquals("host1", jaxws.getProxyHost());
    assertEquals(789, jaxws.getProxyPort());
    assertEquals("proxyUser1", jaxws.getProxyUser());
    assertEquals("proxyPwd1", jaxws.getProxyPassword());
    assertEquals("sslType1", jaxws.getSslContextType());
    assertEquals("keyManager1", jaxws.getKeyManagerType());
    assertEquals("keyStore1", jaxws.getKeyStoreType());
    assertEquals("keyPath1", jaxws.getKeyStorePath());
    assertEquals("keyPwd1", jaxws.getKeyStorePassword());
    assertEquals("trustManager1", jaxws.getTrustManagerType());
    assertEquals("trustStore1", jaxws.getTrustStoreType());
    assertEquals("trustPath1", jaxws.getTrustStorePath());
    assertEquals("trustPwd1", jaxws.getTrustStorePassword());
  }

}
