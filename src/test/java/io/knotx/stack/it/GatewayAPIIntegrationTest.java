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
package io.knotx.stack.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.util.FileReader;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class GatewayAPIIntegrationTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @Test
  @DisplayName("Expect response containing data from HTTP service")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/gateway-api-with-fragments/mocks.conf",
      "scenarios/gateway-api-with-fragments/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx, @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(context, vertx, "/api/author-info",
        resp -> {
          String expectedResponse = FileReader
              .readTextSafe("scenarios/gateway-api-with-fragments/result/author-info.json");
          assertEquals(new JsonObject(expectedResponse), resp.body().toJsonObject());
        });
  }

}
