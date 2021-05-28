/*
 * Copyright (c) 2016-2018, Uber Technologies, Inc
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

package com.uber.jaeger.mocks;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("")
public class MockAgentResource {
  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public String getServiceSamplingStrategy(@QueryParam("service") String serviceName) {

    if (serviceName.equals("clairvoyant")) {
      return "{\"strategyType\":0,\"probabilisticSampling\":{\"samplingRate\":0.001},\"rateLimitingSampling\":null}";
    }

    throw new WebApplicationException();
  }

  @GET
  @Path("baggageRestrictions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getBaggageRestrictions(@QueryParam("service") String serviceName) {
    if (serviceName.equals("clairvoyant")) {
      return "[{\"baggageKey\":\"key\",\"maxValueLength\":\"10\"}]";
    }
    throw new WebApplicationException();
  }

  @GET
  @Path("credits")
  @Produces(MediaType.APPLICATION_JSON)
  public String getCredits(
      @QueryParam("uuid") int clientId,
      @QueryParam("service") String serviceName,
      @QueryParam("operations") List<String> operations) {
    if (serviceName.equals("clairvoyant")) {
      StringBuffer buffer = new StringBuffer("{ \"balances\": [");
      for (int i = 0; i < operations.size(); i++) {
        if (i > 0) {
          buffer.append(", ");
        }
        buffer.append("{ \"operation\": \"" + operations.get(i) + "\", \"balance\": 1 }");
      }
      buffer.append("]}");
      return buffer.toString();
    }
    throw new WebApplicationException();
  }
}
