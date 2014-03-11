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

import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.Buildable;
import com.facebook.buck.rules.ConstructorArg;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.SourcePath;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Path;
import java.util.List;

public class JsBinaryDescription implements Description<JsBinaryDescription.Arg> {

  private final static BuildRuleType TYPE = new BuildRuleType("js_binary");

  @Override
  public BuildRuleType getBuildRuleType() {
    return TYPE;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public Buildable createBuildable(BuildRuleParams params, Arg args) {
    return new JsBinary(
        params.getBuildTarget(),
        params.getDeps(),
        args.srcs,
        args.defines,
        args.flags,
        args.externs);
  }

  public static class Arg implements ConstructorArg {
    public Optional<List<String>> defines;
    public Optional<List<Path>> externs;
    public Optional<List<String>> flags;
    public ImmutableSortedSet<SourcePath> srcs;

    public Optional<ImmutableSortedSet<BuildRule>> deps;
  }
}
