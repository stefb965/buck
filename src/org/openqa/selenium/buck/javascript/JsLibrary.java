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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JsLibrary extends AbstractBuildable implements
    InitializableFromDisk<JavascriptDependencies>, HasJavascriptDependencies {

  private final Path output;
  private final ImmutableSortedSet<BuildRule> deps;
  private final ImmutableSortedSet<SourcePath> srcs;
  private JavascriptDependencies joy;
  private final BuildOutputInitializer<JavascriptDependencies> buildOutputInitializer;

  public JsLibrary(
      BuildTarget target,
      ImmutableSortedSet<BuildRule> deps,
      ImmutableSortedSet<SourcePath> srcs) {
    super(target);
    this.deps = Preconditions.checkNotNull(deps);
    this.srcs = Preconditions.checkNotNull(srcs);

    this.output = Paths.get(
        GEN_DIR, target.getBaseName(), target.getShortName() + "-library.deps");

    buildOutputInitializer = new BuildOutputInitializer<>(target, this);
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(srcs);
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder
        .setSourcePaths("srcs", srcs);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    Set<String> allRequires = Sets.newHashSet();
    Set<String> allProvides = Sets.newHashSet();
    JavascriptDependencies smidgen = new JavascriptDependencies();
    for (SourcePath src : srcs) {
      Path path = src.resolve();
      JavascriptSource source = new JavascriptSource(path);
      smidgen.add(source);
      allRequires.addAll(source.getRequires());
      allProvides.addAll(source.getProvides());
    }

    allRequires.removeAll(allProvides);

    for (BuildRule dep : deps) {
      Iterator<String> iterator = allRequires.iterator();

      Buildable buildable = dep.getBuildable();
      if (!(buildable instanceof HasJavascriptDependencies)) {
        continue;
      }
      JavascriptDependencies moreJoy = ((HasJavascriptDependencies) buildable).getBundleOfJoy();

      while (iterator.hasNext()) {
        String require = iterator.next();

        Set<JavascriptSource> sources = moreJoy.getDeps(require);
        if (!sources.isEmpty()) {
          smidgen.addAll(sources);
          iterator.remove();
        }
      }
    }

    if (!allRequires.isEmpty()) {
      throw new RuntimeException(target + " --- Missing dependencies for: " + allRequires);
    }

    StringWriter writer = new StringWriter();
    smidgen.writeTo(writer);

    ImmutableList.Builder<Step> builder = ImmutableList.builder();
    builder.add(new MkdirStep(output.getParent()));
    builder.add(new WriteFileStep(writer.toString(), output));

    return builder.build();
  }

  @Override
  public JavascriptDependencies initializeFromDisk(OnDiskBuildInfo onDiskBuildInfo) {
    try {
      List<String> allLines = onDiskBuildInfo.getOutputFileContentsByLine(output);
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
//
//
//
//  @Override
//  public void setBuildOutput(JavascriptDependencies joy) throws IllegalStateException {
//    Preconditions.checkState(this.joy == null, "Attempted to set build output more than once.");
//    this.joy = joy;
//  }
//
//  @Override
//  public JavascriptDependencies getBuildOutput() throws IllegalStateException {
//    Preconditions.checkNotNull(joy, "Build output has not been set.");
//
//    try {
//      List<String> allLines = Files.readAllLines(output, UTF_8);
//      return JavascriptDependencies.buildFrom(Joiner.on("\n").join(allLines));
//    } catch (IOException e) {
//      throw Throwables.propagate(e);
//    }
//  }

  @Override
  public Path getPathToOutputFile() {
    return output;
  }

  public JavascriptDependencies getBundleOfJoy() {
    return joy;
  }
}
