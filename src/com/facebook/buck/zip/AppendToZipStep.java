/*
 * Copyright 2017-present Facebook, Inc.
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

package com.facebook.buck.zip;

import static com.facebook.buck.zip.ZipOutputStreams.HandleDuplicates.APPEND_TO_ZIP;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.StepExecutionResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppendToZipStep implements Step {

  private final Path zipFile;
  private final ImmutableMap<Path, String> paths;

  public AppendToZipStep(Path zipFile, ImmutableMap<Path, String> paths) {
    Preconditions.checkState(zipFile.isAbsolute(), "Zip file must be absolute: %s", zipFile);

    this.zipFile = zipFile;
    this.paths = paths;
  }

  @Override
  public StepExecutionResult execute(ExecutionContext context)
      throws IOException, InterruptedException {
    if (!Files.exists(zipFile)) {
      return StepExecutionResult.ERROR;
    }

    // It would be efficient to do this in-place, but let's be inefficient for now
    Path updated = Files.createTempFile("buck", "stamp.jar");

    try (CustomZipOutputStream zos = ZipOutputStreams.newOutputStream(updated, APPEND_TO_ZIP);
         ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
      for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
        if (!(paths.values().contains(entry.getName()))) {
          zos.putNextEntry(entry);
          ByteStreams.copy(zis, zos);
        }
      }

      for (Map.Entry<Path, String> path : paths.entrySet()) {
        Preconditions.checkState(path.getKey().isAbsolute());
        ZipEntry entry = new ZipEntry(path.getValue());
        entry.setTime(ZipConstants.getFakeTime());
        zos.putNextEntry(entry);
        Files.copy(path.getKey(), zos);
      }
    }

    Files.move(updated, zipFile, REPLACE_EXISTING);

    return StepExecutionResult.SUCCESS;
  }

  @Override
  public String getShortName() {
    return "append to zip";
  }

  @Override
  public String getDescription(ExecutionContext context) {
    return "append to zip";
  }
}
