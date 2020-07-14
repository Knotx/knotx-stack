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
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class LongRunningHttpServiceWithCircuitBreakerIntegrationTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  private WireMockServer delayedServiceServer;

  @AfterEach
  void tearDown() {
    delayedServiceServer.stop();
  }

  @Test
  @DisplayName("Expect page containing data from services and fallback data for broken service.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/long-running-http-service-with-circuit-breaker/mocks.conf",
      "scenarios/long-running-http-service-with-circuit-breaker/tasks.conf"})
  void taskWithCircuitBreaker(VertxTestContext context, Vertx vertx,
      @RandomPort Integer delayedServicePort,
      @RandomPort Integer globalServerPort) {
    // given
    delayedServiceServer = new WireMockServer(delayedServicePort);
    delayedServiceServer.stubFor(get(urlEqualTo("/service/mock/delayed")).willReturn(
        aResponse()
            .withStatus(200)
            .withFixedDelay(2000)));
    delayedServiceServer.start();

    // when
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester
        .testGetWithExpectedResponse(context, vertx, "/content/fullPage.html",
            "scenarios/long-running-http-service-with-circuit-breaker/result/fullPage.html");
  }

}
