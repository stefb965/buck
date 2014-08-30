package org.openqa.selenium.buck.file;

import com.facebook.buck.java.JavacStep;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.SymlinkTreeStep;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.buck.zip.UnzipStep;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class SrcZipAwareFileBundler {

  private final Path basePath;

  public SrcZipAwareFileBundler(BuildTarget target) {
    this(target.getBasePath());
  }

  public SrcZipAwareFileBundler(Path basePath) {
    this.basePath = Preconditions.checkNotNull(basePath);
  }

  public void copy(
      ImmutableList.Builder<Step> steps,
      Path destinationDir,
      Iterable<SourcePath> toCopy,
      boolean junkPaths) {

    ImmutableMap.Builder<Path, Path> links = ImmutableMap.builder();

    for (SourcePath sourcePath : toCopy) {
      Path resolved = sourcePath.resolve();

      if (resolved.toString().endsWith(JavacStep.SRC_ZIP)) {
        steps.add(new UnzipStep(resolved, destinationDir));
      }

      if (Files.isDirectory(resolved)) {
        throw new HumanReadableException("Cowardly refusing to copy a directory: " + resolved);
      }

      Path destination;
      if (!junkPaths && resolved.startsWith(basePath)) {
        destination = basePath.relativize(resolved);
      } else {
        destination = resolved.getFileName();
      }
      links.put(destination, resolved);
    }
    steps.add(new SymlinkTreeStep(destinationDir, links.build()));
  }
}
