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
rootProject.name = "Knot.x - Integration Tests"

if (file(".composite-enabled").exists()) {
    includeBuild("../../knotx-commons")
    includeBuild("../../knotx-launcher")
    includeBuild("../../knotx-junit5")
    includeBuild("../../knotx-fragment-api")
    includeBuild("../../knotx-server-http")
    includeBuild("../../knotx-repository-connector")
    includeBuild("../../knotx-fragments-handler")
    includeBuild("../../knotx-data-bridge")
    includeBuild("../../knotx-template-engine")
}