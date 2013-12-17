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
import static java.nio.charset.StandardCharsets.UTF_8;

import com.facebook.buck.graph.DefaultImmutableDirectedAcyclicGraph;
import com.facebook.buck.graph.ImmutableDirectedAcyclicGraph;
import com.facebook.buck.graph.MutableDirectedGraph;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.InitializableFromDisk;
import com.facebook.buck.rules.OnDiskBuildInfo;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePaths;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.WriteFileStep;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsLibrary extends AbstractBuildable implements InitializableFromDisk<ImmutableDirectedAcyclicGraph<String>> {

  private final Path output;
  private final ImmutableSortedSet<SourcePath> srcs;
  private ImmutableDirectedAcyclicGraph<String> graph;

  public JsLibrary(BuildTarget buildTarget, ImmutableSortedSet<SourcePath> srcs) {
    this.srcs = Preconditions.checkNotNull(srcs);

    this.output = Paths.get(
        GEN_DIR, buildTarget.getBaseName(), buildTarget.getShortName() + "-library.graph");
  }

  @Override
  public Iterable<String> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(srcs);
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) throws IOException {
    return builder
        .setSourcePaths("srcs", srcs);
  }

  @Override
  public List<Step> getBuildSteps(BuildContext context, BuildableContext buildableContext) throws IOException {
    ImmutableList.Builder<Step> builder = ImmutableList.builder();

    HashMultimap<String, String> provideToRequires = HashMultimap.create();
    for (SourcePath src : srcs) {
      Path path = src.resolve(context);
      JavascriptSource source = new JavascriptSource(path);

      for (String provide : source.getProvides()) {
        provideToRequires.putAll(provide, source.getRequires());
      }
    }

    StringWriter writer = new StringWriter();
    new ObjectMapper().writeValue(writer, provideToRequires.asMap());

    System.out.println("writer = " + writer.toString());

    builder.add(new MkdirStep(output.getParent()));
    builder.add(new WriteFileStep(writer.toString(), output));

    return builder.build();
  }

  @Override
  public ImmutableDirectedAcyclicGraph<String> initializeFromDisk(OnDiskBuildInfo onDiskBuildInfo) {
    Preconditions.checkState(graph == null, "Attempt to reinitialize from disk");

    try {
      List<String> allLines = onDiskBuildInfo.getOutputFileContentsByLine(output);
      return buildGraph(allLines);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void setBuildOutput(ImmutableDirectedAcyclicGraph<String> graph) throws IllegalStateException {
    Preconditions.checkState(this.graph == null, "Attempted to set build output more than once.");
    this.graph = graph;
  }

  @Override
  public ImmutableDirectedAcyclicGraph<String> getBuildOutput() throws IllegalStateException {
    Preconditions.checkNotNull(graph, "Build output has not been set.");

    try {
      List<String> allLines = Files.readAllLines(output, UTF_8);
      return buildGraph(allLines);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @SuppressWarnings("unchecked")
  private ImmutableDirectedAcyclicGraph<String> buildGraph(List<String> allLines) throws IOException {
    String rawJson = Joiner.on("\n").join(allLines);

    Map<String, Collection<String>> read = new ObjectMapper().readValue(rawJson, Map.class);

    MutableDirectedGraph<String> mutableGraph = new MutableDirectedGraph<>();
    for (Map.Entry<String, Collection<String>> entry : read.entrySet()) {
      mutableGraph.addNode(entry.getKey());
      for (String require : entry.getValue()) {
        mutableGraph.addNode(require);
        mutableGraph.addEdge(entry.getKey(), require);
      }
    }

    return new DefaultImmutableDirectedAcyclicGraph<>(mutableGraph);
  }

  @Override
  public String getPathToOutputFile() {
    return output.toString();
  }
}
