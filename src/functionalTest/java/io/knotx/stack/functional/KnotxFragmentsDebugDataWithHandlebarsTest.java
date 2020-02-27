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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import java.util.regex.Matcher;
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
class KnotxFragmentsDebugDataWithHandlebarsTest {

  private static final String SCRIPT_REGEXP = "<script data-knotx-debug=\"log\" data-knotx-id=\"?.*?\" type=\"application/json\">(?<logJson>.*?)</script>";
  private static final Pattern SCRIPT_PATTERN = Pattern.compile(SCRIPT_REGEXP, Pattern.DOTALL);
  private static final String REQUESTED_PATH = "/content/payments.html?debug=true";

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

  @ClasspathResourcesMockServer
  private WireMockServer delayedServiceServer;

  private KnotxServerTester serverTester;

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

    givenDelayedServiceServer(delayedServicePort);
    givenServerTester(globalServerPort);

    knotxShouldProvideDebugData(testContext, vertx);
  }

  private void givenServerTester(Integer globalServerPort) {
    serverTester = KnotxServerTester.defaultInstance(globalServerPort);
  }

  private void givenDelayedServiceServer(Integer delayedServicePort) {
    delayedServiceServer = new WireMockServer(delayedServicePort);
    delayedServiceServer.stubFor(get(urlEqualTo("/mock/scenario/delayed")).willReturn(
        aResponse()
            .withStatus(200)
            .withFixedDelay(200)));
    delayedServiceServer.start();
  }

  private void knotxShouldProvideDebugData(VertxTestContext testContext, Vertx vertx) {
    serverTester.testGet(testContext, vertx,
        REQUESTED_PATH, response -> {
          responseShouldBeValid(response);
          debugDataForTwoSnippetsShouldBeValid(response.bodyAsString());
        });
  }

  private void responseShouldBeValid(HttpResponse<Buffer> response) {
    assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
    assertNotNull(response.bodyAsString());
  }

  private void debugDataForTwoSnippetsShouldBeValid(String responseBody) {
    Matcher matcher = SCRIPT_PATTERN.matcher(responseBody);

    firstFragmentShouldHaveExpectedLog(matcher);
    secondFragmentShouldHaveExpectedLog(matcher);
    thereShouldBeNoMoreLogScripts(
        matcher); // only 2 fragments, matcher should fail looking for next entries
  }

  private void firstFragmentShouldHaveExpectedLog(Matcher matcher) {
    assertTrue(matcher.find()); // first fragment with user-task

    JsonObject log = getLog(matcher);

    shouldHaveTopLevelMetadata(log);
    shouldDescribeUserTask(log.getJsonObject("graph"));
  }

  private void secondFragmentShouldHaveExpectedLog(Matcher matcher) {
    assertTrue(matcher.find()); // second fragment with payments-task

    JsonObject log = getLog(matcher);

    shouldHaveTopLevelMetadata(log);
    shouldDescribePaymentsTask(log.getJsonObject("graph"));
  }

  private void thereShouldBeNoMoreLogScripts(Matcher matcher) {
    assertFalse(matcher.find());
  }

  private JsonObject getLog(Matcher matcher) {
    String logAsString = matcher.group("logJson");
    return new JsonObject(logAsString);
  }

  private void shouldHaveTopLevelMetadata(JsonObject fragmentExecutionLog) {
    assertNotNull(fragmentExecutionLog);
    assertFalse(StringUtils.isBlank(fragmentExecutionLog.getString("fragmentId")));
    assertEquals(SNIPPET, fragmentExecutionLog.getString(TYPE));
    assertEquals(SUCCESS, fragmentExecutionLog.getString(STATUS));
    assertNotNull(fragmentExecutionLog.getLong("startTime"));
    assertNotNull(fragmentExecutionLog.getLong("finishTime"));
  }

  private void shouldDescribeUserTask(JsonObject graph) {
    assertFalse(graph.isEmpty());
    shouldDescribeSingleNode(graph, "fetch-user-info");
    shouldDescribeAction(graph.getJsonObject(OPERATION), HTTP);
    shouldContainSuccessResponse(graph.getJsonObject(RESPONSE), 1);

    shouldDescribeTemplateEngineNode(graph.getJsonObject("on").getJsonObject(_SUCCESS), "te-hbs");
  }

  private void shouldDescribePaymentsTask(JsonObject graph) {
    assertFalse(graph.isEmpty());
    shouldDescribeSingleNode(graph, "fetch-user-info");
    shouldDescribeAction(graph.getJsonObject(OPERATION), HTTP);
    shouldContainSuccessResponse(graph.getJsonObject(RESPONSE), 1);

    shouldDescribeSuccessTransitionAfterFirstLevelNode(graph.getJsonObject("on").getJsonObject(
        _SUCCESS));
    shouldDescribeErrorTransitionAfterFirstLevelNode(
        graph.getJsonObject("on").getJsonObject("_error"));
    shouldDescribeCustomTransitionAfterFirstLevelNode(
        graph.getJsonObject("on").getJsonObject("_custom"));
  }

  private void shouldDescribeSuccessTransitionAfterFirstLevelNode(JsonObject node) {
    shouldDescribeCompositeNode(node);
    shouldContainSuccessResponse(node.getJsonObject(RESPONSE), 0);
    shouldContainThreeNestedNodes(node.getJsonArray("subtasks"));

    shouldDescribeTemplateEngineNode(node.getJsonObject("on").getJsonObject(_SUCCESS), "te-pebble");
  }

  private void shouldContainThreeNestedNodes(JsonArray subtasks) {
    assertEquals(3, subtasks.size());
    for (int i = 0; i < 3; i++) {
      JsonObject subtask = subtasks.getJsonObject(i);
      shouldDescribeSingleNode(subtask, subtask.getString(LABEL));
      // execution verification for subtasks skipped
    }
  }

  private void shouldDescribeErrorTransitionAfterFirstLevelNode(JsonObject node) {
    shouldDescribeSingleNode(node, "fetch-user-info-fallback", UNPROCESSED);
    shouldDescribeAction(node.getJsonObject(OPERATION), HTTP);

    JsonObject successTransition = node.getJsonObject("on").getJsonObject(_SUCCESS);
    shouldDescribeSingleNode(successTransition, "fetch-user-info-fallback-success", UNPROCESSED);
    shouldDescribeAction(successTransition.getJsonObject(OPERATION), HTTP);

    JsonObject errorTransition = node.getJsonObject("on").getJsonObject("_error");
    shouldDescribeSingleNode(errorTransition, "fetch-user-info-fallback-error", UNPROCESSED);
    shouldDescribeAction(errorTransition.getJsonObject(OPERATION), HTTP);
  }

  private void shouldDescribeCustomTransitionAfterFirstLevelNode(JsonObject node) {
    shouldDescribeSingleNode(node, "fetch-user-info-custom-fallback", UNPROCESSED);
    shouldDescribeAction(node.getJsonObject(OPERATION), HTTP);
  }

  private void shouldContainSuccessResponse(JsonObject response, int invocations) {
    assertFalse(response.isEmpty());
    assertEquals(_SUCCESS, response.getString("transition"));
    assertEquals(invocations, response.getJsonArray("invocations").size());
  }

  private void shouldDescribeSingleNode(JsonObject node, String alias) {
    shouldDescribeSingleNode(node, alias, SUCCESS);
  }

  private void shouldDescribeSingleNode(JsonObject node, String alias, String status) {
    assertFalse(StringUtils.isBlank(node.getString("id")));
    assertEquals("SINGLE", node.getString(TYPE));
    assertEquals(alias, node.getString(LABEL));
    assertEquals(status, node.getString(STATUS));
  }

  private void shouldDescribeCompositeNode(JsonObject node) {
    assertFalse(StringUtils.isBlank(node.getString("id")));
    assertEquals("COMPOSITE", node.getString(TYPE));
    assertEquals("composite", node.getString(LABEL));
    assertEquals(SUCCESS, node.getString(STATUS));
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
