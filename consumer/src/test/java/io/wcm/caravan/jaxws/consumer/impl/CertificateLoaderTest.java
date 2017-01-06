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

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Rule;
import org.junit.Test;

import io.wcm.caravan.jaxws.consumer.JaxWsClientInitializer;

public class CertificateLoaderTest {

  @Rule
  public OsgiContext context = new OsgiContext();

  public static final String KEYSTORE_PATH = "/certificates/testcert.p12";
  public static final String KEYSTORE_PASSWORD = "test-certificate";
  public static final String TRUSTSTORE_PATH = "/certificates/trust.jks";
  public static final String TRUSTSTORE_PASSWORD = "test-keystore";

  @Test
  public void testGetKeyManagerFactory() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setKeyStorePath(KEYSTORE_PATH);
    config.setKeyStorePassword(KEYSTORE_PASSWORD);

    KeyManagerFactory keyManagerFactory = CertificateLoader.getKeyManagerFactory(config);
    assertNotNull(keyManagerFactory);
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetKeyManagerFactoryInvalidPath() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setKeyStorePath("/invalid/path");
    config.setKeyStorePassword(KEYSTORE_PASSWORD);

    CertificateLoader.getKeyManagerFactory(config);
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetKeyManagerFactoryNullPath() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setKeyStorePath(null);
    config.setKeyStorePassword(KEYSTORE_PASSWORD);

    CertificateLoader.getKeyManagerFactory(config);
  }

  @Test
  public void testGetTrustManagerFactory() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setTrustStorePath(TRUSTSTORE_PATH);
    config.setTrustStorePassword(TRUSTSTORE_PASSWORD);

    TrustManagerFactory trustManagerFactory = CertificateLoader.getTrustManagerFactory(config);
    assertNotNull(trustManagerFactory);
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetTrustManagerFactoryInvalidPath() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setTrustStorePath("/invalid/path");
    config.setTrustStorePassword(TRUSTSTORE_PASSWORD);

    CertificateLoader.getTrustManagerFactory(config);
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetTrustManagerFactoryNullPath() throws IOException, GeneralSecurityException {
    JaxWsClientInitializer config = new JaxWsClientInitializer();
    config.setTrustStorePath(null);
    config.setTrustStorePassword(TRUSTSTORE_PASSWORD);

    CertificateLoader.getTrustManagerFactory(config);
  }

}
