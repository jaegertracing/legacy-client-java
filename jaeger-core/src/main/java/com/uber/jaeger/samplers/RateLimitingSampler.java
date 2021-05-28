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

package com.uber.jaeger.samplers;

import com.uber.jaeger.Constants;
import com.uber.jaeger.utils.RateLimiter;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("EqualsHashCode")
@ToString(exclude = "rateLimiter")
public class RateLimitingSampler implements Sampler {
  public static final String TYPE = "ratelimiting";
  private static final double MINUMUM_BALANCE = 1.0;

  private final RateLimiter rateLimiter;
  @Getter
  private double maxTracesPerSecond;
  private final Map<String, Object> tags;

  public RateLimitingSampler(double maxTracesPerSecond) {
    this.maxTracesPerSecond = maxTracesPerSecond;
    this.rateLimiter = new RateLimiter(maxTracesPerSecond, getMaxBalance(maxTracesPerSecond));

    this.tags = new HashMap<String, Object>();
    tags.put(Constants.SAMPLER_TYPE_TAG_KEY, TYPE);
    tags.put(Constants.SAMPLER_PARAM_TAG_KEY, maxTracesPerSecond);
  }

  @Override
  public synchronized SamplingStatus sample(String operation, long id) {
    return SamplingStatus.of(this.rateLimiter.checkCredit(MINUMUM_BALANCE), tags);
  }

  @Override
  public synchronized boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof RateLimitingSampler) {
      return this.maxTracesPerSecond == ((RateLimitingSampler) other).maxTracesPerSecond;
    }
    return false;
  }

  public synchronized boolean update(double maxTracesPerSecond) {
    if (this.maxTracesPerSecond == maxTracesPerSecond) {
      return false;
    }
    this.maxTracesPerSecond = maxTracesPerSecond;
    rateLimiter.update(maxTracesPerSecond, getMaxBalance(maxTracesPerSecond));
    tags.put(Constants.SAMPLER_PARAM_TAG_KEY, maxTracesPerSecond);
    return true;
  }

  private double getMaxBalance(double maxTracesPerSecond) {
    return maxTracesPerSecond < MINUMUM_BALANCE ? MINUMUM_BALANCE : maxTracesPerSecond;
  }

  /**
   * Only implemented to maintain compatibility with sampling interface.
   */
  @Override
  public void close() {}
}
