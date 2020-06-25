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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class RepositoryConnectorRedirectPassedToEndUserTest {

  private WireMockServer httpRepositoryServer;



  @Test
  @DisplayName("Should return redirect response when Http Repository returns redirect with empty body (AEM author login redirect case)")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/repository-connector-redirect-passed-to-end-user/httpRepoConnectorHandler.conf",
      "scenarios/repository-connector-redirect-passed-to-end-user/mocks.conf"})
  void requestAemAuthorResourceLoginRedirect(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer httpRepositoryPort, @RandomPort Integer globalServerPort) {
    httpRepositoryServer = new WireMockServer(httpRepositoryPort);
    httpRepositoryServer.stubFor(get(urlEqualTo("/admin-panel.html")).willReturn(
        aResponse()
            .withStatus(HttpResponseStatus.FOUND.code())
            .withHeader("location", "/login.html")));
    httpRepositoryServer.start();

    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester
        .withClientOptions(new WebClientOptions().setFollowRedirects(false))
        .testGet(testContext, vertx, "/admin-panel.html",
        resp -> {
          assertEquals(HttpResponseStatus.FOUND.code(), resp.statusCode());
          assertEquals("/login.html", resp.getHeader("location"));
        });
  }

  @Test
  @DisplayName("Should return not-found response when Http Repository returns 404 with empty body (AEM author login redirect case)")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/repository-connector-redirect-passed-to-end-user/httpRepoConnectorHandler.conf",
      "scenarios/repository-connector-redirect-passed-to-end-user/mocks.conf"})
  void requestRepositoryGetNotFound(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer httpRepositoryPort, @RandomPort Integer globalServerPort) {
    httpRepositoryServer = new WireMockServer(httpRepositoryPort);
    httpRepositoryServer.stubFor(get(urlEqualTo("/not-existing.html")).willReturn(
        aResponse()
            .withStatus(HttpResponseStatus.NOT_FOUND.code()))
    );
    httpRepositoryServer.start();

    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(testContext, vertx, "/not-existing.html",
        resp -> {
          assertEquals(HttpResponseStatus.NOT_FOUND.code(), resp.statusCode());
        });
  }

  @AfterEach
  void tearDown() {
    httpRepositoryServer.stop();
  }
}
