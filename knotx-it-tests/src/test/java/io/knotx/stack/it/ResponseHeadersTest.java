/*
 * Copyright (C) 2018 Knot.x Project
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.knotx.junit5.wiremock.KnotxWiremockExtension.stubForServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class ResponseHeadersTest {

  private MultiMap expectedHeaders = MultiMap.caseInsensitiveMultiMap();

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  @BeforeEach
  void before() {
    stubForServer(mockService,
        get(urlMatching("/service/mock/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Cache-control", "no-cache, no-store, must-revalidate")
                    .withHeader("Content-Type", "application/json; charset=UTF-8")
                    .withHeader("X-Server", "Knot.x")
            ));

    stubForServer(mockRepository,
        get(urlMatching("/content/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Cache-control", "no-cache, no-store, must-revalidate")
                    .withHeader("Content-Type", "text/html; charset=UTF-8")
                    .withHeader("X-Server", "Knot.x-Custom-Header")
            ));

    expectedHeaders.clear();
    expectedHeaders.add("Content-Type", "text/html; charset=UTF-8");
    expectedHeaders.add("X-Server", "Knot.x-Custom-Header");
  }

  @Test
  @DisplayName("Expect allowed headers in Server response.")
  @KnotxApplyConfiguration({"conf/application.conf", "scenarios/response-headers/mocks.conf"})
  void whenRequestingRemoteRepository_expectOnlyAllowedResponseHeaders(VertxTestContext context,
      Vertx vertx, @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(context, vertx, "/content/fullPage.html", resp -> {
      MultiMap headers = resp.headers();
      expectedHeaders.names().forEach(name -> {
        assertTrue(headers.contains(name), "Header " + name + " is expected to be present.");
        assertEquals(expectedHeaders.get(name), headers.get(name),
            "Wrong value of " + name + " header.");
      });
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode(), "Wrong status code received.");
      assertTrue(headers.contains("content-length"),
          "content-length header is expected to be present.");
    });
  }

}
