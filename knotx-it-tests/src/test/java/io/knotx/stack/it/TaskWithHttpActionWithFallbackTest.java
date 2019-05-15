package io.knotx.stack.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class TaskWithHttpActionWithFallbackTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  @Test
  @DisplayName("Expect page containing data from services and fallback data for broken service.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/task-with-http-action-with-fallback/mocks.conf",
      "scenarios/task-with-http-action-with-fallback/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx, @RandomPort Integer mockBrokenServicePort,
      @RandomPort Integer globalServerPort) {
    // when
    WireMockServer mockBrokenService = new WireMockServer(mockBrokenServicePort);
    mockBrokenService.stubFor(get(urlMatching("/service/broken/.*"))
        .willReturn(
            aResponse()
                .withStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        ));

    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester
        .testGetRequest(context, vertx, "/content/fullPage.html",
            "scenarios/task-with-http-action-with-fallback/result/fullPage.html");
  }

}
