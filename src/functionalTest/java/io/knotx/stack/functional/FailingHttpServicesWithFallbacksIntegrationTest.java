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
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class FailingHttpServicesWithFallbacksIntegrationTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  private WireMockServer mockBrokenService;

  @AfterEach
  void tearDown() {
    mockBrokenService.stop();
  }

  @Test
  @DisplayName("Expect page containing data from services and fallback data for broken service.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/failing-http-services-with-fallbacks/mocks.conf",
      "scenarios/failing-http-services-with-fallbacks/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx, @RandomPort Integer mockBrokenServicePort,
      @RandomPort Integer globalServerPort) {
    // when
    mockBrokenService = new WireMockServer(mockBrokenServicePort);
    mockBrokenService.stubFor(get(urlMatching("/service/broken/500.json"))
        .willReturn(
            aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        ));
    mockBrokenService.start();

    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester
        .testGetRequest(context, vertx, "/content/fullPage.html",
            "scenarios/failing-http-services-with-fallbacks/result/fullPage.html");
  }
}
