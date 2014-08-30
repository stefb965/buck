package org.openqa.selenium.buck.file;

import static com.facebook.buck.zip.ZipStep.DEFAULT_COMPRESSION_LEVEL;

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
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.RmStep;
import com.facebook.buck.step.fs.SymlinkTreeStep;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.buck.zip.UnzipStep;
import com.facebook.buck.zip.ZipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Folder extends AbstractBuildRule {
  private final Path folderName;
  private final Path output;
  private final ImmutableSortedSet<SourcePath> srcs;

  protected Folder(
      BuildRuleParams buildRuleParams,
      String folderName,
      ImmutableSortedSet<SourcePath> srcs) {
    super(buildRuleParams);

    this.folderName = Preconditions.checkNotNull(Paths.get(folderName));
    this.output = BuildTargets.getGenPath(getBuildTarget(),  "%s.src.zip");
    this.srcs = Preconditions.checkNotNull(srcs);
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context, BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    steps.add(new RmStep(output, true));
    steps.add(new MkdirStep(output.getParent()));

    Path scratch = BuildTargets.getBinPath(getBuildTarget(), "%s-scratch/" + folderName);
    steps.add(new MakeCleanDirectoryStep(scratch));

    Path basePath = getBuildTarget().getBasePath();
    // Keyed on destination, with the value being the Real Thing.
    ImmutableMap.Builder<Path, Path> linkMap = ImmutableMap.builder();
    for (SourcePath src : srcs) {
      Path resolved = src.resolve();

      if (resolved.toString().endsWith(JavacStep.SRC_ZIP)) {
        steps.add(new UnzipStep(resolved, scratch));
        continue;
      }

      Path destination;
      if (resolved.startsWith(basePath)) {
        destination = basePath.relativize(resolved);
      } else {
        destination = resolved.getFileName();
      }
      if (Files.isDirectory(resolved)) {
        throw new HumanReadableException("Please expand directory entries using glob()");
      } else {
        linkMap.put(destination, resolved);
      }
    }
    steps.add(new SymlinkTreeStep(scratch, linkMap.build()));
    steps.add(new ZipStep(
            output,
            ImmutableSet.<Path>of(),
            false,
            DEFAULT_COMPRESSION_LEVEL,
            scratch.getParent()));

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  @Override
  public Path getPathToOutputFile() {
    return output;
  }

  @Override
  protected Iterable<Path> getInputsToCompareToOutput() {
    return SourcePaths.filterInputsToCompareToOutput(srcs);
  }

  @Override
  protected RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder;
  }
}
