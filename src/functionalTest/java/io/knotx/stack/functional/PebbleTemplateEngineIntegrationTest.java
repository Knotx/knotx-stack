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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(KnotxExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
class PebbleTemplateEngineIntegrationTest {

  @Test
  @DisplayName("Expect page with markup processed by Pebble Template Engine")
  @KnotxApplyConfiguration({"conf/application.conf",
      "common/templating/routing.conf",
      "common/templating/fragments.conf",
      "scenarios/pebble-template-engine/mocks.conf",
      "scenarios/pebble-template-engine/tasks.conf"})
  void requestPage(VertxTestContext context, Vertx vertx,
      @RandomPort Integer globalServerPort) {
    // when
    KnotxServerTester serverTester = KnotxServerTester.defaultInstance(globalServerPort);
    serverTester.testGetWithExpectedResponse(context, vertx,
        "/content/fullPebblePage.html", "results/fullPage.html");
  }
}
