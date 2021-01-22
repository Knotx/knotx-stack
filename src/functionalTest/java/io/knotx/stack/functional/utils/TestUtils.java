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
package io.knotx.stack.functional.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpResponse;

public final class TestUtils {

  private TestUtils() {
    // utility class
  }

  public static void OK200(HttpResponse<Buffer> response) {
    assertEquals(HttpResponseStatus.OK.code(), response.statusCode());
  }

  public static void bodyNotEmpty(HttpResponse<Buffer> response) {
    assertNotNull(response.bodyAsString());
    assertFalse(response.bodyAsString().isEmpty());
  }

}
