package org.openqa.selenium.buck.file;

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
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Zip extends AbstractBuildRule {

  private final Path output;
  private final ImmutableSortedSet<SourcePath> sources;
  private final Path localPath;
  private final Path scratchDir;

  public Zip(BuildRuleParams params, ImmutableSortedSet<SourcePath> sources) {
    super(params);
    this.sources = Preconditions.checkNotNull(sources);

    this.output = BuildTargets.getGenPath(getBuildTarget(), "%s.zip");
    this.scratchDir = BuildTargets.getBinPath(getBuildTarget(), "%s.zip.scratch");
    this.localPath = Paths.get(getBuildTarget().getBasePath());
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(sources);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    ImmutableList.Builder<Step> commands = ImmutableList.builder();

    commands.add(new MakeCleanDirectoryStep(output.getParent()));
    commands.add(new MakeCleanDirectoryStep(scratchDir));

    Set<Path> createdDir = Sets.newHashSet();
    for (SourcePath source : sources) {
      Path path = source.resolve();

      Path localName = localPath.relativize(path);

      Path destination = scratchDir.resolve(localName);

      if (Files.isDirectory(path)) {
        if (createdDir.add(path)) {
          commands.add(new MkdirStep(destination));
        }
      } else {
        if (createdDir.add(path)) {
          commands.add(new MkdirStep(destination));
        }
        commands.add(CopyStep.forFile(path, destination));
      }
    }

    commands.add(new ZipStep(
            output,
            ImmutableSortedSet.<Path>of(),
            /* junk paths */ false,
            ZipStep.DEFAULT_COMPRESSION_LEVEL,
            scratchDir));

    buildableContext.recordArtifact(output);

    return commands.build();
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
