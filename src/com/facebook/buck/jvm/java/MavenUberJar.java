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

package com.facebook.buck.jvm.java;

import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

import java.nio.file.Path;

import javax.annotation.Nullable;

/**
 * A {@link BuildRule} used to have the provided {@link JavaLibrary} published to a maven repository
 *
 * @see #create
 */
public class MavenUberJar extends AbstractBuildRule implements MavenPublishable {

  private final Optional<String> mavenCoords;
  private final Optional<Path> pomTemplate;
  private final RuleGatherer.GatheredDeps gatheredDeps;

  private MavenUberJar(
      RuleGatherer.GatheredDeps gatheredDeps,
      BuildRuleParams params,
      SourcePathResolver resolver,
      Optional<String> mavenCoords,
      Optional<SourcePath> mavenPomTemplate) {
    super(params, resolver);
    this.gatheredDeps = gatheredDeps;
    this.mavenCoords = mavenCoords;

    this.pomTemplate = mavenPomTemplate.transform(getResolver().getAbsolutePathFunction());
  }

  private static BuildRuleParams adjustParams(BuildRuleParams params, RuleGatherer.GatheredDeps traversedDeps) {
    return params.copyWithDeps(
        Suppliers.ofInstance(
            FluentIterable.from(traversedDeps.getRulesToPackage())
                .toSortedSet(Ordering.<BuildRule>natural())),
        Suppliers.ofInstance(ImmutableSortedSet.<BuildRule>of()));
  }

  /**
   * Will traverse transitive dependencies of {@code rootRule}, separating those that do and don't
   * have maven coordinates. Those that do will be considered maven-external dependencies. They will
   * be returned by {@link #getMavenDeps} and will end up being specified as dependencies in
   * pom.xml. Others will be packaged in the same jar as if they are just a part of the one
   * published item.
   */
  public static MavenUberJar create(
      JavaLibrary rootRule,
      BuildRuleParams params,
      SourcePathResolver resolver,
      Optional<String> mavenCoords,
      Optional<SourcePath> mavenPomTemplate) {
    RuleGatherer.GatheredDeps gatheredDeps = RuleGatherer.MAVEN_JAR.gatherRules(rootRule);
    return new MavenUberJar(
        gatheredDeps,
        adjustParams(params, gatheredDeps),
        resolver,
        mavenCoords,
        mavenPomTemplate);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    Path pathToOutput = getPathToOutput();
    MkdirStep mkOutputDirStep = new MkdirStep(getProjectFilesystem(), pathToOutput.getParent());
    JarDirectoryStep mergeOutputsStep = new JarDirectoryStep(
        getProjectFilesystem(),
        pathToOutput,
        toOutputPaths(gatheredDeps.getRulesToPackage()),
        /* mainClass */ null,
        /* manifestFile */ null);
    return ImmutableList.of(mkOutputDirStep, mergeOutputsStep);
  }

  private static ImmutableSortedSet<Path> toOutputPaths(Iterable<? extends BuildRule> rules) {
    return FluentIterable
          .from(rules)
          .transform(
              new Function<BuildRule, Path>() {
                @Nullable
                @Override
                public Path apply(BuildRule input) {
                  Path pathToOutput = input.getPathToOutput();
                  if (pathToOutput == null) {
                    return null;
                  }
                  return input.getProjectFilesystem().resolve(pathToOutput);
                }
              })
          .filter(Predicates.notNull())
          .toSortedSet(Ordering.<Path>natural());
  }

  @Override
  public Path getPathToOutput() {
    return DefaultJavaLibrary.getOutputJarPath(getBuildTarget(), getProjectFilesystem());
  }

  @Override
  public Optional<Path> getTemplatePom() {
    return pomTemplate;
  }

  @Override
  public Optional<String> getMavenCoords() {
    return mavenCoords;
  }

  @Override
  public Iterable<HasMavenCoordinates> getMavenDeps() {
    return gatheredDeps.getMavenDeps();
  }

  @Override
  public Iterable<BuildRule> getPackagedDependencies() {
    return gatheredDeps.getRulesToPackage();
  }
}
