package io.knotx.stack.it;

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.knotx.junit5.assertions.HtmlMarkupAssertions;
import io.knotx.junit5.util.FileReader;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.util.Map;

final class KnotxServerTester {

  private static final int KNOTX_TESTS_SERVER_PORT_DEFAULT = 9092;
  private static final String KNOTX_TESTS_SERVER_ADDRESS_DEFAULT = "localhost";

  private final String serverHost;
  private final int serverPort;

  KnotxServerTester(String serverHost, int serverPort) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
  }

  static KnotxServerTester defaultInstance() {
    return defaultInstance(KNOTX_TESTS_SERVER_PORT_DEFAULT);
  }

  static KnotxServerTester defaultInstance(int port) {
    return new KnotxServerTester(KNOTX_TESTS_SERVER_ADDRESS_DEFAULT, port);
  }

  void testPostRequest(VertxTestContext context, Vertx vertx, String url,
      Map<String, Object> formData, String expectedResponseFile) {

    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .post(serverPort, serverHost, url)
        .rxSendForm(getMultiMap(formData));

    subscribeToResult_shouldSucceed(context, httpResponseSingle, resp -> {
      HtmlMarkupAssertions.assertHtmlBodyMarkupsEqual(FileReader.readTextSafe(expectedResponseFile),
          resp.body().toString());
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
    });
  }

  void testGetRequest(VertxTestContext context, Vertx vertx, String url,
      String expectedResponseFile) {
    testGet(context, vertx, url, resp -> {
      HtmlMarkupAssertions.assertHtmlBodyMarkupsEqual(FileReader.readTextSafe(expectedResponseFile),
          resp.body().toString());
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
    });
  }

  void testGetServerError(VertxTestContext context, Vertx vertx, String url,
      int expectedError) {
    testGet(context, vertx, url, resp -> {
      assertEquals(expectedError, resp.statusCode());
    });
  }

  void testGet(VertxTestContext context, Vertx vertx, String url,
      Consumer<HttpResponse<Buffer>> assertions) {
    WebClient client = WebClient.create(vertx);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .get(serverPort, serverHost, url)
        .rxSend();

    subscribeToResult_shouldSucceed(context, httpResponseSingle,
        resp -> {
          assertions.accept(resp);
          client.close();
        });
  }

  private MultiMap getMultiMap(Map<String, Object> formData) {
    MultiMap formMap = MultiMap.caseInsensitiveMultiMap();
    formData.forEach((key, value) -> formMap.add(key, (String) value));
    return formMap;
  }
}
