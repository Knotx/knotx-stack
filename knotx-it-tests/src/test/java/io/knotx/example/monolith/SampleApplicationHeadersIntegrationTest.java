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
package io.knotx.example.monolith;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;
import static io.knotx.junit5.wiremock.KnotxWiremockExtension.stubForServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.wiremock.KnotxWiremock;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class SampleApplicationHeadersIntegrationTest {

  private static final int KNOTX_SERVER_PORT = 9092;
  private static final String KNOTX_SERVER_ADDRESS = "localhost";

  private MultiMap expectedHeaders = MultiMap.caseInsensitiveMultiMap();

  @KnotxWiremock
  protected WireMockServer mockService;

  @KnotxWiremock
  protected WireMockServer mockRepository;

  @BeforeEach
  public void before() {
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
                    .withHeader("X-Server", "Knot.x")
            ));

    expectedHeaders.clear();
    expectedHeaders.add("Content-Type", "text/html; charset=UTF-8");
    expectedHeaders.add("X-Server", "Knot.x-Custom-Header");
  }

  @Test
  @KnotxApplyConfiguration("conf/integrationTestsStack.conf")
  public void whenRequestingRemoteRepository_expectOnlyAllowedResponseHeaders(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/remote/fullPage.html");
  }

  private void testGetRequest(VertxTestContext context, Vertx vertx, String url) {
    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpRequest = client
        .get(KNOTX_SERVER_PORT, KNOTX_SERVER_ADDRESS, url).rxSend();

    subscribeToResult_shouldSucceed(context, httpRequest,
        resp -> {
          MultiMap headers = resp.headers();
          expectedHeaders.names().forEach(name -> {
            assertTrue(headers.contains(name), "Header " + name + " is expected to be present.");
            assertEquals(expectedHeaders.get(name), headers.get(name),
                "Wrong value of " + name + " header.");
          });
          assertEquals(HttpResponseStatus.OK.code(), resp.statusCode(), "Wrong status code received.");
          assertTrue(headers.contains("content-length"), "content-length header is expected to be present.");
        }
    );
  }
}
