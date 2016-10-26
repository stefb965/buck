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

package com.facebook.buck.jvm.java;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.facebook.buck.event.BuckEventBusFactory;
import com.facebook.buck.jvm.core.JavaPackageFinder;
import com.facebook.buck.model.BuildTargetFactory;
import com.facebook.buck.parser.NoSuchBuildTargetException;
import com.facebook.buck.rules.ActionGraph;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildTargetSourcePath;
import com.facebook.buck.rules.DefaultTargetNodeToBuildRuleTransformer;
import com.facebook.buck.rules.FakeBuildRuleParamsBuilder;
import com.facebook.buck.rules.FakeBuildableContext;
import com.facebook.buck.rules.FakeSourcePath;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.TargetGraph;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.easymock.EasyMock;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class JavaSourceJarTest {

  @Test
  public void outputNameShouldIndicateThatTheOutputIsASrcJar() throws NoSuchBuildTargetException {
    BuildRuleResolver resolver = new BuildRuleResolver(
        TargetGraph.EMPTY,
        new DefaultTargetNodeToBuildRuleTransformer());

    JavaSourceJar rule = new JavaSourceJar(
        new FakeBuildRuleParamsBuilder("//example:target#src").build(),
        new SourcePathResolver(resolver),
        JavaLibraryBuilder.createBuilder(BuildTargetFactory.newInstance("//example:target"))
            .build(resolver),
        RuleGatherer.SINGLE_JAR,
        Optional.<Path>empty(),
        Optional.<String>empty());

    Path output = rule.getPathToOutput();

    assertNotNull(output);
    assertTrue(output.toString().endsWith(Javac.SRC_JAR));
  }

  @Test
  public void shouldOnlyIncludePathBasedSources() throws NoSuchBuildTargetException {
    BuildRuleResolver resolver = new BuildRuleResolver(
        TargetGraph.EMPTY,
        new DefaultTargetNodeToBuildRuleTransformer());
    SourcePathResolver pathResolver = new SourcePathResolver(resolver);
    SourcePath fileBased = new FakeSourcePath("some/path/File.java");
    BuildRule dep =
        JavaLibraryBuilder.createBuilder(BuildTargetFactory.newInstance("//cheese:cake"))
            .build(resolver);
    SourcePath ruleBased = new BuildTargetSourcePath(dep.getBuildTarget());

    JavaPackageFinder finderStub = createNiceMock(JavaPackageFinder.class);
    expect(finderStub.findJavaPackageFolder((Path) anyObject()))
        .andStubReturn(Paths.get("cheese"));
    expect(finderStub.findJavaPackage((Path) anyObject())).andStubReturn("cheese");

    // No need to verify. It's a stub. I don't care about the interactions.
    EasyMock.replay(finderStub);

    BuildRule lib = JavaLibraryBuilder.createBuilder(BuildTargetFactory.newInstance(
        "//example:target"))
        .addSrc(ruleBased)
        .addSrc(fileBased)
        .build(resolver);

    JavaSourceJar rule = new JavaSourceJar(
        new FakeBuildRuleParamsBuilder("//example:target#src").build(),
        pathResolver,
        lib,
        RuleGatherer.SINGLE_JAR,
        Optional.<Path>empty(),
        Optional.<String>empty());

    BuildContext buildContext = BuildContext.builder()
        .setActionGraph(new ActionGraph(ImmutableList.of()))
        .setJavaPackageFinder(finderStub)
        .setEventBus(BuckEventBusFactory.newInstance())
        .build();
    ImmutableList<Step> steps = rule.getBuildSteps(
        buildContext,
        new FakeBuildableContext());

    // There should be a CopyStep per file being copied. Count 'em.
    int copyStepsCount = FluentIterable.from(steps)
        .filter(CopyStep.class::isInstance)
        .size();

    assertEquals(1, copyStepsCount);
  }
}
