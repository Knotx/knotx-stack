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
import io.knotx.dataobjects.ClientResponse;
import io.knotx.forms.api.FormsAdapterProxy;
import io.knotx.forms.api.FormsAdapterResponse;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.util.FileReader;
import io.knotx.junit5.wiremock.KnotxWiremock;
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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class SampleApplicationIntegrationTest {

  private static final int KNOTX_SERVER_PORT = 9092;
  private static final String KNOTX_SERVER_ADDRESS = "localhost";

  @KnotxWiremock
  protected WireMockServer mockService;

  @KnotxWiremock
  protected WireMockServer mockRepository;

  @BeforeAll
  void initMocks() {
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
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestFsRepoSimplePage(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/fullPage.html", "results/local-fullPage.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestHttpRepoSimplePage(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/remote/fullPage.html", "results/remote-fullPage.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestPageWithRequestParameters(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx,
        "/content/remote/fullPage.html?parameter%20with%20space=value&q=knotx",
        "results/remote-fullPage.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestPageWithMissingDataDefinitionWithoutConfiguration_expectServerError(
      VertxTestContext context, Vertx vertx) {
    testGetServerError(context, vertx, "/content/local/notExistingService.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestPageWithMissingDataDefinitionAndFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/notExistingServiceWithFallback.html",
        "results/notExistingServiceWithFallback.html");
  }

  @Test
  @KnotxApplyConfiguration({"conf/it-tests-application.conf",
      "conf/overrides/default-fallback.conf"})
  public void requestPageWithMissingDataDefinitionWhenGlobalFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/notExistingService.html",
        "results/notExistingServiceWithGlobalFallback.html");
  }

  @Test
  @KnotxApplyConfiguration({"conf/it-tests-application.conf",
      "conf/overrides/custom-tag-and-prefix.conf"})
  public void requestPageWithCustomTagAndParamPrefix(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/customSnippetTag.html",
        "results/customSnippetTag.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void requestPageThatUseFormsDatabridgeAndTe(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, "/content/local/formsBridgeTe.html",
        "results/formsBridgeTe.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/it-tests-application.conf")
  public void submitOneFormAfterAnother(VertxTestContext context, Vertx vertx) {
    final JsonObject competitionFormData = new JsonObject()
        .put("name", "test")
        .put("email", "email-1@example.com")
        .put("_frmId", "competition");
    final JsonObject newsletterFormData = new JsonObject()
        .put("name", "test")
        .put("email", "email-2@example.com")
        .put("_frmId", "newsletter");

    mockFormsAdapter(vertx, competitionFormData, newsletterFormData);
    testPostRequest(context, vertx, "/content/local/formsBridgeTe.html",
        competitionFormData.getMap(),
        "results/submitCompetitionForm.html");
    testPostRequest(context, vertx, "/content/local/formsBridgeTe.html",
        newsletterFormData.getMap(),
        "results/submitNewsletterForm.html");
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

  private void mockFormsAdapter(Vertx vertx, JsonObject competitionData,
      JsonObject newsletterData) {
    ClientResponse clientResponse = new ClientResponse().setStatusCode(404);
    FormsAdapterResponse resp = new FormsAdapterResponse().setResponse(clientResponse);

    new ServiceBinder(vertx.getDelegate())
        .setAddress("knotx.forms.mock.adapter")
        .register(FormsAdapterProxy.class, (request, result) -> {
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
