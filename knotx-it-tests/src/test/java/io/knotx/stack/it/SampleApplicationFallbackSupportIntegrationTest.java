package io.knotx.stack.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.knotx.junit5.wiremock.KnotxWiremockExtension.stubForServer;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.wiremock.KnotxWiremock;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@Disabled("Fallbacks need to be reimplemented.")
@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class SampleApplicationFallbackSupportIntegrationTest {

  @KnotxWiremock
  private WireMockServer mockService;

  @KnotxWiremock
  private WireMockServer mockRepository;

  private KnotxServerTester knotxServerTester;

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

    knotxServerTester = KnotxServerTester.defatultInstance();
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  public void requestPageWhenFormsProcessingFails_expectServerError(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetServerError(context, vertx, "/content/local/notExistingFormsAdapter.html",
            HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  @Disabled("Bug in forms - when using undefined data adapter fallback is not applied")
  public void requestPageWhenFormsProcessingFailsAndFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingFormsAdapterWithFallback.html",
            "results/pageWithFallback.html");
  }

  @Test
  @KnotxApplyConfiguration({"conf/application.conf",
      "conf/overrides/defaultFallback.conf"})
  @Disabled("Bug in forms - when using undefined data adapter fallback is not applied")
  public void requestPageWhenFormsProcessingFailsAndGlobalFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingFormsAdapter.html",
            "results/pageWithGlobalFallback.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  public void requestPageWhenDatabridgeProcessingFails_expectServerError(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetServerError(context, vertx, "/content/local/notExistingDataDefinition.html",
            HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  public void requestPageWhenDatabridgeProcessingFailsAndFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingDataDefinitionWithFallback.html",
            "results/pageWithFallback.html");
  }

  @Test
  @KnotxApplyConfiguration({"conf/application.conf",
      "conf/overrides/defaultFallback.conf"})
  public void requestPageWhenDatabridgeProcessingFailsAndGlobalFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingDataDefinition.html",
            "results/pageWithGlobalFallback.html");
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  public void requestPageWhenTemplateEngineProcessingFails_expectServerError(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetServerError(context, vertx, "/content/local/notExistingTemplateEngine.html",
            HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }

  @Test
  @KnotxApplyConfiguration("conf/application.conf")
  public void requestPageWhenTemplateEngineProcessingFailsAndFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingTemplateEngineWithFallback.html",
            "results/pageWithFallback.html");
  }

  @Test
  @KnotxApplyConfiguration({"conf/application.conf",
      "conf/overrides/defaultFallback.conf"})
  public void requestPageWhenTemplateEngineProcessingFailsAndGlobalFallbackDefined(
      VertxTestContext context, Vertx vertx) {
    knotxServerTester
        .testGetRequest(context, vertx, "/content/local/notExistingTemplateEngine.html",
            "results/pageWithGlobalFallback.html");
  }

}
