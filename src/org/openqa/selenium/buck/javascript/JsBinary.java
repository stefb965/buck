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

import static com.facebook.buck.util.BuckConstant.GEN_DIR;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildOutputInitializer;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.Buildable;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.InitializableFromDisk;
import com.facebook.buck.rules.OnDiskBuildInfo;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePaths;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.WriteFileStep;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class JsBinary extends AbstractBuildable implements
    InitializableFromDisk<JavascriptDependencies>, HasJavascriptDependencies {

  private final Path joyPath;
  private final Path output;
  private final ImmutableSortedSet<BuildRule> deps;
  private final ImmutableSortedSet<SourcePath> srcs;
  private final Optional<List<String>> defines;
  private final Optional<List<Path>> externs;
  private final Optional<List<String>> flags;
  private JavascriptDependencies joy;
  private final BuildOutputInitializer<JavascriptDependencies> buildOutputInitializer;

  public JsBinary(
      BuildTarget target,
      ImmutableSortedSet<BuildRule> deps,
      ImmutableSortedSet<SourcePath> srcs,
      Optional<List<String>> defines,
      Optional<List<String>> flags,
      Optional<List<Path>> externs) {
    super(target);
    this.deps = Preconditions.checkNotNull(deps);
    this.srcs = Preconditions.checkNotNull(srcs);
    this.defines = Preconditions.checkNotNull(defines);
    this.externs = Preconditions.checkNotNull(externs);
    this.flags = Preconditions.checkNotNull(flags);

    this.output = Paths.get(
        GEN_DIR, target.getBaseName(), target.getShortName() + ".js");
    this.joyPath = Paths.get(
        GEN_DIR, target.getBaseName(), target.getShortName() + ".deps");

    buildOutputInitializer = new BuildOutputInitializer<>(target, this);
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(srcs);
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder
        .set("defines", defines.or(ImmutableList.<String>of()))
        .setInputs("externs", externs.or(ImmutableList.<Path>of()).iterator())
        .setSourcePaths("srcs", srcs)
        ;
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    JavascriptDependencyGraph graph = new JavascriptDependencyGraph();

    // Iterate over deps and build a bundle of joy
    JavascriptDependencies smidgen = new JavascriptDependencies();

    Set<String> jsDeps = Sets.newHashSet();
    // Do the magic with the sources, as if we're a js_library
    for (SourcePath src : srcs) {
      Path resolved = src.resolve();
      JavascriptSource jsSource = new JavascriptSource(resolved);

      graph.amendGraph(jsSource);
      smidgen.add(jsSource);
      jsDeps.addAll(jsSource.getRequires());
    }

    Set<String> seen = Sets.newHashSet();
    for (BuildRule dep : deps) {
      Buildable buildable = dep.getBuildable();
      if (!(buildable instanceof HasJavascriptDependencies)) {
        continue;
      }

      JavascriptDependencies moreJoy = ((HasJavascriptDependencies) buildable).getBundleOfJoy();

      for (String require : jsDeps) {
        Set<JavascriptSource> sources = moreJoy.getDeps(require);
        if (!seen.contains(require) && !sources.isEmpty()) {
          smidgen.addAll(sources);
          graph.amendGraph(sources);
          seen.add(require);
        }
      }
    }

    // Now ask that mess of data for the things we need.
    for (String dep : jsDeps) {
      smidgen.amendGraph(graph, dep);
    }

    ImmutableSortedSet<Path> requiredSources = graph.sortSources();
    ClosureCompilerStep compiler = ClosureCompilerStep.builder()
        .defines(defines)
        .externs(externs)
        .flags(flags)
        .prettyPrint()
        .sources(requiredSources)
        .output(output)
        .build();

    StringWriter writer = new StringWriter();
    smidgen.writeTo(writer);

    steps.add(new MkdirStep(joyPath.getParent()));
    steps.add(new WriteFileStep(writer.toString(), joyPath));
    steps.add(new MkdirStep(output.getParent()));
    steps.add(compiler);

    return steps.build();
  }

  @Nullable
  @Override
  public Path getPathToOutputFile() {
    return output;
  }

  @Override
  public JavascriptDependencies initializeFromDisk(OnDiskBuildInfo onDiskBuildInfo) {
    try {
      List<String> allLines = onDiskBuildInfo.getOutputFileContentsByLine(joyPath);
      joy = JavascriptDependencies.buildFrom(Joiner.on("\n").join(allLines));
      return joy;
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public BuildOutputInitializer<JavascriptDependencies> getBuildOutputInitializer() {
    return buildOutputInitializer;
  }

  @Override
  public JavascriptDependencies getBundleOfJoy() {
    return joy;
  }
}
