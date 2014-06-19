package org.openqa.selenium.buck.file;


import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.Buildable;
import com.facebook.buck.rules.ConstructorArg;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.SourcePath;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;

public class ZipDescription implements Description<ZipDescription.Arg> {

  public static final BuildRuleType TYPE = new BuildRuleType("zip");

  @Override
  public BuildRuleType getBuildRuleType() {
    return TYPE;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public <A extends Arg> Buildable createBuildable(
      BuildRuleParams params, A args) {
    return new Zip(params.getBuildTarget(), args.srcs);
  }

  public class Arg implements ConstructorArg {
    public ImmutableSortedSet<SourcePath> srcs;

    public Optional<ImmutableSortedSet<BuildRule>> deps;
  }
}
