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

package com.uber.jaeger.httpclient;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

/**
 * Apache http client request interceptor This is designed to be used along with
 * {@link TracingResponseInterceptor} to report tracing information.
 *
 * In most cases you shouldn't be using this directly. Use the appropriate client builder from
 * {@link TracingInterceptors}
 *
 */
@Slf4j
public class SpanCreationRequestInterceptor implements HttpRequestInterceptor {
  private final Tracer tracer;

  public SpanCreationRequestInterceptor(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public void process(HttpRequest httpRequest, HttpContext httpContext)
      throws HttpException, IOException {
    try {
      Span currentActiveSpan = tracer.activeSpan();

      Tracer.SpanBuilder clientSpanBuilder = tracer.buildSpan(getOperationName(httpRequest));
      if (currentActiveSpan != null) {
        clientSpanBuilder.asChildOf(currentActiveSpan);
      }

      Span clientSpan = clientSpanBuilder.start();

      RequestLine requestLine = httpRequest.getRequestLine();

      Tags.SPAN_KIND.set(clientSpan, Tags.SPAN_KIND_CLIENT);
      Tags.HTTP_URL.set(clientSpan, requestLine.getUri());

      if (httpContext instanceof HttpClientContext) {
        HttpHost host = ((HttpClientContext) httpContext).getTargetHost();
        Tags.PEER_HOSTNAME.set(clientSpan, host.getHostName());
        Tags.PEER_PORT.set(clientSpan, host.getPort());
      }

      onSpanStarted(clientSpan, httpRequest, httpContext);

      httpContext.setAttribute(Constants.CURRENT_SPAN_CONTEXT_KEY, clientSpan);
    } catch (Exception e) {
      log.error("Could not start client tracing span.", e);
    }
  }

  /**
   * onSpanStarted will be called right after the span is created.
   * The subclasses can override this method to add additional information
   * to the span, including tags and logs.
   *
   * @param clientSpan - the span that's being created
   * @param httpRequest - the http request for the operation
   * @param httpContext - the context on which the operation is being executed
   */
  protected void onSpanStarted(Span clientSpan, HttpRequest httpRequest, HttpContext httpContext) {
    // no-op, can be overriden by subclasses
  }

  /**
   * Get the Span's operation name. Defaults to the HTTP method.
   *
   * @param httpRequest the request for the http operation being executed
   * @return the operation name
   */
  protected String getOperationName(HttpRequest httpRequest) {
    return httpRequest.getRequestLine().getMethod();
  }
}