/*
 * Copyright 2014-present Facebook, Inc.
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

package org.openqa.selenium.buck.file;

import static com.facebook.buck.zip.ZipStep.DEFAULT_COMPRESSION_LEVEL;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.AddToRuleKey;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Folder extends AbstractBuildRule {
  @AddToRuleKey(stringify = true)
  private final Path folderName;
  private final Path output;
  @AddToRuleKey
  private final ImmutableSortedSet<SourcePath> srcs;

  protected Folder(
      BuildRuleParams buildRuleParams,
      SourcePathResolver resolver,
      String folderName,
      ImmutableSortedSet<SourcePath> srcs) {
    super(buildRuleParams, resolver);

    BuildTarget target = getBuildTarget();
    this.folderName = Preconditions.checkNotNull(Paths.get(folderName));
    this.output = BuildTargets.getGenPath(
        target,
        String.format("%s/%%s.src.zip", target.getShortName()));
    this.srcs = Preconditions.checkNotNull(srcs);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    steps.add(new MakeCleanDirectoryStep(output.getParent()));

    Path scratch = BuildTargets.getScratchPath(getBuildTarget(), "%s-scratch/" + folderName);
    steps.add(new MakeCleanDirectoryStep(scratch));

    SrcZipAwareFileBundler bundler = new SrcZipAwareFileBundler(getBuildTarget());
    bundler.copy(getResolver(), steps, scratch, srcs, false);
    steps.add(new ZipStep(
            output,
            ImmutableSet.<Path>of(),
            false,
            DEFAULT_COMPRESSION_LEVEL,
            scratch.getParent()));

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  @Override
  public Path getPathToOutputFile() {
    return output;
  }
}
