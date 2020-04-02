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
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(KnotxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FailedKnotxFragmentScenarioTest {

  @ClasspathResourcesMockServer
  private WireMockServer server;

  private KnotxServerTester serverTester;

  @AfterEach
  void tearDown() {
    server.stop();
  }

  @Test
  @DisplayName("Fragments handler fails for a failed fragment")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/failed-knotx-fragment/mocks.conf",
      "scenarios/failed-knotx-fragment/tasks.conf",
      "scenarios/failed-knotx-fragment/pebble.conf" })
  void requestFailedData(VertxTestContext testContext, Vertx vertx, @RandomPort Integer delayedServicePort, @RandomPort Integer globalServerPort) {
    server = new WireMockServer(delayedServicePort);
    server.stubFor(get(urlEqualTo("/mock/broken/500.json")).willReturn(aResponse().withStatus(500)));
    server.start();

    serverTester = KnotxServerTester.defaultInstance(globalServerPort);

    serverTester.testGet(testContext, vertx, "/content/failedFragment.html?debug=true", response -> {
      assertEquals(500, response.statusCode());
    });
  }

  @Test
  @DisplayName("Fragments handler succeeds for a failed fragment when a param is provided")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/failed-knotx-fragment/mocks.conf",
      "scenarios/failed-knotx-fragment/tasks.conf",
      "scenarios/failed-knotx-fragment/pebble.conf" })
  void requestFailedDataWithHeader(VertxTestContext testContext, Vertx vertx, @RandomPort Integer delayedServicePort, @RandomPort Integer globalServerPort) {
    server = new WireMockServer(delayedServicePort);
    server.stubFor(get(urlEqualTo("/mock/broken/500.json")).willReturn(aResponse().withStatus(500)));
    server.start();

    serverTester = KnotxServerTester.defaultInstance(globalServerPort);

    serverTester.testGet(testContext, vertx, "/content/failedFragment.html?debug=true&allowInvalidFragments=true", response -> {
      assertEquals(200, response.statusCode());
    });
  }

}
