/*
 * Copyright (c) 2017, Uber Technologies, Inc
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

package com.uber.jaeger.tracerresolver;

import com.uber.jaeger.Configuration;

import io.opentracing.contrib.tracerresolver.TracerResolver;

/**
 * @deprecated use package {@code io.jaegertracing} instead. See https://github.com/jaegertracing/legacy-client-java/issues/13
 */
@Deprecated
public class JaegerTracerResolver extends TracerResolver {

  @Override
  protected io.opentracing.Tracer resolve() {
    return Configuration.fromEnv().getTracer();
  }

}
