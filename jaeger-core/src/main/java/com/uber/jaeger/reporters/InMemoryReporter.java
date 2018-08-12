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

package com.uber.jaeger.reporters;

import com.uber.jaeger.Span;
import io.jaegertracing.internal.JaegerSpan;

import java.util.ArrayList;
import java.util.List;

public class InMemoryReporter implements Reporter {
  private final io.jaegertracing.internal.reporters.InMemoryReporter reporter =
      new io.jaegertracing.internal.reporters.InMemoryReporter();

  public InMemoryReporter() {}

  @Override
  public void report(JaegerSpan span) {
    reporter.report(span);
  }

  @Override
  public void report(Span span) {
    report((JaegerSpan) span);
  }

  @Override
  public void close() {
    reporter.close();
  }

  public List<Span> getSpans() {
    List<Span> result = new ArrayList<Span>();
    for (JaegerSpan span : reporter.getSpans()) {
      result.add((Span) span);
    }
    return result;
  }
}
