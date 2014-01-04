package org.openqa.selenium.buck.javascript;

import static com.facebook.buck.util.BuckConstant.BIN_DIR;
import static com.facebook.buck.util.BuckConstant.GEN_DIR;

import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.AbstractBuildable;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.Buildable;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.shell.ShellStep;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.WriteFileStep;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public class JsFragment extends AbstractBuildable {

  private final Path output;
  private final Path temp;
  private final ImmutableSortedSet<BuildRule> deps;
  private final String module;
  private final String function;
  private final boolean minify;

  public JsFragment(
      BuildTarget target,
      ImmutableSortedSet<BuildRule> deps,
      String module,
      String function,
      boolean minify) {
    this.deps = deps;
    this.module = module;
    this.function = function;
    this.minify = minify;
    this.output = Paths.get(
        GEN_DIR, target.getBaseName(), target.getShortName() + "-fragment.js");
    this.temp = Paths.get(
        BIN_DIR, target.getBaseName(), target.getShortName() + "-temp.js");
  }

  @Override
  public Iterable<String> getInputsToCompareToOutput() {
    // All the inputs are defined by our dependencies.
    return ImmutableSet.of();
  }

  @Override
  public List<Step> getBuildSteps(BuildContext context, BuildableContext buildableContext) throws IOException {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    JavascriptDependencyGraph graph = new JavascriptDependencyGraph();

    for (BuildRule dep : deps) {
      Buildable buildable = dep.getBuildable();
      if (!(buildable instanceof JsLibrary)) {
        continue;
      }

      Set<JavascriptSource> allDeps = ((JsLibrary) buildable).getBundleOfJoy().getDeps(module);
      if (!allDeps.isEmpty()) {
        graph.amendGraph(allDeps);
        break;
      }
    }

    steps.add(new MkdirStep(temp.getParent()));
    steps.add(new WriteFileStep(
        String.format("goog.require('%s'); goog.exportSymbol('_', %s);", module, function),
        temp));
    steps.add(new MkdirStep(output.getParent()));
    steps.add(new JavascriptFragmentStep(temp, output, graph.sortSources()));

    return steps.build();
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) throws IOException {
    return builder;
  }

  @Nullable
  @Override
  public String getPathToOutputFile() {
    return null;
  }

  private static class JavascriptFragmentStep extends ShellStep {

    private final Iterable<Path> jsDeps;
    private final Path temp;
    private final Path output;

    public JavascriptFragmentStep(Path temp, Path output, Iterable<Path> jsDeps) {
      this.temp = temp;
      this.output = output;
      this.jsDeps = jsDeps;
    }

    @Override
    protected ImmutableList<String> getShellCommandInternal(ExecutionContext context) {
      ImmutableList.Builder<String> cmd = ImmutableList.builder();

      String wrapper = "function(){%output%; return this._.apply(null,arguments);}";
      wrapper = String.format(
          "function(){return %s.apply({" +
              "navigator:typeof window!='undefined'?window.navigator:null,document:typeof window!='undefined'?window.document:null" +
              "}, arguments);}", wrapper);

      cmd.add("java", "-jar", ClosureCompilerStep.COMPILER);
      cmd.add(
          "--create_name_map_files=true",
          "--third_party=false",
          String.format("--js_output_file='%s'", output),
          String.format("--output_wrapper='%s'", wrapper),
          "--compilation_level=ADVANCED_OPTIMIZATIONS",
          "--define=goog.NATIVE_ARRAY_PROTOTYPES=false",
          "--define=bot.json.NATIVE_JSON=false",

          "--jscomp_off=unknownDefines",
          "--jscomp_off=deprecated",
          "--jscomp_error=accessControls",
          "--jscomp_error=ambiguousFunctionDecl",
          "--jscomp_error=checkDebuggerStatement",
          "--jscomp_error=checkRegExp",
          "--jscomp_error=checkTypes",
          "--jscomp_error=checkVars",
          "--jscomp_error=const",
          "--jscomp_error=constantProperty",
          "--jscomp_error=duplicate",
          "--jscomp_error=duplicateMessage",
          "--jscomp_error=externsValidation",
          "--jscomp_error=fileoverviewTags",
          "--jscomp_error=globalThis",
          "--jscomp_error=internetExplorerChecks",
          "--jscomp_error=invalidCasts",
          "--jscomp_error=missingProperties",
          "--jscomp_error=nonStandardJsDocs",
          "--jscomp_error=strictModuleDepCheck",
          "--jscomp_error=typeInvalidation",
          "--jscomp_error=undefinedNames",
          "--jscomp_error=undefinedVars",
          "--jscomp_error=uselessCode",
          "--jscomp_error=visibility"
      );

      for (Path dep : jsDeps) {
        cmd.add("--js='" + dep + "'");
        if (dep.endsWith("closure/goog/base.js")) {
          // Always load closure's deps.js first to "forward declare" all of the Closure types. This
          // prevents type errors when a symbol is referenced in a file's type annotation, but not
          // actually needed in the compiled output.
          cmd.add("--js='third_party/closure/goog/deps.js'");
        }
      }
      cmd.add("--js='" + temp + "'");

      return cmd.build();
    }

    @Override
    public String getShortName() {
      return "compile js fragment";
    }
  }
}
