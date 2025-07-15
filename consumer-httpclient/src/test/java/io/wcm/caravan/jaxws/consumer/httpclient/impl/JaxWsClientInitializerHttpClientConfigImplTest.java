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
        "hostPatterns", "^server.*$",
        "pathPatterns", ".*path.*",
        "connectTimeout", 123,
        "socketTimeout", 456,
        "httpUser", "user1",
        "httpPassword", "pwd1",
        "proxyHost", "host1",
        "proxyPort", 789,
        "proxyUser", "proxyUser1",
        "proxyPassword", "proxyPwd1",
        "sslContextType", "sslType1",
        "keyManagerType", "keyManager1",
        "keyStoreType", "keyStore1",
        "keyStoreProvider", "keyStoreProvider1",
        "keyStorePath", "keyPath1",
        "keyStorePassword", "keyPwd1",
        "trustManagerType", "trustManager1",
        "trustStoreType", "trustStore1",
        "trustStoreProvider", "trustStoreProvider1",
        "trustStorePath", "trustPath1",
        "trustStorePassword", "trustPwd1");

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
    assertEquals("keyStoreProvider1", jaxws.getKeyStoreProvider());
    assertEquals("keyPath1", jaxws.getKeyStorePath());
    assertEquals("keyPwd1", jaxws.getKeyStorePassword());
    assertEquals("trustManager1", jaxws.getTrustManagerType());
    assertEquals("trustStore1", jaxws.getTrustStoreType());
    assertEquals("trustStoreProvider1", jaxws.getTrustStoreProvider());
    assertEquals("trustPath1", jaxws.getTrustStorePath());
    assertEquals("trustPwd1", jaxws.getTrustStorePassword());
  }

}
