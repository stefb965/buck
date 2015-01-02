/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.openqa.selenium.buck.javascript;

import com.facebook.buck.model.BuildTargets;
import com.facebook.buck.rules.AbstractBuildRule;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildableContext;
import com.facebook.buck.rules.RuleKey;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.shell.ShellStep;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.WriteFileStep;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.Nullable;

public class JsFragment extends AbstractBuildRule {

  private final Path output;
  private final Path temp;
  private final Path compiler;
  private final ImmutableSortedSet<BuildRule> deps;
  private final String module;
  private final String function;

  public JsFragment(
      BuildRuleParams params,
      SourcePathResolver resolver,
      Path compiler,
      ImmutableSortedSet<BuildRule> deps,
      String module,
      String function) {
    super(params, resolver);

    this.deps = deps;
    this.module = module;
    this.function = function;
    this.output = BuildTargets.getGenPath(getBuildTarget(), "%s.js");
    this.temp = BuildTargets.getBinPath(getBuildTarget(), "%s-temp.js");
    this.compiler = compiler;
  }

  @Override
  public ImmutableCollection<Path> getInputsToCompareToOutput() {
    // All the inputs are defined by our dependencies.
    return ImmutableSet.of();
  }

  @Override
  public ImmutableList<Step> getBuildSteps(
      BuildContext context,
      BuildableContext buildableContext) {
    ImmutableList.Builder<Step> steps = ImmutableList.builder();

    JavascriptDependencyGraph graph = new JavascriptDependencyGraph();

    for (BuildRule dep : deps) {
      if (!(dep instanceof JsLibrary)) {
        continue;
      }

      Set<JavascriptSource> allDeps = ((JsLibrary) dep).getBundleOfJoy().getDeps(module);
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

    buildableContext.recordArtifact(output);

    return steps.build();
  }

  @Override
  public RuleKey.Builder appendDetailsToRuleKey(RuleKey.Builder builder) {
    return builder;
  }

  @Nullable
  @Override
  public Path getPathToOutputFile() {
    return output;
  }

  private class JavascriptFragmentStep extends ShellStep {

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
              "navigator:typeof window!='undefined'?window.navigator:null," +
              "document:typeof window!='undefined'?window.document:null" +
              "}, arguments);}", wrapper);

      cmd.add("java", "-jar", compiler.toAbsolutePath().toString());
      cmd.add(
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
