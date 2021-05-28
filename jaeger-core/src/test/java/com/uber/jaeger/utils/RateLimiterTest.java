/*
 * Copyright (c) 2016, Uber Technologies, Inc
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

package com.uber.jaeger.utils;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class RateLimiterTest {
  RateLimiter limiter;

  private static class MockClock implements Clock {

    long timeNanos;

    @Override
    public long currentTimeMicros() {
      return 0;
    }

    @Override
    public long currentNanoTicks() {
      return timeNanos;
    }

    @Override
    public boolean isMicrosAccurate() {
      return false;
    }
  }

  @Test
  public void testRateLimiterWholeNumber() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(2.0, 2.0, clock, 2.0);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertTrue(limiter.checkCredit(1.0));
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertFalse(limiter.checkCredit(1.0));

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
  }

  @Test
  public void testRateLimiterLessThanOne() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.5, 0.5, clock, 0.5);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertTrue(limiter.checkCredit(0.25));
    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    // move time 250ms forward, not enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(250);
    clock.timeNanos = currentTime;
    assertFalse(limiter.checkCredit(0.25));

    // move time 500ms forward, now enough credits to pay for 1.0 item
    currentTime += TimeUnit.MILLISECONDS.toNanos(500);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));

    // move time 5s forward, enough to accumulate credits for 10 messages, but it should still be capped at 2
    currentTime += TimeUnit.MILLISECONDS.toNanos(5000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(0.25));
    assertTrue(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
    assertFalse(limiter.checkCredit(0.25));
  }

  @Test
  public void testRateLimiterMaxBalance() {
    MockClock clock = new MockClock();
    RateLimiter limiter = new RateLimiter(0.1, 1.0, clock, 1.0);

    long currentTime = TimeUnit.MICROSECONDS.toNanos(100);
    clock.timeNanos = currentTime;
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));

    // move time 20s forward, enough to accumulate credits for 2 messages, but it should still be capped at 1
    currentTime += TimeUnit.MILLISECONDS.toNanos(20000);
    clock.timeNanos = currentTime;

    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
  }

  @Test
  public void testRateLimiterUpdate() {
    RateLimiter limiter = new RateLimiter(3.0, 3.0, new MockClock(), 3.0);
  
    // After this call, there should be 2 credits left.
    assertTrue(limiter.checkCredit(1.0));
  
    // Update to a max balance of 1 should only leave 2/3 credits, not enough for a message.
    limiter.update(1.0, 1.0);
    assertFalse(limiter.checkCredit(1.0));
  
    // Revert back to max balance of 3, there should be 2 credits available.
    limiter.update(3.0, 3.0);
    assertTrue(limiter.checkCredit(1.0));
    assertTrue(limiter.checkCredit(1.0));
    assertFalse(limiter.checkCredit(1.0));
  }
}

