/*
 *  Copyright (c) 2011-2018 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.knotx.example.monolith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
public class SampleApplicationHeadersIntegrationTest {

  private static final String REMOTE_REQUEST_URI = "/content/remote/simple.html";
  private static final int KNOTX_SERVER_PORT = 9092;
  private static final String KNOTX_SERVER_ADDRESS = "localhost";

  private MultiMap expectedHeaders = MultiMap.caseInsensitiveMultiMap();

  @BeforeEach
  public void before() {
    expectedHeaders.add("Access-Control-Allow-Origin", "*");
    expectedHeaders.add("Content-Type", "text/html; charset=UTF-8");
    expectedHeaders.add("content-length", "3020");
    expectedHeaders.add("X-Server", "Knot.x");
  }

  @Test
  @KnotxApplyConfiguration("knotx-test-app.json")
  public void whenRequestingRemoteRepository_expectOnlyAllowedResponseHeaders(
      VertxTestContext context, Vertx vertx) {
    testGetRequest(context, vertx, REMOTE_REQUEST_URI);
  }

  private void testGetRequest(VertxTestContext context, Vertx vertx, String url) {
    HttpClient client = vertx.createHttpClient();
    client.getNow(KNOTX_SERVER_PORT, KNOTX_SERVER_ADDRESS, url,
        resp -> {
          MultiMap headers = resp.headers();
          headers.names().forEach(name -> {
            assertEquals(resp.statusCode(), 200, "Wrong status code received.");
            assertTrue(expectedHeaders.contains(name), "Header " + name + " is not expected.");
            assertEquals(expectedHeaders.get(name), headers.get(name),
                "Wrong value of " + name + " header.");
          });
          context.completeNow();
        });
  }
}
