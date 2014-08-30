package org.openqa.selenium.buck.file;

import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.ConstructorArg;
import com.facebook.buck.rules.Description;
import com.facebook.buck.rules.SourcePath;
import com.facebook.infer.annotation.SuppressFieldNotInitialized;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;

public class FolderDescription implements Description<FolderDescription.Arg> {

  public static final BuildRuleType TYPE = new BuildRuleType("folder");

  @Override
  public BuildRuleType getBuildRuleType() {
    return TYPE;
  }

  @Override
  public Arg createUnpopulatedConstructorArg() {
    return new Arg();
  }

  @Override
  public <A extends Arg> BuildRule createBuildRule(
      BuildRuleParams params,
      BuildRuleResolver resolver,
      A args) {
    return new Folder(params, args.out.or(params.getBuildTarget().getShortName()), args.srcs);
  }

  @SuppressFieldNotInitialized
  public class Arg implements ConstructorArg {
    public Optional<String> out;
    public ImmutableSortedSet<SourcePath> srcs;
  }
}
