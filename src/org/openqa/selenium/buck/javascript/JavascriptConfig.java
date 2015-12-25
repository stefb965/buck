/*
 * Copyright 2015-present Facebook, Inc.
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

import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.rules.BinaryBuildRule;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.HashedFileTool;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.Tool;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class JavascriptConfig {

  private final BuckConfig delegate;

  public JavascriptConfig(BuckConfig delegate) {
    this.delegate = delegate;
  }

  public Tool getClosureCompiler(Optional<SourcePath> compilerPath, SourcePathResolver resolver) {
    SourcePath path = getClosureCompilerSourcePath(compilerPath);
    Optional<BuildRule> rule = resolver.getRule(path);
    if (rule.isPresent()) {
      Preconditions.checkState(
          rule.get() instanceof BinaryBuildRule,
          "Closure compiler must be a binary build rule");
      return ((BinaryBuildRule) rule.get()).getExecutableCommand();
    }

    return new HashedFileTool(resolver.getAbsolutePath(path));
  }

  public SourcePath getClosureCompilerSourcePath(Optional<SourcePath> compilerPath) {
    return compilerPath.or(delegate.getRequiredSourcePath("tools", "closure_compiler"));
  }
}

