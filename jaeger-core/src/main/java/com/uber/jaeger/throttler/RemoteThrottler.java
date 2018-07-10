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

import com.uber.jaeger.TracedProcess;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.throttler.http.CreditResponse;
import com.uber.jaeger.throttler.http.OperationBalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RemoteThrottler implements Throttler {
  private static final int DEFAULT_REFRESH_INTERVAL_MS = 5000;
  private static final boolean DEFAULT_SYNCHRONOUS_INITIALIZATION = false;
  private static final double MINIMUM_CREDITS = 1;

  private Map<String, Double> credits = new HashMap<String, Double>();
  private String serviceName;
  private int clientId;
  private final int refreshIntervalMs;
  private final boolean synchronousInitialization;
  private final Metrics metrics;
  private final HttpThrottlerProxy proxy;
  private final Timer pollTimer = new Timer(true);  // true makes this a daemon thread

  public RemoteThrottler(
      HttpThrottlerProxy proxy,
      Metrics metrics,
      int refreshIntervalMs,
      boolean synchronousInitialization
  ) {
    this.proxy = proxy;
    this.refreshIntervalMs = (refreshIntervalMs > 0) ? refreshIntervalMs : DEFAULT_REFRESH_INTERVAL_MS;
    this.synchronousInitialization = synchronousInitialization;
    this.metrics = metrics;
    pollTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        fetchCredits();
      }
    }, 0, refreshIntervalMs);
  }

  public RemoteThrottler(
      HttpThrottlerProxy proxy,
      Metrics metrics) {
    this(
        proxy,
        metrics,
        DEFAULT_REFRESH_INTERVAL_MS,
        DEFAULT_SYNCHRONOUS_INITIALIZATION);
  }

  @Override
  public synchronized boolean isAllowed(String operation) {
    final boolean hasKey = credits.containsKey(operation);
    final double value = hasKey ? credits.get(operation) : 0;
    if (!hasKey || value == 0) {
      if (!hasKey) {
        credits.put(operation, (double) 0);
      }
      if (!synchronousInitialization) {
        metrics.throttledDebugSpans.inc(1);
        return false;
      }
      fetchCredits();
    }
    return isAllowedHelper(operation);
  }

  private boolean isAllowedHelper(String operation) {
    final double value = credits.get(operation);
    if (value < MINIMUM_CREDITS) {
      metrics.throttledDebugSpans.inc(1);
      return false;
    }
    credits.put(operation, value - MINIMUM_CREDITS);
    return true;
  }

  private void fetchCredits() {
    if (serviceName == null || clientId == 0) {
      // TODO: Warn?
      return;
    }
    try {
      CreditResponse response = proxy.getCredits(clientId, serviceName, getOperations());
      metrics.throttlerUpdateSuccess.inc(1);
      mergeCredits(response);
    } catch (ThrottlerException e) {
      metrics.throttlerUpdateFailure.inc(1);
    }
  }

  @Override
  public synchronized void setProcess(TracedProcess process) {
    serviceName = process.getService();
    clientId = process.getClientId();
  }

  private synchronized void mergeCredits(CreditResponse response) throws ThrottlerException {
    if (response == null) {
      throw new ThrottlerException("response is null", new NullPointerException());
    }
    for (OperationBalance opBalance : response.getBalances()) {
      final String operation = opBalance.getOperation();
      credits.put(operation,
          credits.containsKey(operation) ? credits.get(operation) + opBalance.getBalance()
              : 0);
    }
  }

  private synchronized List<String> getOperations() {
    return new ArrayList<String>(credits.keySet());
  }

  @Override
  public void close() {
    synchronized (this) {
      pollTimer.cancel();
    }
  }
}
