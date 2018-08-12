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

package com.uber.jaeger;

import io.jaegertracing.internal.JaegerObjectFactory;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.PropagationRegistry;
import io.jaegertracing.internal.clock.Clock;
import io.jaegertracing.internal.metrics.Metrics;
import io.jaegertracing.spi.BaggageRestrictionManager;
import io.jaegertracing.spi.Extractor;
import io.jaegertracing.spi.Injector;
import io.jaegertracing.spi.MetricsFactory;
import io.jaegertracing.spi.Reporter;
import io.jaegertracing.spi.Sampler;
import io.opentracing.ScopeManager;
import io.opentracing.propagation.Format;

import java.util.Map;

/**
 * @deprecated use package {@code io.jaegertracing} instead. See https://github.com/jaegertracing/legacy-client-java/issues/13
 */
@Deprecated
public class Tracer extends JaegerTracer {
  public static class Builder extends JaegerTracer.Builder {
    @Override
    public Builder withReporter(Reporter reporter) {
      super.withReporter(reporter);
      return this;
    }

    @Override
    public Builder withSampler(Sampler sampler) {
      super.withSampler(sampler);
      return this;
    }

    @Override
    public <T> Builder registerInjector(Format<T> format, Injector<T> injector) {
      super.registerInjector(format, injector);
      return this;
    }

    @Override
    public <T> Builder registerExtractor(Format<T> format, Extractor<T> extractor) {
      super.registerExtractor(format, extractor);
      return this;
    }

    @Override
    public Builder withMetricsFactory(MetricsFactory metricsFactory) {
      super.withMetricsFactory(metricsFactory);
      return this;
    }

    @Override
    public Builder withScopeManager(ScopeManager scopeManager) {
      super.withScopeManager(scopeManager);
      return this;
    }

    @Override
    public Builder withClock(Clock clock) {
      super.withClock(clock);
      return this;
    }

    @Override
    public Builder withZipkinSharedRpcSpan() {
      super.withZipkinSharedRpcSpan();
      return this;
    }

    @Override
    public Builder withExpandExceptionLogs() {
      super.withExpandExceptionLogs();
      return this;
    }

    @Override
    public Builder withMetrics(Metrics metrics) {
      super.withMetrics(metrics);
      return this;
    }

    @Override
    public Builder withTag(String key, String value) {
      super.withTag(key, value);
      return this;
    }

    @Override
    public Builder withTag(String key, boolean value) {
      super.withTag(key, value);
      return this;
    }

    @Override
    public Builder withTag(String key, Number value) {
      super.withTag(key, value);
      return this;
    }

    @Override
    public Builder withTags(Map<String, String> tags) {
      super.withTags(tags);
      return this;
    }

    @Override
    public Builder withBaggageRestrictionManager(BaggageRestrictionManager baggageRestrictionManager) {
      super.withBaggageRestrictionManager(baggageRestrictionManager);
      return this;
    }

    public Builder(String serviceName) {
      super(serviceName, new ObjectFactory());
    }

    @Override
    public Tracer build() {
      return (Tracer) super.build();
    }

    @Override
    protected Tracer createTracer() {
      return new Tracer(this);
    }
  }

  public class SpanBuilder extends JaegerTracer.SpanBuilder {
    protected SpanBuilder(String operationName) {
      super(operationName);
    }

    @Override
    public Span start() {
      return (Span) super.start();
    }
  }

  private Tracer(Tracer.Builder builder) {
    super(builder);
  }
}
