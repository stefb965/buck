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

package org.openqa.selenium.buck.mozilla;

import com.facebook.buck.event.LogEvent;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.util.BuckConstant;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Xpt extends AbstractBuildable {

  private final SourcePath fallback;
  private final Path src;
  private final Path out;

  public Xpt(BuildRuleParams params, Path src, SourcePath fallback) {
    this.fallback = Preconditions.checkNotNull(fallback);
    this.src = Preconditions.checkNotNull(src);
    String name = Files.getNameWithoutExtension(src.getFileName().toString()) + ".xpt";

    this.out = Paths.get(BuckConstant.GEN_DIR, params.getBuildTarget().getBasePath(), name);
  }

  @Override
  public Iterable<String> getInputsToCompareToOutput() {
    return ImmutableSortedSet.of(src.toString());
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) throws IOException {
    return builder
        .setInputs("src", ImmutableSet.of(src).iterator())
        .setInputs("out", ImmutableSet.of(out).iterator());
  }

  @Override
  public List<Step> getBuildSteps(BuildContext context, BuildableContext buildableContext)
      throws IOException {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    context.getEventBus().post(LogEvent.warning("Defaulting to fallback for " + out));
    Path from = fallback.resolve(context);

    steps.add(new MakeCleanDirectoryStep(out.getParent()));
    steps.add(new CopyStep(from, out));

    return steps.build();
  }

  @Override
  public String getPathToOutputFile() {
    return out.toString();
  }
}
