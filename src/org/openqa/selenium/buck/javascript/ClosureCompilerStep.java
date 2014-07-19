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


import com.facebook.buck.shell.ShellStep;
import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ClosureCompilerStep extends ShellStep {

  public static final String COMPILER = "third_party/closure/bin/compiler-20140407.jar";
  private final Path output;
  private final ImmutableList<String> cmd;

  private ClosureCompilerStep(Path output, ImmutableList<String> cmd) {
    this.output = Preconditions.checkNotNull(output);
    this.cmd = cmd;
  }

  @Override
  protected ImmutableList<String> getShellCommandInternal(ExecutionContext context) {
    return cmd;
  }

  @Override
  public String getShortName() {
    return "closure compiler";
  }

  @Override
  public int execute(ExecutionContext context) throws InterruptedException {
    int exitCode = super.execute(context);

    if (exitCode == 0) {
      return exitCode;
    }
    File file = output.toFile();
    if (file.exists() && !file.delete()) {
      throw new HumanReadableException(
          "Unable to delete output, which may lead to incorrect builds: " + output);
    }
    return exitCode;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ImmutableList.Builder<String> cmd = ImmutableList.builder();
    private Path output;

    public Builder() {
      cmd.add("java", "-jar", COMPILER);
      cmd.add("--third_party=true");
    }

    public Builder defines(Optional<List<String>> defines) {
      for (String define : defines.or(ImmutableList.<String>of())) {
        cmd.add("--define=" + define);
      }
      return this;
    }

    public Builder externs(Optional<List<Path>> externs) {
      for (Path path : externs.or(ImmutableList.<Path>of())) {
        cmd.add("--externs='" + path.normalize() + "'");
      }
      return this;
    }

    public Builder flags(Optional<List<String>> flags) {
      for (String flag : flags.or(ImmutableList.<String>of())) {
        cmd.add(flag);
      }
      return this;
    }

    public Builder prettyPrint() {
      cmd.add("--formatting=PRETTY_PRINT");
      return this;
    }

    public Builder sources(Iterable<Path> paths) {
      for (Path path : paths) {
        cmd.add("--js='" + path + "'");
      }
      return this;
    }

    public Builder output(Path out) {
      this.output = out;
      cmd.add("--js_output_file=" + out);
      return this;
    }

    public ClosureCompilerStep build() {
      return new ClosureCompilerStep(output, cmd.build());
    }
  }
}
