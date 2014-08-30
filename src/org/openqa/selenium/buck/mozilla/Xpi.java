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

package org.openqa.selenium.buck.mozilla;

import static com.facebook.buck.step.fs.CopyStep.DirectoryMode.DIRECTORY_AND_CONTENTS;

import com.facebook.buck.java.JavacStep;
import com.facebook.buck.log.Logger;
import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.zip.UnzipStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nullable;

public class Xpi extends AbstractBuildRule {

  private final static Logger LOG = Logger.get(Xpi.class);

  private final Path scratch;
  private final Path output;
  private final Path chrome;
  private final ImmutableSortedSet<SourcePath> components;
  private final ImmutableSortedSet<Path> content;
  private final Path install;
  private final ImmutableSortedSet<SourcePath> resources;
  private final ImmutableSortedSet<SourcePath> platforms;

  public Xpi(
      BuildRuleParams params,
      Path chrome,
      ImmutableSortedSet<SourcePath> components,
      ImmutableSortedSet<Path> content,
      Path install,
      ImmutableSortedSet<SourcePath> resources,
      ImmutableSortedSet<SourcePath> platforms) {
    super(params);

    this.chrome = Preconditions.checkNotNull(chrome);
    this.components = Preconditions.checkNotNull(components);
    this.content = Preconditions.checkNotNull(content);
    this.install = Preconditions.checkNotNull(install);
    this.resources = Preconditions.checkNotNull(resources);
    this.platforms = Preconditions.checkNotNull(platforms);

    this.output = BuildTargets.getGenPath(
        getBuildTarget(),
        String.format("%%s/%s.xpi", getBuildTarget().getShortName()));

    this.scratch = BuildTargets.getBinPath(getBuildTarget(), "%s-xpi");
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    return content;
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    steps.add(new MakeCleanDirectoryStep(scratch));

    steps.add(CopyStep.forFile(chrome, scratch.resolve("chrome.manifest")));
    steps.add(CopyStep.forFile(install, scratch.resolve("install.rdf")));

    Path contentDir = scratch.resolve("content");
    steps.add(new MkdirStep(contentDir));
    for (Path item : content) {
      Path destination = contentDir.resolve(item.getFileName());
      steps.add(getCopyStep(item, destination));
    }

    Path componentDir = scratch.resolve("components");
    steps.add(new MkdirStep(componentDir));
    for (SourcePath component : components) {
      Path resolved = component.resolve();
      copy(steps, resolved, componentDir);
    }

    Path platformDir = scratch.resolve("platform");
    steps.add(new MkdirStep(platformDir));
    for (SourcePath resource : platforms) {
      Path resolved = resource.resolve();
      copy(steps, resolved, platformDir);
    }

    Path resourceDir = scratch.resolve("resource");
    steps.add(new MkdirStep(resourceDir));
    for (SourcePath resource : resources) {
      Path resolved = resource.resolve();
      copy(steps, resolved, resourceDir);
    }

    steps.add(new MakeCleanDirectoryStep(output.getParent()));
    steps.add(new ZipStep(
        output.normalize().toAbsolutePath(),
        ImmutableSet.<Path>of(),
        false,
        ZipStep.DEFAULT_COMPRESSION_LEVEL,
        scratch));

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  private void copy(ImmutableList.Builder<Step> steps, Path item, Path destinationDir) {
    if (item.toString().endsWith(JavacStep.SRC_ZIP)) {
      steps.add(new UnzipStep(item, destinationDir));
    } else if (Files.isDirectory(item)) {
      LOG.warn("Being asked to copy a directory. Tsk!");
      steps.add(CopyStep.forDirectory(item, destinationDir, DIRECTORY_AND_CONTENTS));
    } else {
      steps.add(CopyStep.forFile(item, destinationDir.resolve(item.getFileName())));
    }
  }

  private CopyStep getCopyStep(Path item, Path destination) {
    if (item.toFile().isDirectory()) {
      return CopyStep.forDirectory(item, destination, DIRECTORY_AND_CONTENTS);
    }

    return CopyStep.forFile(item, destination);
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder
        .setInputs("chrome", ImmutableSet.of(chrome).iterator())
        .setSourcePaths("components", components)
        .setInputs("content", content.iterator())
        .setInputs("install", ImmutableSet.of(install).iterator())
        .setSourcePaths("resources", resources)
        ;
  }

  @Nullable
  @Override
  public Path getPathToOutputFile() {
    return output;
  }
}
