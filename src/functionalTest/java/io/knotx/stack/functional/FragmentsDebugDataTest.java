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
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.knotx.stack.functional.utils.TestUtils.OK200;
import static io.knotx.stack.functional.utils.TestUtils.bodyNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.knotx.stack.KnotxServerTester;
import io.knotx.stack.functional.debug.DebugUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class FragmentsDebugDataTest {

  private static final String REQUESTED_PATH_TEMPLATING = "/content/payments.html?debug=true";
  private static final String REQUESTED_PATH_WEB_API = "/api/consumer?debug=true";

  private static final String SUCCESS = "SUCCESS";
  private static final String SNIPPET = "snippet";
  private static final String TYPE = "type";
  private static final String STATUS = "status";
  private static final String OPERATION = "operation";
  private static final String RESPONSE = "response";
  private static final String HTTP = "http";
  private static final String _SUCCESS = "_success";
  private static final String LABEL = "label";
  private static final String UNPROCESSED = "UNPROCESSED";
  private static final String MISSING = "MISSING";

  @ClasspathResourcesMockServer
  private WireMockServer brokenServiceMock;

  @ClasspathResourcesMockServer
  private WireMockServer delayedServiceMock;

  private KnotxServerTester serverTester;

  @AfterEach
  void tearDown() {
    brokenServiceMock.stop();
    delayedServiceMock.stop();
  }

  @Test
  @DisplayName("Expect HTML markup with many fragments containing debug data.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/fragments-debug-data/debugHtml.conf",
      "scenarios/fragments-debug-data/mocks.conf",
      "scenarios/fragments-debug-data/tasks.conf",
      "scenarios/fragments-debug-data/pebble.conf"})
  void requestPage(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer brokenServicePort, @RandomPort Integer delayedServicePort,
      @RandomPort Integer globalServerPort) {
    // given
    givenBrokenServiceServer(brokenServicePort);
    givenDelayedServiceServer(delayedServicePort);
    givenServerTester(globalServerPort);

    // when & then
    knotxShouldProvideDebugData(testContext, vertx);
  }

  @Test
  @DisplayName("Expect JSON with single fragment debug data")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/api/routing.conf",
      "common/api/fragments.conf",
      "scenarios/fragments-debug-data/debugJson.conf",
      "scenarios/fragments-debug-data/mocks.conf",
      "scenarios/fragments-debug-data/tasks.conf"})
  void requestWebApi(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer brokenServicePort, @RandomPort Integer delayedServicePort,
      @RandomPort Integer globalServerPort) {
    // given
    givenBrokenServiceServer(brokenServicePort);
    givenDelayedServiceServer(delayedServicePort);
    givenServerTester(globalServerPort);

    // when & then
    knotxShouldAppendConsumerDataToFragmentBody(testContext, vertx);
  }

  private void givenServerTester(Integer globalServerPort) {
    serverTester = KnotxServerTester.defaultInstance(globalServerPort);
  }

  private void givenBrokenServiceServer(Integer port) {
    brokenServiceMock = new WireMockServer(port);
    brokenServiceMock.stubFor(
        get(urlEqualTo("/mock/scenario/broken"))
            .willReturn(aResponse().withStatus(500)));
    brokenServiceMock.stubFor(
        post(urlEqualTo("/mock/scenario/analytics"))
            .withRequestBody(equalToJson("{\"user-data-id\":\"3214232\"}"))
            .willReturn(aResponse().withStatus(500)));
    brokenServiceMock.start();
  }

  private void givenDelayedServiceServer(Integer port) {
    delayedServiceMock = new WireMockServer(port);
    delayedServiceMock.stubFor(
        get(urlEqualTo("/mock/scenario/delayed"))
            .willReturn(aResponse().withStatus(200).withFixedDelay(200)));
    delayedServiceMock.start();
  }

  private void knotxShouldProvideDebugData(VertxTestContext testContext, Vertx vertx) {
    serverTester.testGet(testContext, vertx,
        REQUESTED_PATH_TEMPLATING, response -> {
          responseShouldBeValid(response);
          debugDataForTwoSnippetsShouldBeValid(response.bodyAsString());
        });
  }

  private void knotxShouldAppendConsumerDataToFragmentBody(VertxTestContext testContext,
      Vertx vertx) {
    serverTester.testGet(testContext, vertx, REQUESTED_PATH_WEB_API,
        response -> {
          responseShouldBeValid(response);

          JsonObject responseData = new JsonObject(response.bodyAsString());

          knotxFragmentResponseDataShouldContainBodyAndKnotxFragmentEntries(
              new JsonObject(response.bodyAsString()));
          knotxFragmentShouldContainExecutionLogEntries(
              responseData.getJsonObject("_knotx_fragment"));

        });
  }

  private void knotxFragmentResponseDataShouldContainBodyAndKnotxFragmentEntries(
      JsonObject responseData) {
    assertEquals(8, responseData.size());
    assertTrue(responseData.containsKey("_knotx_fragment"));
    assertTrue(responseData.containsKey("fetch-user-info"));
    assertTrue(responseData.containsKey("fetch-payment-providers"));
    assertTrue(responseData.containsKey("fetch-offers"));
    assertTrue(responseData.containsKey("fetch-offers-fallback"));
    assertTrue(responseData.containsKey("notify-analytics"));
    assertTrue(responseData.containsKey("notify-analytics-fallback"));
    assertTrue(responseData.containsKey("fetch-delivery"));
  }

  private void knotxFragmentShouldContainExecutionLogEntries(JsonObject knotxFragment) {
    assertEquals("SUCCESS", knotxFragment.getString("status"));
    assertNotEquals(0, knotxFragment.getLong("finishTime"));
    assertNotEquals(0, knotxFragment.getLong("startTime"));
    assertEquals(5, knotxFragment.getJsonObject("fragment").size());
    assertEquals(10, knotxFragment.getJsonObject("graph").size());
  }

  private void responseShouldBeValid(HttpResponse<Buffer> response) {
    OK200(response);
    bodyNotEmpty(response);
  }

  private void debugDataForTwoSnippetsShouldBeValid(String responseBody) {
    LinkedHashMap<String, String> logs = DebugUtils.findLogsInHtml(responseBody);

    assertEquals(2, logs.size());

    // Traversing in insertion (=match) ordering
    Iterator<Entry<String, String>> elements = logs.entrySet().iterator();

    userTaskLog(elements.next().getValue());
    paymentsTaskLog(elements.next().getValue());
  }

  private void userTaskLog(String logAsString) {
    JsonObject log = new JsonObject(logAsString);

    shouldHaveTopLevelMetadata(log);
    shouldHaveFragmentLevelMetadata(log.getJsonObject("fragment"));
    shouldDescribeUserTask(log.getJsonObject("graph"));
  }

  private void paymentsTaskLog(String logAsString) {
    JsonObject log = new JsonObject(logAsString);

    shouldHaveTopLevelMetadata(log);
    shouldHaveFragmentLevelMetadata(log.getJsonObject("fragment"));
    shouldDescribePaymentsTask(log.getJsonObject("graph"));
  }

  private void shouldHaveTopLevelMetadata(JsonObject fragmentExecutionLog) {
    assertNotNull(fragmentExecutionLog);
    assertEquals(SUCCESS, fragmentExecutionLog.getString(STATUS));
    assertNotNull(fragmentExecutionLog.getLong("startTime"));
    assertNotNull(fragmentExecutionLog.getLong("finishTime"));
  }

  private void shouldHaveFragmentLevelMetadata(JsonObject fragment) {
    assertNotNull(fragment);
    assertFalse(StringUtils.isBlank(fragment.getString("id")));
    assertEquals(SNIPPET, fragment.getString(TYPE));
  }

  private void shouldDescribeUserTask(JsonObject graph) {
    assertFalse(graph.isEmpty());
    shouldDescribeSingleNode(graph, "fetch-user-info");
    shouldDescribeAction(graph.getJsonObject(OPERATION), HTTP);
    shouldContainSuccessResponse(graph.getJsonObject(RESPONSE));

    shouldDescribeTemplateEngineNode(graph.getJsonObject("on").getJsonObject(_SUCCESS), "te-hbs");
  }

  private void shouldDescribePaymentsTask(JsonObject graph) {
    assertFalse(graph.isEmpty());
    shouldDescribeSingleNode(graph, "fetch-user-info");
    shouldDescribeAction(graph.getJsonObject(OPERATION), HTTP);
    shouldContainSuccessResponse(graph.getJsonObject(RESPONSE));

    shouldDescribeSuccessTransitionAfterFirstLevelNode(graph.getJsonObject("on").getJsonObject(
        _SUCCESS));
  }

  private void shouldDescribeSuccessTransitionAfterFirstLevelNode(JsonObject node) {
    shouldDescribeCompositeNode(node);
    shouldContainSuccessResponse(node.getJsonObject(RESPONSE));
    shouldContainNestedNodes(node.getJsonArray("subtasks"));

    shouldDescribeTemplateEngineNode(node.getJsonObject("on").getJsonObject(_SUCCESS), "te-pebble");
  }

  private void shouldContainNestedNodes(JsonArray subtasks) {
    assertEquals(4, subtasks.size());
    for (int i = 0; i < 4; i++) {
      JsonObject subtask = subtasks.getJsonObject(i);
      shouldDescribeSingleNode(subtask, subtask.getString(LABEL), subtask.getString(STATUS));
      // execution verification for subtasks skipped
    }
  }

  private void shouldContainSuccessResponse(JsonObject response) {
    assertFalse(response.isEmpty());
    assertEquals(_SUCCESS, response.getString("transition"));
    assertNotNull(response.getJsonObject("log"));
  }

  private void shouldDescribeSingleNode(JsonObject node, String alias) {
    shouldDescribeSingleNode(node, alias, SUCCESS);
  }

  private void shouldDescribeSingleNode(JsonObject node, String alias, String status) {
    assertNotNull(node, "Node not found for [" + alias + "]");
    assertFalse(StringUtils.isBlank(node.getString("id")));
    assertEquals("SINGLE", node.getString(TYPE));
    assertEquals(alias, node.getString(LABEL));
    assertEquals(status, node.getString(STATUS));

    Long started = node.getLong("started");
    Long finished = node.getLong("finished");

    if (MISSING.equals(status) || UNPROCESSED.equals(status)) {
      assertEquals(0, started);
      assertEquals(0, finished);
    } else {
      assertTrue(started > 0);
      assertTrue(finished > started);
    }
  }

  private void shouldDescribeCompositeNode(JsonObject node) {
    assertFalse(StringUtils.isBlank(node.getString("id")));
    assertEquals("COMPOSITE", node.getString(TYPE));
    assertEquals("composite", node.getString(LABEL));
    assertEquals(SUCCESS, node.getString(STATUS));

    Long started = node.getLong("started");
    Long finished = node.getLong("finished");

    assertTrue(started > 0);
    assertTrue(finished > started);
  }

  private void shouldDescribeAction(JsonObject operation, String actionFactory) {
    assertEquals("action", operation.getString("factory"));
    assertEquals(actionFactory, operation.getJsonObject("data").getString("actionFactory"));
  }

  private void shouldDescribeTemplateEngineNode(JsonObject node, String alias) {
    shouldDescribeSingleNode(node, alias);
    shouldDescribeAction(node.getJsonObject(OPERATION), "knot");
    assertEquals(_SUCCESS, node.getJsonObject(RESPONSE).getString("transition"));
  }
}
