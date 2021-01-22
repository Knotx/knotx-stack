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
package io.knotx.stack.functional.debug;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DebugUtils {

  private static final String SCRIPT_REGEXP = "<script data-knotx-debug=\"log\" data-knotx-id=\"(?<fragmentId>[^\"]+?)\" type=\"application/json\">(?<logJson>.*?)</script>";
  private static final Pattern SCRIPT_PATTERN = Pattern.compile(SCRIPT_REGEXP, Pattern.DOTALL);

  private DebugUtils() {
    // utility class
  }

  public static LinkedHashMap<String, String> findLogsInHtml(String html) {
    LinkedHashMap<String, String> logs = new LinkedHashMap<>();
    Matcher matcher = SCRIPT_PATTERN.matcher(html);
    while (matcher.find()) {
      String id = matcher.group("fragmentId");
      String log = matcher.group("logJson");
      logs.put(id, log);
    }
    return logs;
  }

}
