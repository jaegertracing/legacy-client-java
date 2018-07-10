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

import static com.uber.jaeger.utils.Http.makeGetRequest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.uber.jaeger.throttler.http.CreditResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.List;

public class HttpThrottlerProxy implements ThrottlerProxy {
  private static final String DEFAULT_HOST_PORT = "localhost:5778";
  private static final Type RESPONSE_TYPE = new TypeToken<CreditResponse>() {}.getType();
  private final Gson gson = new Gson();
  private final String hostPort;

  public HttpThrottlerProxy(String hostPort) {
    this.hostPort = hostPort != null ? hostPort : DEFAULT_HOST_PORT;
  }

  CreditResponse parseJson(String json) throws ThrottlerException {
    try {
      return gson.fromJson(json, RESPONSE_TYPE);
    } catch (JsonSyntaxException e) {
      throw new ThrottlerException("Cannot deserialize json", e);
    }
  }

  @Override
  public CreditResponse getCredits(int clientId, String serviceName, List<String> operations)
      throws ThrottlerException {
    String jsonString;
    try {
      StringBuffer operationsQueryBuffer = new StringBuffer();
      for (String op : operations) {
        operationsQueryBuffer.append("&operations=" + URLEncoder.encode(op, "UTF-8"));
      }
      jsonString =
          makeGetRequest(
              "http://"
                  + hostPort
                  + "/credits?"
                  + "uuid="
                  + URLEncoder.encode(Integer.toString(clientId), "UTF-8")
                  + "&service="
                  + URLEncoder.encode(serviceName, "UTF-8")
                  + operationsQueryBuffer.toString());
    } catch (IOException e) {
      throw new ThrottlerException(
          "http call to get baggage restriction from local agent failed.", e);
    }
    return parseJson(jsonString);
  }
}
