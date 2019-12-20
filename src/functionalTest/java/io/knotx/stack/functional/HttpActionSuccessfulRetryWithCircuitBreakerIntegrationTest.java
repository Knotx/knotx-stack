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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.util.FileReader;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class HttpActionSuccessfulRetryWithCircuitBreakerIntegrationTest {

  private static final String SCENARIO = "HttpAction with retry";
  private static final String RETRY = "RETRY";
  private static final String OFFERS_FILE_PATH = "service/mock/specialOffers.json";

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  private WireMockServer scenarioMockService;

  @Test
  @DisplayName("HttpAction successful retry with circuit breaker integration test")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/http-action-successful-retry-with-circuit-breaker/mocks.conf",
      "scenarios/http-action-successful-retry-with-circuit-breaker/tasks.conf"})
  public void requestPage(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer delayedServicePort, @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    scenarioMockService = new WireMockServer(delayedServicePort);

    scenarioMockService.stubFor(get(urlEqualTo("/service/mock/scenario")).inScenario(SCENARIO)
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(500)
            .withHeader("Content-Type", "application/json")
            .withFixedDelay(300))
        .willSetStateTo(RETRY));

    scenarioMockService.stubFor(get(urlEqualTo("/service/mock/scenario")).inScenario(SCENARIO)
        .whenScenarioStateIs(RETRY)
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(FileReader.readTextSafe(OFFERS_FILE_PATH))));

    scenarioMockService.start();

    serverTester.testGet(testContext, vertx, "/api/http-with-retry", resp -> {
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
    });
  }
}
