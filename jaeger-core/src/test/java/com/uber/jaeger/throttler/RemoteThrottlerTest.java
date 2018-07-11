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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uber.jaeger.TracedProcess;
import com.uber.jaeger.metrics.InMemoryMetricsFactory;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.throttler.http.CreditResponse;
import com.uber.jaeger.throttler.http.OperationBalance;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteThrottlerTest {
  private static final int CLIENT_ID = 123;
  private static final String SERVICE_NAME = "test-service";
  private static final String OPERATION_NAME = "test-operation";

  @Mock HttpThrottlerProxy proxy;
  private RemoteThrottler throttler;
  private InMemoryMetricsFactory metricsFactory;
  private Metrics metrics;

  @Before
  public void setUp() {
    metricsFactory = new InMemoryMetricsFactory();
    metrics = new Metrics(metricsFactory);
  }

  @After
  public void tearDown() {
    throttler.close();
  }

  @Test
  public void testFetchCredits() throws Exception {
    final int refreshIntervalMs = 10;
    throttler = new RemoteThrottler(proxy, metrics, refreshIntervalMs, true);
    throttler.isAllowed(OPERATION_NAME);
    final List<OperationBalance> operationBalances =
        Collections.singletonList(new OperationBalance(OPERATION_NAME, 1));
    final CreditResponse response = new CreditResponse(operationBalances);
    when(proxy.getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), anyList()))
        .thenReturn(response)
        .thenReturn(new CreditResponse(Collections.EMPTY_LIST));
    verify(proxy, timeout(refreshIntervalMs * 10).times(0))
        .getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), anyList());

    throttler.setProcess(new TracedProcess(SERVICE_NAME, 123, Collections.EMPTY_MAP));
    final List<String> operations = Collections.singletonList(OPERATION_NAME);
    verify(proxy, timeout(refreshIntervalMs * 10).atLeastOnce())
        .getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), eq(operations));
    assertTrue(
        metricsFactory.getCounter(
                "jaeger:throttler_updates", Collections.singletonMap("result", "ok"))
            > 0);
    assertTrue(throttler.isAllowed(OPERATION_NAME));
    assertFalse(throttler.isAllowed(OPERATION_NAME));
    assertFalse(throttler.isAllowed(OPERATION_NAME));
  }

  @Test
  public void testNullResponse() throws Exception {
    final int refreshIntervalMs = 10;
    throttler = new RemoteThrottler(proxy, metrics, refreshIntervalMs, false);
    throttler.setProcess(new TracedProcess(SERVICE_NAME, 123, Collections.EMPTY_MAP));
    throttler.isAllowed(OPERATION_NAME);
    final List<String> operations = Collections.singletonList(OPERATION_NAME);
    verify(proxy, timeout(refreshIntervalMs * 10).atLeastOnce())
        .getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), eq(operations));
    assertTrue(
        metricsFactory.getCounter(
                "jaeger:throttler_updates", Collections.singletonMap("result", "err"))
            > 0);
  }
}
