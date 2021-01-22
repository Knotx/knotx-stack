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
import static io.knotx.stack.functional.utils.TestUtils.OK200;
import static io.knotx.stack.functional.utils.TestUtils.bodyNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class AllActionsDebugDataTest {

  private static final String MOCK_PATH = "/database/manage";
  private static final String SCENARIO_PATH = "/content/allActions.html";

  private static final String SERVICE_RESPONSE = new JsonObject().put("status", "OK").toString();

  @ClasspathResourcesMockServer
  private WireMockServer serviceServer;

  private KnotxServerTester serverTester;

  @AfterEach
  void tearDown() {
    serviceServer.stop();
  }

  @Test
  @DisplayName("Expect HTML markup with many fragments containing debug data.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/all-actions-debug-data/debugHtml.conf",
      "scenarios/all-actions-debug-data/mocks.conf",
      "scenarios/all-actions-debug-data/tasks.conf",
      "scenarios/all-actions-debug-data/pebble.conf"})
  void requestPage(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer servicePort,
      @RandomPort Integer globalServerPort) {
    // given
    givenServiceServer(servicePort);
    givenServerTester(globalServerPort);

    // when & then
    knotxShouldProvideDebugData(testContext, vertx);
  }

  private void knotxShouldProvideDebugData(VertxTestContext testContext, Vertx vertx) {
    serverTester.testGet(testContext, vertx, SCENARIO_PATH, this::responseShouldBeValid);
  }

  private void responseShouldBeValid(HttpResponse<Buffer> response) {
    OK200(response);
    bodyNotEmpty(response);
  }

  private void givenServerTester(Integer globalServerPort) {
    serverTester = KnotxServerTester.defaultInstance(globalServerPort);
  }

  private void givenServiceServer(int servicePort) {
    serviceServer = new WireMockServer(servicePort);
    serviceServer.stubFor(get(urlEqualTo(MOCK_PATH))
        .willReturn(aResponse().withStatus(200).withHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
            HttpHeaderValues.APPLICATION_JSON.toString())
            .withBody(SERVICE_RESPONSE)));
    serviceServer.start();
  }

}
