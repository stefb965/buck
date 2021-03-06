/*
 * Copyright 2015-present Facebook, Inc.
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

package com.facebook.buck.cxx;

import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.RuleKeyObjectSink;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.Tool;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.Optional;

public class WindowsPreprocessor implements Preprocessor {

  private static Function<String, String> prependIncludeFlag = "/I"::concat;

  private final Tool tool;

  public WindowsPreprocessor(Tool tool) {
    this.tool = tool;
  }

  @Override
  public Optional<ImmutableList<String>> getFlagsForColorDiagnostics() {
    return Optional.empty();
  }

  @Override
  public boolean supportsHeaderMaps() {
    return false;
  }

  @Override
  public boolean supportsPrecompiledHeaders() {
    return false;
  }

  @Override
  public Iterable<String> localIncludeArgs(Iterable<String> includeRoots) {
    return Iterables.transform(includeRoots, prependIncludeFlag);
  }

  @Override
  public Iterable<String> systemIncludeArgs(Iterable<String> includeRoots) {
    return Iterables.transform(includeRoots, prependIncludeFlag);
  }

  @Override
  public ImmutableCollection<BuildRule> getDeps(SourcePathRuleFinder ruleFinder) {
    return tool.getDeps(ruleFinder);
  }

  @Override
  public ImmutableCollection<SourcePath> getInputs() {
    return tool.getInputs();
  }

  @Override
  public ImmutableList<String> getCommandPrefix(SourcePathResolver resolver) {
    return tool.getCommandPrefix(resolver);
  }

  @Override
  public ImmutableMap<String, String> getEnvironment() {
    return tool.getEnvironment();
  }

  @Override
  public void appendToRuleKey(RuleKeyObjectSink sink) {
    sink
        .setReflectively("tool", tool)
        .setReflectively("type", getClass().getSimpleName());
  }

}
