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

import io.jaegertracing.spi.MetricsFactory;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is designed to provide {@link Tracer} or {@link Tracer.Builder} when Jaeger client
 * configuration is provided in environmental or property variables. It also simplifies creation of
 * the client from configuration files.
 *
 * @deprecated use package {@code io.jaegertracing} instead. See
 *     https://github.com/jaegertracing/legacy-client-java/issues/13
 */
@Deprecated
@Slf4j
public class Configuration extends io.jaegertracing.Configuration {
  public Configuration(String serviceName) {
    super(serviceName);
  }

  @Override
  public synchronized Tracer getTracer() {
    return (Tracer) super.getTracer();
  }

  @Override
  public Configuration withMetricsFactory(MetricsFactory metricsFactory) {
    super.withMetricsFactory(metricsFactory);
    return this;
  }

  @Override
  public Configuration withServiceName(String serviceName) {
    super.withServiceName(serviceName);
    return this;
  }

  @Override
  public Configuration withReporter(ReporterConfiguration reporterConfig) {
    super.withReporter(reporterConfig);
    return this;
  }

  @Override
  public Configuration withSampler(SamplerConfiguration samplerConfig) {
    super.withSampler(samplerConfig);
    return this;
  }

  @Override
  public Configuration withCodec(CodecConfiguration codecConfig) {
    super.withCodec(codecConfig);
    return this;
  }

  @Override
  public Configuration withTracerTags(Map<String, String> tracerTags) {
    super.withTracerTags(tracerTags);
    return this;
  }

  @Override
  public Tracer.Builder getTracerBuilder() {
    return (Tracer.Builder) super.getTracerBuilder();
  }

  @Override
  protected Tracer.Builder createTracerBuilder(String serviceName) {
    return new Tracer.Builder(serviceName);
  }
}
