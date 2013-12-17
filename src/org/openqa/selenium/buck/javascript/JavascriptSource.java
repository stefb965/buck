/*
 * Copyright 2013-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.openqa.selenium.buck.javascript;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSortedSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavascriptSource {

  private static Pattern REQUIRE = Pattern.compile(
      "^goog\\.require\\s*\\(\\s*[\\'\\\"]([^\\)]+)[\\'\\\"]\\s*\\)");
  private static Pattern PROVIDE = Pattern.compile(
      "^goog\\.provide\\s*\\(\\s*[\\'\\\"]([^\\)]+)[\\'\\\"]\\s*\\)");


  private final Path path;
  private final ImmutableSortedSet<String> provides;
  private final ImmutableSortedSet<String> requires;

  public JavascriptSource(Path path) {
    this.path = Preconditions.checkNotNull(path);

    ImmutableSortedSet.Builder<String> toProvide = ImmutableSortedSet.naturalOrder();
    ImmutableSortedSet.Builder<String> toRequire = ImmutableSortedSet.naturalOrder();

    try {
      for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
        Matcher requireMatcher = REQUIRE.matcher(line);
        if (requireMatcher.find()) {
          toRequire.add(requireMatcher.group(1));
        }

        Matcher provideMatcher = PROVIDE.matcher(line);
        if (provideMatcher.find()) {
          toProvide.add(provideMatcher.group(1));
        }
      }
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

    this.provides = toProvide.build();
    this.requires = toRequire.build();

  }

  public Path getPath() {
    return path;
  }

  public ImmutableSortedSet<String> getProvides() {
    return provides;
  }

  public ImmutableSortedSet<String> getRequires() {
    return requires;
  }
}
