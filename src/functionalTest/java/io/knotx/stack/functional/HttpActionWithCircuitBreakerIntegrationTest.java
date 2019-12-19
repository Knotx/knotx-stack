/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.stack.functional;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(KnotxExtension.class)
public class HttpActionWithCircuitBreakerIntegrationTest {

  private static final String UNAVAILABLE_MESSAGE = "Offers service is currently unavailable, impossible to fetch offers from external data sorce";

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @Test
  @DisplayName("HttpAction with circuit breaker integration test")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/http-action-with-circuit-breaker/mocks.conf",
      "scenarios/http-action-with-circuit-breaker/tasks.conf"})
  void requestPage(VertxTestContext testContext, Vertx vertx, @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(testContext, vertx, "/api/http-action-with-cb", resp -> {
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
      assertNotNull(resp.body().toJsonObject());
      JsonObject response = resp.body().toJsonObject();
      assertEquals(UNAVAILABLE_MESSAGE, response.getJsonObject("get-available-offers-http").getJsonObject("_result")
          .getJsonObject("offers").getString("message"));
    });
  }
}

