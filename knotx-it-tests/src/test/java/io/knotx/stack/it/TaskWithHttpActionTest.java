package io.knotx.stack.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.knotx.junit5.wiremock.KnotxWiremockExtension.stubForServer;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.junit5.wiremock.ClasspathResourcesMockServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class TaskWithHttpActionTest {

  @ClasspathResourcesMockServer
  private WireMockServer mockService;

  @ClasspathResourcesMockServer
  private WireMockServer mockRepository;

  @Test
  @DisplayName("Expect page containing data from HTTP services.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/task-with-http-action/mocks.conf",
      "scenarios/task-with-http-action/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx,
      @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGetRequest(context, vertx, "/content/fullPage.html", "results/fullPage.html");
  }

}
