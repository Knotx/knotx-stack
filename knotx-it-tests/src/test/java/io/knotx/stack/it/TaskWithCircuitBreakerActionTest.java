package io.knotx.stack.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class TaskWithCircuitBreakerActionTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  @Test
  @Disabled
  @DisplayName("Expect page containing data from services and fallback data for broken service.")
  @KnotxApplyConfiguration({"conf/application.conf", "scenarios/task-with-circuit-breaker/mocks.conf",
      "scenarios/task-with-circuit-breaker/tasks.conf"})
  void taskWithCircuitBreaker(VertxTestContext context, Vertx vertx,
      @RandomPort Integer delayedServicePort,
      @RandomPort Integer globalServerPort) {
    // given
    WireMockServer wireMockServer = new WireMockServer(delayedServicePort);
    wireMockServer.stubFor(get(urlEqualTo("/service/mock/delayed")).willReturn(
        aResponse()
            .withStatus(200)
            .withFixedDelay(2000)));

    // when
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester
        .testGetRequest(context, vertx, "/content/fullPage.html", "results/fullPage-fallback.html");


  }

}
