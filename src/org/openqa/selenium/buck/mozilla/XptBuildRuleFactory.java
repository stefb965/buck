package org.openqa.selenium.buck.mozilla;

import com.facebook.buck.parser.NoSuchBuildTargetException;
import com.facebook.buck.rules.AbstractBuildRuleBuilderParams;
import com.facebook.buck.rules.AbstractBuildRuleFactory;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildRuleFactoryParams;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildRuleType;
import com.facebook.buck.rules.Buildable;
import com.facebook.buck.rules.SourcePath;

import java.nio.file.Path;
import java.nio.file.Paths;

public class XptBuildRuleFactory extends AbstractBuildRuleFactory<XptBuildRuleFactory.Builder> {

  public final static BuildRuleType XPT = new BuildRuleType("mozilla_xpt");

  @Override
  protected Builder newBuilder(AbstractBuildRuleBuilderParams params) {
    return new Builder(params);
  }

  @Override
  protected void amendBuilder(Builder builder, BuildRuleFactoryParams params) throws NoSuchBuildTargetException {
    String src = params.getRequiredStringAttribute("src");
    src = params.resolveFilePathRelativeToBuildFileDirectory(src);
    builder.setSource(Paths.get(src));

    String fallback = params.getRequiredStringAttribute("fallback");
    SourcePath sourcePath = params.asSourcePath(fallback, builder);
    builder.setFallback(sourcePath);
  }

  static class Builder extends AbstractBuildable.Builder {

    private Path source;
    private SourcePath fallback;

    protected Builder(AbstractBuildRuleBuilderParams params) {
      super(params);
    }

    @Override
    protected BuildRuleType getType() {
      return XPT;
    }

    public void setSource(Path source) {
      this.source = source;
    }

    public void setFallback(SourcePath fallback) {
      this.fallback = fallback;
    }

    @Override
    protected Buildable newBuildable(BuildRuleParams params, BuildRuleResolver resolver) {
      return new Xpt(params, source, fallback);
    }
  }
}
