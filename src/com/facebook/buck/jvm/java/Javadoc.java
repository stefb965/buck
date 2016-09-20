package com.facebook.buck.jvm.java;

import static com.facebook.buck.zip.ZipCompressionLevel.DEFAULT_COMPRESSION_LEVEL;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.zip.ZipCompressionLevel;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Javadoc extends AbstractBuildRule implements MavenPublishable {

  private final Path output;
  private final Optional<String> mavenCoords;
  private final Optional<Path> mavenPomTemplate;
  private final RuleGatherer.GatheredDeps gatheredDeps;

  protected Javadoc(
      BuildRule javaLibrary,
      BuildRuleParams params,
      SourcePathResolver resolver,
      Optional<String> mavenCoords,
      Optional<Path> mavenPomTemplate,
      RuleGatherer gatherer) {
    super(params, resolver);

    BuildTarget target = params.getBuildTarget();
    this.output =
        BuildTargets.getGenPath(
            getProjectFilesystem(),
            target,
            String.format("%%s%s", Javac.JAVADOC_JAR));
    this.mavenCoords = mavenCoords;
    this.mavenPomTemplate = mavenPomTemplate;

    this.gatheredDeps = gatherer.gatherRules(javaLibrary);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {

    ImmutableSet<SourcePath> sources = FluentIterable.from(gatheredDeps.getRulesToPackage())
        .filter(HasSources.class)
        .transformAndConcat(new Function<HasSources, Iterable<SourcePath>>() {
          @Override
          public Iterable<SourcePath> apply(HasSources input) {
            return input.getSources();
          }
        })
        .toSortedSet(Ordering.<SourcePath>natural());

    buildableContext.recordArtifact(output);
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    Path javadocOutput = output.getParent().resolve("javadoc");
    steps.add(new RmStep(getProjectFilesystem(), javadocOutput, /* force deletion */ true, true));
    steps.add(new RmStep(getProjectFilesystem(), output, true));
    steps.add(new MkdirStep(getProjectFilesystem(), javadocOutput));

    if (sources.isEmpty()) {
      // Create an empty jar. This keeps things that want a jar happy without causing javadoc to
      // choke on there not being any sources.
      steps.add(
          new ZipStep(
              getProjectFilesystem(),
              output,
              ImmutableSet.<Path>of(),
              /* junk paths */ false,
              ZipCompressionLevel.MIN_COMPRESSION_LEVEL,
              javadocOutput));
      return steps.build();
    }

    List<String> javaDocArgs = new LinkedList<>();
    javaDocArgs.addAll(
        Arrays.asList(
            "-Xdoclint:none",
            "-notimestamp",
            "-private",
            "-subpackages", ".",
            "-d", javadocOutput.toString()));

    javaDocArgs.addAll(
        FluentIterable.from(sources)
            .transform(getResolver().getAbsolutePathFunction())
            .transform(Functions.toStringFunction())
            .toSortedSet(Ordering.<String>natural()));

    ImmutableSortedSet.Builder<Path> allJars = ImmutableSortedSet.naturalOrder();
    for (JavaLibrary dep : gatheredDeps.getTransitiveClasspath()) {
      allJars.addAll(dep.getTransitiveClasspaths());
    }

    steps.add(new JavaDocStep(Joiner.on(File.pathSeparator).join(allJars.build()), javaDocArgs));

    steps.add(
        new ZipStep(
            getProjectFilesystem(),
            output,
            ImmutableSet.<Path>of(),
            /* junk paths */ false,
            DEFAULT_COMPRESSION_LEVEL,
            javadocOutput));

    return steps.build();
  }

  @Override
  public Path getPathToOutput() {
    return output;
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
  public Optional<Path> getPomTemplate() {
    return mavenPomTemplate;
  }

  @Override
  public Iterable<BuildRule> getPackagedDependencies() {
    return gatheredDeps.getRulesToPackage();
  }
}
