/*
 * Copyright (c) 2018, Uber Technologies, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.uber.jaeger.throttler;

import com.uber.jaeger.mocks.MockAgentResource;
import com.uber.jaeger.throttler.http.CreditResponse;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpThrottlerProxyTest extends JerseyTest {
  private static Properties originalProps;
  private static final int CLIENT_ID = 1234;
  private static final String OPERATION_NAME = "test-operation";

  private HttpThrottlerProxy proxy;

  @BeforeClass
  public static void beforeClass() {
    originalProps = new Properties(System.getProperties());
    if (System.getProperty(TestProperties.CONTAINER_PORT) == null) {
      System.setProperty(TestProperties.CONTAINER_PORT, "0");
    }
  }

  @AfterClass
  public static void afterClass() {
    System.setProperties(originalProps);
  }

  @Override
  protected Application configure() {

    return new ResourceConfig(MockAgentResource.class);
  }

  @Test
  public void testGetCredits() throws Exception {
    URI uri = target().getUri();
    proxy = new HttpThrottlerProxy(uri.getHost() + ":" + uri.getPort());
    final List<String> operations = new ArrayList<String>();
    operations.add(OPERATION_NAME);
    CreditResponse response = proxy.getCredits(CLIENT_ID, "clairvoyant", operations);
    assertNotNull(response);
    assertEquals(1, response.getBalances().size());
    assertEquals(OPERATION_NAME, response.getBalances().get(0).getOperation());
    assertEquals(1, response.getBalances().get(0).getBalance(), 0.0001);

    try {
      proxy.getCredits(CLIENT_ID, "invalid-service", operations);
    } catch (ThrottlerException e) {
      return;
    }

    assert(false);
  }
}
