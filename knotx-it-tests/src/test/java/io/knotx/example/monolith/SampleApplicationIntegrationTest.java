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

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.dataobjects.AdapterResponse;
import io.knotx.dataobjects.ClientResponse;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.util.FileReader;
import io.knotx.junit5.wiremock.KnotxWiremock;
import io.knotx.proxy.AdapterProxy;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceBinder;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class SampleApplicationIntegrationTest {

  private static final String REMOTE_REQUEST_URI = "/content/remote/simple.html";
  private static final String REMOTE_REQUEST_URI_WITH_PARAMETER_CONTAINING_SPACE = "/content/remote/simple.html?parameter%20with%20space=value";
  private static final String LOCAL_REQUEST_URI = "/content/local/simple.html";
  private static final String MISSING_SERVICE_CONFIG_REQUEST_URI = "/content/local/missingServiceConfig.html";
  private static final String LOCAL_NO_BODY_REQUEST_URI = "/content/local/noBody.html";
  private static final String LOCAL_MULTIPLE_FORMS_URI = "/content/local/multiple-forms.html";
  private static final int KNOTX_SERVER_PORT = 9092;
  private static final String KNOTX_SERVER_ADDRESS = "localhost";

  @KnotxWiremock(port = 4001)
  protected WireMockServer mockRepository;

  @KnotxWiremock(port = 4002)
  protected WireMockServer mockService;

  @BeforeAll
  void initMocks() {
    stubForServer(mockRepository,
        get(urlMatching("/content/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Cache-control", "no-cache, no-store, must-revalidate")
                    .withHeader("Content-Type", "text/html; charset=UTF-8")
                    .withHeader("X-Server", "Knot.x")
            ));

    //fixme add bouncer from original verticle
    stubForServer(mockService,
        get(urlMatching("/content/.*"))
            .willReturn(
                aResponse()
                    .withHeader("Cache-control", "no-cache, no-store, must-revalidate")
                    .withHeader("Content-Type", "text/html; charset=UTF-8")
                    .withHeader("X-Server", "Knot.x")
            ));
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingLocalSimplePageWithGet_expectLocalSimpleHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, LOCAL_REQUEST_URI, "localSimpleResult.html");
  }

  @Test
  @KnotxApplyConfiguration({"knotx-test-app.conf", "knotx-test-handlebars-custom-symbol.conf"})
  public void whenRequestingLocalSimplePageWithGetCustomSymbol_expectLocalSimpleHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/customSymbol.html",
        "localSimpleResultAngular.html");
  }

  @Test
  @KnotxApplyConfiguration({"knotx-test-app.conf", "knotx-test-handlebars-custom-symbol.conf"})
  public void whenRequestingLocalSimplePageWithGetCustomAndDefaultSymbol_expectLocalSimpleHtmlWithDefault(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/customAndDefaultSymbol.html",
        "localSimpleResultCustomAndDefault.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingLocalPageWhereInServiceIsMissingResponseBody_expectNoBodyHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, LOCAL_NO_BODY_REQUEST_URI, "noBody.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingPageWithMissingServiceWithoutConfiguration_expectServerError(
      VertxTestContext context, Vertx vertx) {
    testGetServerError(context, vertx, MISSING_SERVICE_CONFIG_REQUEST_URI);
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingRemoteSimplePageWithGet_expectRemoteSimpleHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, REMOTE_REQUEST_URI, "remoteSimpleResult.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingRemoteSimplePageWithGetAndRequestParameterNameContainsSpace_expectRemoteSimpleHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, REMOTE_REQUEST_URI_WITH_PARAMETER_CONTAINING_SPACE,
        "remoteSimpleResult.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingLocalMultipleFormsPageWithGet_expectMutlipleFormsWithGetResultHtml(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, LOCAL_MULTIPLE_FORMS_URI, "multipleFormWithGetResult.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingWithPostMethodFirstForm_expectFirstFormPresentingFormActionResult(
      VertxTestContext context, Vertx vertx) {
    mockActionAdapter(vertx, getFirstTestFormData(), null);
    testPostRequest(context, vertx, LOCAL_MULTIPLE_FORMS_URI, getFirstTestFormData().getMap(),
        "multipleFormWithPostResult.html");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.conf")
  public void whenRequestingWithPostFirstFormTwiceWithDifferentData_expectDifferentResultOfFirstFormForEachRequest(
      VertxTestContext context, Vertx vertx) {
    mockActionAdapter(vertx, getFirstTestFormData(), getSecondTestFormData());
    testPostRequest(context, vertx, LOCAL_MULTIPLE_FORMS_URI, getFirstTestFormData().getMap(),
        "multipleFormWithPostResult.html");
    testPostRequest(context, vertx, LOCAL_MULTIPLE_FORMS_URI, getSecondTestFormData().getMap(),
        "multipleFormWithPostResult2.html");
  }

  private void testPostRequest(VertxTestContext context, Vertx vertx, String url,
      Map<String, Object> formData,
      String expectedResponseFile) {

    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .post(KNOTX_SERVER_PORT, KNOTX_SERVER_ADDRESS, url)
        .rxSendForm(getMultiMap(formData));

    subscribeToResult_shouldSucceed(context, httpResponseSingle, resp -> {
      assertEquals(Jsoup.parse(FileReader.readTextSafe(expectedResponseFile)).body().html(),
          Jsoup.parse(resp.body().toString()).body().html());
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
    });
  }

  private MultiMap getMultiMap(Map<String, Object> formData) {
    MultiMap formMap = MultiMap.caseInsensitiveMultiMap();
    formData.forEach((key, value) -> formMap.add(key, (String) value));
    return formMap;
  }

  private void testGetRequest(VertxTestContext context, Vertx vertx, String url,
      String expectedResponseFile) {
    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .get(KNOTX_SERVER_PORT, KNOTX_SERVER_ADDRESS, url).rxSend();

    subscribeToResult_shouldSucceed(context, httpResponseSingle, resp -> {
      assertEquals(Jsoup.parse(FileReader.readTextSafe(expectedResponseFile)).body().html().trim(),
          Jsoup.parse(resp.body().toString()).body().html().trim());
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
      client.close();
    });
  }

  private void testGetServerError(VertxTestContext context, Vertx vertx, String url) {
    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .get(KNOTX_SERVER_PORT, KNOTX_SERVER_ADDRESS, url)
        .rxSend();

    subscribeToResult_shouldSucceed(context, httpResponseSingle, resp -> {
      assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), resp.statusCode());
      client.close();
    });
  }

  private JsonObject getFirstTestFormData() {
    return new JsonObject()
        .put("name", "test")
        .put("email", "email-1@example.com")
        .put("_frmId", "competition");
  }

  private JsonObject getSecondTestFormData() {
    return new JsonObject()
        .put("name", "test")
        .put("email", "email-2@example.com")
        .put("_frmId", "newsletter");
  }

  private void mockActionAdapter(Vertx vertx, JsonObject competitionData,
      JsonObject newsletterData) {
    ClientResponse clientResponse = new ClientResponse().setStatusCode(404);
    AdapterResponse resp = new AdapterResponse().setResponse(clientResponse);

    new ServiceBinder(vertx.getDelegate())
        .setAddress("knotx.action.adapter")
        .register(AdapterProxy.class, (request, result) -> {
          String path = request.getParams().getString("path");
          if (StringUtils.isNotBlank(path)) {
            if (path.equals("/service/mock/post-competition.json")) {
              clientResponse.setStatusCode(200)
                  .setBody(new JsonObject().put("form", competitionData).toBuffer());
            } else if (path.equals("/service/mock/post-newsletter.json")) {
              clientResponse.setStatusCode(200)
                  .setBody(new JsonObject().put("form", newsletterData).toBuffer());
            }
          }
          result.handle(Future.succeededFuture(resp));
        });
  }
}
