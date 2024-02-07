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
package io.knotx.stack;

import static io.knotx.junit5.util.RequestUtil.subscribeToResult_shouldSucceed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.knotx.junit5.assertions.HtmlMarkupAssertions;
import io.knotx.junit5.util.FileReader;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.util.Collections;
import java.util.Map;

public final class KnotxServerTester {

  private static final int KNOTX_TESTS_SERVER_PORT_DEFAULT = 8092;
  private static final String KNOTX_TESTS_SERVER_ADDRESS_DEFAULT = "localhost";

  private final String serverHost;
  private final int serverPort;

  private WebClientOptions clientOptions = new WebClientOptions();
  private Map<String, String> headers = Collections.emptyMap();

  private KnotxServerTester(String serverHost, int serverPort) {
    this.serverHost = serverHost;
    this.serverPort = serverPort;
  }

  public static KnotxServerTester defaultInstance() {
    return defaultInstance(KNOTX_TESTS_SERVER_PORT_DEFAULT);
  }

  public static KnotxServerTester defaultInstance(int port) {
    return new KnotxServerTester(KNOTX_TESTS_SERVER_ADDRESS_DEFAULT, port);
  }

  public KnotxServerTester withClientOptions(WebClientOptions clientOptions) {
    this.clientOptions = clientOptions;
    return this;
  }

  public KnotxServerTester withRequestHeaders(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public void testGetWithExpectedResponse(VertxTestContext context, Vertx vertx, String url,
      String expectedResponseFile) {
    testGet(context, vertx, url, resp -> {
      assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
      HtmlMarkupAssertions.assertHtmlBodyMarkupsEqual(FileReader.readTextSafe(expectedResponseFile),
          resp.body().toString());
    });
  }

  public void testGet(VertxTestContext context, Vertx vertx, String url,
      Consumer<HttpResponse<Buffer>> assertions) {
    MultiMap headersMultiMap = MultiMap.caseInsensitiveMultiMap();
    headersMultiMap.addAll(headers);

    WebClient client = WebClient.create(vertx, clientOptions);
    Single<HttpResponse<Buffer>> httpResponseSingle = client
        .get(serverPort, serverHost, url)
        .putHeaders(headersMultiMap)
        .rxSend();

    subscribeToResult_shouldSucceed(context, httpResponseSingle,
        resp -> {
          assertions.accept(resp);
          client.close();
        });
  }

}
