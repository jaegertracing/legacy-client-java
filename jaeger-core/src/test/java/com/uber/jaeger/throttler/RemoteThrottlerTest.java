package com.uber.jaeger.throttler;

import com.uber.jaeger.TracedProcess;
import com.uber.jaeger.metrics.InMemoryMetricsFactory;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.throttler.http.CreditResponse;
import com.uber.jaeger.throttler.http.OperationBalance;
import org.glassfish.hk2.api.Operation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    final int REFRESH_INTERVAL_MS = 10;
    throttler = new RemoteThrottler(proxy, metrics, REFRESH_INTERVAL_MS, true);
    throttler.isAllowed(OPERATION_NAME);
    final List<OperationBalance> operationBalances = Collections.singletonList(new OperationBalance(OPERATION_NAME, 1));
    final CreditResponse response = new CreditResponse(operationBalances);
    when(proxy.getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), anyList())).thenReturn(response).thenReturn(new CreditResponse(Collections.EMPTY_LIST));
    verify(proxy, timeout(REFRESH_INTERVAL_MS * 3).times(0)).getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), anyList());

    throttler.setProcess(new TracedProcess(SERVICE_NAME, 123, Collections.EMPTY_MAP));
    final List<String> operations = Collections.singletonList(OPERATION_NAME);
    verify(proxy, timeout(REFRESH_INTERVAL_MS * 10).atLeastOnce()).getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), eq(operations));
    assertTrue(metricsFactory.getCounter("jaeger:throttler_updates", Collections.singletonMap("result", "ok")) > 0);
    assertTrue(throttler.isAllowed(OPERATION_NAME));
    assertFalse(throttler.isAllowed(OPERATION_NAME));
    assertFalse(throttler.isAllowed(OPERATION_NAME));
  }

  @Test
  public void testNullResponse() throws Exception {
    final int REFRESH_INTERVAL_MS = 10;
    throttler = new RemoteThrottler(proxy, metrics, REFRESH_INTERVAL_MS, false);
    throttler.setProcess(new TracedProcess(SERVICE_NAME, 123, Collections.EMPTY_MAP));
    throttler.isAllowed(OPERATION_NAME);
    final List<String> operations = Collections.singletonList(OPERATION_NAME);
    verify(proxy, timeout(REFRESH_INTERVAL_MS * 10).atLeastOnce()).getCredits(eq(CLIENT_ID), eq(SERVICE_NAME), eq(operations));
    assertTrue(metricsFactory.getCounter("jaeger:throttler_updates", Collections.singletonMap("result", "err")) > 0);
  }
}
