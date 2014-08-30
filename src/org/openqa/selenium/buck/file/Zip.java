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
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Path;

public class Zip extends AbstractBuildRule {

  private final Path output;
  private final ImmutableSortedSet<SourcePath> sources;
  private final Path scratchDir;

  public Zip(BuildRuleParams params, String outputName, ImmutableSortedSet<SourcePath> sources) {
    super(params);
    this.sources = Preconditions.checkNotNull(sources);

    this.output = BuildTargets.getGenPath(getBuildTarget(), outputName);
    this.scratchDir = BuildTargets.getBinPath(getBuildTarget(), "%s.zip.scratch");
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

    SrcZipAwareFileBundler bundler = new SrcZipAwareFileBundler(getBuildTarget());
    bundler.copy(steps, scratchDir, sources, false);

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
