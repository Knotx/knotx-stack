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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class HttpServiceTimesOutAndRetriesScenarioTest {

  private WireMockServer scenarioMockService;

  @Test
  @DisplayName("Expect offers from second service invocation (retry) following the first attempt timeout.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/http-service-times-out-and-cb-retries/mocks.conf",
      "scenarios/http-service-times-out-and-cb-retries/tasks.conf"})
  void requestApi(VertxTestContext ctx, Vertx vertx,
      @RandomPort Integer scenarioServicePort, @RandomPort Integer globalServerPort) {
    scenarioMockService = WireMockScenarios
        .firstOffersServiceInvocationWithDelay(scenarioServicePort);
    scenarioMockService.start();

    KnotxServerTester.defaultInstance(globalServerPort).testGet(ctx, vertx, "/api/user",
        resp -> {
          assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
          JsonObject response = resp.bodyAsJsonObject();
          assertNotNull(response);
          Assertions.assertEquals(5,
              response.getJsonObject("fetch-offers").getJsonArray("_result").size());
        });
  }

  @AfterEach
  void tearDown() {
    scenarioMockService.stop();
  }
}
