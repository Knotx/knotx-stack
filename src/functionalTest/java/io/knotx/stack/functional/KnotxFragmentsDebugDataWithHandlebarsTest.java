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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class KnotxFragmentsDebugDataWithHandlebarsTest {

  @ClasspathResourcesMockServer
  private WireMockServer delayedServiceServer;

  @AfterEach
  void tearDown() {
    delayedServiceServer.stop();
  }

  @Test
  @DisplayName("Should return page with valid debug data")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/knotx-fragments-debug-data/mocks.conf",
      "scenarios/knotx-fragments-debug-data/tasks.conf",
      "scenarios/knotx-fragments-debug-data/pebble.conf"})
  void requestPage(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer delayedServicePort, @RandomPort Integer globalServerPort) {
    delayedServiceServer = new WireMockServer(delayedServicePort);
    delayedServiceServer.stubFor(get(urlEqualTo("/mock/scenario/delayed")).willReturn(
        aResponse()
            .withStatus(200)
            .withFixedDelay(200)));
    delayedServiceServer.start();

    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(testContext, vertx,
        "/content/payments.html?debug=true", resp -> {
          assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
          String response = resp.bodyAsString();
          assertNotNull(response);

          String scriptRegexp = "<script data-knotx-id=\"?.*?\" type=\"application/json\">(?<fragmentEventJson>.*?)</script>";
          Pattern scriptPattern = Pattern.compile(scriptRegexp, Pattern.DOTALL);
          Matcher matcher = scriptPattern.matcher(response);

          if (matcher.find()) { //first fragment
            String result = matcher.group();
            result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
            JsonObject nodeLog = new JsonObject(result);
            assertNotNull(nodeLog);
            assertEquals(2, nodeLog.getJsonObject("log").getJsonArray("operations").size());
          }

          if (matcher.find()) { //second fragment
            String result = matcher.group();
            result = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
            JsonObject nodeLog = new JsonObject(result);
            assertNotNull(nodeLog);
            assertEquals(8, nodeLog.getJsonObject("log").getJsonArray("operations").size());
          }

          //only 2 fragments, matcher should fail looking for next entries
          assertFalse(matcher.find());
        });
  }
}
