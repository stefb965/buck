package org.openqa.selenium.buck.file;

import static com.facebook.buck.step.fs.CopyStep.DirectoryMode.DIRECTORY_AND_CONTENTS;

import com.facebook.buck.java.JavacStep;
import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePaths;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.zip.UnzipStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class Zip extends AbstractBuildRule {

  private final Path output;
  private final ImmutableSortedSet<SourcePath> sources;
  private final Path localPath;
  private final Path scratchDir;

  public Zip(BuildRuleParams params, String outputName, ImmutableSortedSet<SourcePath> sources) {
    super(params);
    this.sources = Preconditions.checkNotNull(sources);

    this.output = BuildTargets.getGenPath(getBuildTarget(), outputName);
    this.scratchDir = BuildTargets.getBinPath(getBuildTarget(), "%s.zip.scratch");
    this.localPath = getBuildTarget().getBasePath();
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(sources);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    steps.add(new RmStep(output, true));
    steps.add(new MakeCleanDirectoryStep(scratchDir));

    Set<Path> createdDir = Sets.newHashSet();
    Path basePath = getBuildTarget().getBasePath();
    for (SourcePath source : sources) {
      Path path = source.resolve();

      // Handle a source zip.
      if (path.toString().endsWith(JavacStep.SRC_ZIP)) {
        steps.add(new UnzipStep(path, scratchDir));
        continue;
      }

      Path destination;
      if (path.startsWith(basePath)) {
        // If we're dealing with a file beneath where this rule is executing, copy it based on its
        // relative path.
        destination = scratchDir.resolve(path.relativize(basePath));
      } else {
        // We're dealing with something else entirely. Copy directly into the scratch dir.
        destination = scratchDir.resolve(path.getFileName());
      }

      if (Files.isDirectory(path)) {
        if (createdDir.add(path)) {
          steps.add(new MkdirStep(destination));
        }
        steps.add(CopyStep.forDirectory(path, destination, DIRECTORY_AND_CONTENTS));
      } else {
        if (createdDir.add(path)) {
          steps.add(new MkdirStep(destination.getParent()));
        }
        steps.add(CopyStep.forFile(path, destination));
      }
    }

    steps.add(
        new ZipStep(
            output,
            ImmutableSortedSet.<Path>of(),
            /* junk paths */ false,
            ZipStep.DEFAULT_COMPRESSION_LEVEL,
            scratchDir));

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return null;
  }

  @Override
  public Path getPathToOutputFile() {
    return output;
  }
}
