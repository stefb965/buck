package org.openqa.selenium.buck.mozilla;

import com.facebook.buck.event.LogEvent;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.CopyStep;
import com.facebook.buck.step.fs.MakeCleanDirectoryStep;
import com.facebook.buck.util.BuckConstant;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Xpt extends AbstractBuildable {

  private final SourcePath fallback;
  private final Path src;
  private final Path out;

  public Xpt(BuildRuleParams params, Path src, SourcePath fallback) {
    this.fallback = Preconditions.checkNotNull(fallback);
    this.src = Preconditions.checkNotNull(src);
    String name = Files.getNameWithoutExtension(src.getFileName().toString()) + ".xpt";

    this.out = Paths.get(BuckConstant.GEN_DIR, params.getBuildTarget().getBasePath(), name);
  }

  @Override
  public Iterable<String> getInputsToCompareToOutput() {
    return ImmutableSortedSet.of(src.toString());
  }

  @Override
  public List<Step> getBuildSteps(BuildContext context, BuildableContext buildableContext)
      throws IOException {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    context.getEventBus().post(LogEvent.warning("Defaulting to fallback for " + out));
    Path from = fallback.resolve(context);

    steps.add(new MakeCleanDirectoryStep(out.getParent()));
    steps.add(new CopyStep(from, out));

    return steps.build();
  }

  @Override
  public String getPathToOutputFile() {
    return out.toString();
  }
}
