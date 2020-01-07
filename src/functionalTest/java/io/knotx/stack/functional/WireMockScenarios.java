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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.knotx.junit5.util.FileReader;

public interface WireMockScenarios {

  static WireMockServer firstOffersServiceInvocationWithDelay(Integer scenarioServicePort) {
    WireMockServer scenarioMockService = new WireMockServer(scenarioServicePort);
    scenarioMockService.stubFor(get(urlEqualTo("/service/mock/scenario")).inScenario("DELAYED_FIRST_INVOCATION")
        .whenScenarioStateIs(STARTED)
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withFixedDelay(100)
            .withBody(FileReader.readTextSafe("service/mock/emptyOffers.json")))
        .willSetStateTo("RETRY"));
    scenarioMockService.stubFor(get(urlEqualTo("/service/mock/scenario")).inScenario("DELAYED_FIRST_INVOCATION")
        .whenScenarioStateIs("RETRY")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(FileReader.readTextSafe("service/mock/specialOffers.json"))));
    return scenarioMockService;
  }

}
