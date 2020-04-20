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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class CircuitBreakerTimesOutScenarioTest {

  private WireMockServer delayedServiceServer;

  @Test
  @DisplayName("Expect offers fallback while circuit breaker times out offers service.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/api/routing.conf",
      "common/api/fragments.conf",
      "scenarios/circuit-breaker-times-out/mocks.conf",
      "scenarios/circuit-breaker-times-out/tasks.conf"})
  void requestApi(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer delayedServicePort, @RandomPort Integer globalServerPort) {
    delayedServiceServer = new WireMockServer(delayedServicePort);
    delayedServiceServer.stubFor(get(urlEqualTo("/service/mock/delayed")).willReturn(
        aResponse()
            .withStatus(200)
            .withFixedDelay(200)));
    delayedServiceServer.start();

    KnotxServerTester.defaultInstance(globalServerPort)
        .testGet(testContext, vertx, "/api/user",
            resp -> {
              assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
              assertNotNull(resp.body().toJsonObject());
              JsonObject response = resp.body().toJsonObject();
              assertTrue(response.containsKey("fetch-user-info"));
              assertTrue(response.containsKey("fetch-payment-providers"));
              assertEquals("timeout",
                  response.getJsonObject("fetch-offers").getJsonObject("_result")
                      .getString("fallback"));
            });
  }

  @AfterEach
  void tearDown() {
    delayedServiceServer.stop();
  }
}
