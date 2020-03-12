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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.json.JsonObject;
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
class VariousMethodsForHttpActionIntegrationTest {

  private static final String REQUEST_BODY = "{\"password\": \"pazzword\"}";

  private WireMockServer serviceServer;
  private KnotxServerTester serverTester;

  @AfterEach
  void tearDown() {
    serviceServer.stop();
  }

  @Test
  @DisplayName("Expect various HTTP methods to be supported by HttpAction")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/various-methods-for-http-action/mocks.conf",
      "scenarios/various-methods-for-http-action/tasks.conf"})
  void taskWithManyHttpMethods(VertxTestContext context, Vertx vertx,
      @RandomPort Integer servicePort, @RandomPort Integer globalServerPort) {
    // given
    setUpServiceServer(servicePort);
    setUpServerTester(globalServerPort);

    // when, then
    serverTester
        .testGetRequest(context, vertx, "/content/variousHttpMethods.html",
            "scenarios/various-methods-for-http-action/result/fullPage.html");
  }

  private void setUpServiceServer(int servicePort) {
    serviceServer = new WireMockServer(servicePort);
    // get
    serviceServer.stubFor(get(urlEqualTo("/service/mock/get.json"))
        .willReturn(response200Json(responseBodyFor("GET"))));
    // post
    serviceServer.stubFor(post(urlEqualTo("/service/mock/post.json"))
        .withRequestBody(equalToJson(REQUEST_BODY))
        .willReturn(response200Json(responseBodyFor("POST"))));
    // put
    serviceServer.stubFor(put(urlEqualTo("/service/mock/put.json"))
        .withRequestBody(equalToJson(REQUEST_BODY))
        .willReturn(response200Json(responseBodyFor("PUT"))));
    // patch
    serviceServer.stubFor(patch(urlEqualTo("/service/mock/patch.json"))
        .withRequestBody(equalToJson(REQUEST_BODY))
        .willReturn(response200Json(responseBodyFor("PATCH"))));
    // delete
    serviceServer.stubFor(delete(urlEqualTo("/service/mock/delete.json"))
        .willReturn(response200Json(responseBodyFor("DELETE"))));
    // head
    serviceServer.stubFor(head(urlEqualTo("/service/mock/head.json"))
        .willReturn(response200()));
    serviceServer.start();
  }

  private void setUpServerTester(Integer globalServerPort) {
    serverTester = KnotxServerTester.defaultInstance(globalServerPort);
  }

  private String responseBodyFor(String type) {
    return new JsonObject().put("type", type).toString();
  }

  private ResponseDefinitionBuilder response200Json(String responseBody) {
    return response200().withHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
        HttpHeaderValues.APPLICATION_JSON.toString()).withBody(responseBody);
  }

  private ResponseDefinitionBuilder response200() {
    return aResponse().withStatus(200);
  }
}
