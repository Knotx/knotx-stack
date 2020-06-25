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

import io.knotx.junit5.KnotxApplyConfiguration;
import io.knotx.junit5.KnotxExtension;
import io.knotx.junit5.RandomPort;
import io.knotx.stack.KnotxServerTester;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
class TemplateEnginesIntegrationTest {

  @Test
  @DisplayName("Should return page properly processed by handlebars")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/template-engines-integration/mocks.conf",
      "scenarios/template-engines-integration/tasks.conf",
      "scenarios/template-engines-integration/pebble.conf"})
  void requestPage(VertxTestContext testContext, Vertx vertx,
      @RandomPort Integer globalServerPort) {
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGetWithExpectedResponse(testContext, vertx, "/content/payments.html",
        "scenarios/template-engines-integration/resultPage.html");
  }
}
