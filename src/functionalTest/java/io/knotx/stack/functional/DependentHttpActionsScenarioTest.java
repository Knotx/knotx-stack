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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class DependentHttpActionsScenarioTest {

  private static final String TOKEN = "430sgs4hg0wq3tw00)N(E&GN#s03tgso3t3";
  private static final String API_KEY = "SEousaebES73task4!@%Tyiq3tgs0%%8#^w#JSNB";

  private static final JsonObject AUTH_SERVICE_RESPONSE = new JsonObject().put("token", TOKEN);
  private static final JsonObject DATABASE_SERVICE_RESPONSE = new JsonObject()
      .put("status", "success");

  private static final JsonObject SERVICE_B_REQUIRED_BODY = new JsonObject()
      .put("id", "tester")
      .put("authToken", TOKEN)
      .put("operation", "reloadFromPermanentStorage");

  private KnotxServerTester serverTester;

  @Test
  @DisplayName("Expect successful response from database service when called with a token from auth service")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/api/routing.conf",
      "common/api/fragments.conf",
      "scenarios/dependent-http-actions-scenario-test/mocks.conf",
      "scenarios/dependent-http-actions-scenario-test/tasks.conf"})
  void taskWithManyHttpMethods(VertxTestContext context, Vertx vertx,
      @RandomPort Integer authServicePort, @RandomPort Integer databaseServicePort,
      @RandomPort Integer globalServerPort) throws UnsupportedEncodingException {
    // given
    setUpAuthServer(authServicePort);
    setUpDatabaseServer(databaseServicePort);

    setUpServerTester(globalServerPort);

    // when, then
    serverTester
        .testGet(context, vertx, "/api/user?id=tester&key=" + encodedApiKey(),
            this::shouldReturnValidResponse);
  }

  private void setUpAuthServer(int servicePort) {
    WireMockServer authServiceServer = new WireMockServer(servicePort);
    authServiceServer.stubFor(get(urlPathEqualTo("/auth/login"))
        .withQueryParam("id", equalTo("tester"))
        .withQueryParam("apiKey", equalTo(API_KEY))
        .willReturn(aResponse().withStatus(200).withHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
            HttpHeaderValues.APPLICATION_JSON.toString())
            .withBody(AUTH_SERVICE_RESPONSE.toString())));
    authServiceServer.start();
  }

  private void setUpDatabaseServer(int servicePort) {
    WireMockServer databaseServiceServer = new WireMockServer(servicePort);
    databaseServiceServer.stubFor(post(urlEqualTo("/database/manage"))
        .withRequestBody(equalToJson(SERVICE_B_REQUIRED_BODY.toString()))
        .willReturn(aResponse().withStatus(200).withHeader(HttpHeaderNames.CONTENT_TYPE.toString(),
            HttpHeaderValues.APPLICATION_JSON.toString())
            .withBody(DATABASE_SERVICE_RESPONSE.toString())));
    databaseServiceServer.start();
  }

  private void setUpServerTester(Integer globalServerPort) {
    serverTester = KnotxServerTester.defaultInstance(globalServerPort);
  }

  private void shouldReturnValidResponse(HttpResponse<Buffer> response) {
    assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
    JsonObject responseJson = response.bodyAsJsonObject();
    assertNotNull(responseJson);

    JsonObject databaseActionData = responseJson.getJsonObject("reload-database");
    JsonObject databaseServiceResponse = databaseActionData.getJsonObject("_result");

    assertEquals(DATABASE_SERVICE_RESPONSE, databaseServiceResponse);
  }

  private String encodedApiKey() throws UnsupportedEncodingException {
    return URLEncoder.encode(API_KEY, StandardCharsets.UTF_8.name());
  }

}
