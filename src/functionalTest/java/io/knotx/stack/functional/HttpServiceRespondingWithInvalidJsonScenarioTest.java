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
import static org.junit.Assert.assertTrue;

import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class HttpServiceRespondingWithInvalidJsonScenarioTest {

  private static final String OFFERS_FALLBACK_MESSAGE = "The offers are currently unavailable.";

  @Test
  @DisplayName("Expect offers fallback when endpoint responds with invalid JSON.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/http-service-responding-with-invalid-json/mocks.conf",
      "scenarios/http-service-responding-with-invalid-json/tasks.conf"})
  void requestWebApi(VertxTestContext ctx, Vertx vertx, @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(ctx, vertx, "/api/ecommerce/checkout",
        resp -> {
          assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
          JsonObject responseBody = resp.bodyAsJsonObject();
          assertNotNull(responseBody);
          assertTrue(responseBody.containsKey("fetch-user-info"));
          assertTrue(responseBody.containsKey("fetch-payment-providers"));
          assertEquals(OFFERS_FALLBACK_MESSAGE, responseBody.getJsonObject("fetch-offers")
              .getJsonObject("_result").getJsonObject("offers").getString("message"));
        });
  }
}
