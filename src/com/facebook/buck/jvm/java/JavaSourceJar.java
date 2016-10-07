package com.facebook.buck.jvm.java;

import static com.facebook.buck.zip.ZipCompressionLevel.DEFAULT_COMPRESSION_LEVEL;

import com.facebook.buck.jvm.core.JavaPackageFinder;
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
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.nio.file.Path;
import java.util.Set;

public class JavaSourceJar extends AbstractBuildRule implements MavenPublishable {
  private final Path output;
  private final Path temp;
  private final RuleGatherer.GatheredDeps gatheredDeps;
  private final Optional<Path> mavenPomTemplate;
  private final Optional<String> mavenCoords;

  public JavaSourceJar(
      BuildRuleParams params,
      SourcePathResolver resolver,
      BuildRule baseLibrary,
      RuleGatherer gatherer,
      Optional<Path> mavenPomTemplate,
      Optional<String> mavenCoords) {
    super(params, resolver);

    this.gatheredDeps = gatherer.gatherRules(baseLibrary);
    this.mavenPomTemplate = mavenPomTemplate;
    this.mavenCoords = mavenCoords;

    BuildTarget target = params.getBuildTarget();
    this.output =
        BuildTargets.getGenPath(
            getProjectFilesystem(),
            target,
            "%s" + Javac.SRC_JAR);
    this.temp = BuildTargets.getScratchPath(getProjectFilesystem(), target, "%s-srcs");
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    JavaPackageFinder packageFinder = context.getJavaPackageFinder();

    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    steps.add(new MkdirStep(getProjectFilesystem(), output.getParent()));
    steps.add(new RmStep(getProjectFilesystem(), output, /* force deletion */ true));
    steps.add(new MakeCleanDirectoryStep(getProjectFilesystem(), temp));

    Set<Path> seenPackages = Sets.newHashSet();

    // We only want to consider raw source files, since the java package finder doesn't have the
    // smarts to read the "package" line from a source file.

    for (Path source : getResolver().filterInputsToCompareToOutput(getSources())) {
      Path packageFolder = packageFinder.findJavaPackageFolder(source);
      Path packageDir = temp.resolve(packageFolder);
      if (seenPackages.add(packageDir)) {
        steps.add(new MkdirStep(getProjectFilesystem(), packageDir));
      }
      steps.add(
          CopyStep.forFile(
              getProjectFilesystem(),
              source,
              packageDir.resolve(source.getFileName())));
    }
    steps.add(
        new ZipStep(
            getProjectFilesystem(),
            output,
            ImmutableSet.<Path>of(),
            /* junk paths */ false,
            DEFAULT_COMPRESSION_LEVEL,
            temp));

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  @Override
  public Path getPathToOutput() {
    return output;
  }

  @Override
  public Iterable<HasMavenCoordinates> getMavenDeps() {
    return gatheredDeps.getMavenDeps();
  }

  @Override
  public Iterable<BuildRule> getPackagedDependencies() {
    return gatheredDeps.getRulesToPackage();
  }

  @Override
  public Optional<Path> getPomTemplate() {
    return mavenPomTemplate;
  }

  @Override
  public Optional<String> getMavenCoords() {
    return mavenCoords;
  }

  private ImmutableSortedSet<SourcePath> getSources() {
    return FluentIterable.from(gatheredDeps.getRulesToPackage())
        .filter(HasSources.class)
        .transformAndConcat(new Function<HasSources, Iterable<SourcePath>>() {
          @Override
          public Iterable<SourcePath> apply(HasSources input) {
            return input.getSources();
          }
        })
        .toSortedSet(Ordering.<SourcePath>natural());
  }
}
