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
package io.knotx.stack.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class ManyHandlebarsSnippetsWithDebugScenarioTest {

  @Test
  @DisplayName("Expect page containing fragment's debug data.")
  @KnotxApplyConfiguration({"conf/application.conf",
      "scenarios/many-handlebars-snippets-with-debug/mocks.conf",
      "scenarios/many-handlebars-snippets-with-debug/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx,
      @RandomPort Integer globalServerPort) {
    // when
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGet(context, vertx, "/content/fullPage.html?debug=true",
        resp -> {
          assertEquals(HttpResponseStatus.OK.code(), resp.statusCode());
          String response = resp.bodyAsString();
          assertNotNull(response);

          String scriptRegexp = "<script data-knotx-id=\"?.*?\" type=\"application/json\">(?<fragmentEventJson>.*?)</script>";
          Pattern scriptPattern = Pattern.compile(scriptRegexp, Pattern.DOTALL);

          Matcher matcher = scriptPattern.matcher(response);
          // contains 3 fragments
          assertTrue(matcher.find());
          assertTrue(matcher.find());
          assertTrue(matcher.find());
          assertFalse(matcher.find());
        });
  }
}
