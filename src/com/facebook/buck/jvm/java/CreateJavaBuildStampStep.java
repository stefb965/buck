package com.facebook.buck.jvm.java;

import com.facebook.buck.step.ExecutionContext;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.StepExecutionResult;
import com.facebook.buck.util.versioncontrol.BuildStamp;
import com.facebook.buck.util.versioncontrol.BuildStamper;
import com.google.common.base.Preconditions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class CreateJavaBuildStampStep implements Step {
  private final Path output;

  CreateJavaBuildStampStep(Path output) {
    Preconditions.checkState(output.isAbsolute(), "Output path must be absolute: %s", output);
    this.output = Preconditions.checkNotNull(output);
  }

  @Override
  public StepExecutionResult execute(ExecutionContext context) throws IOException, InterruptedException {
    BuildStamper stamper = context.getBuildStamper();

    BuildStamp stamp = stamper.getBuildStamp();

    String versionTime = format(stamp.getVersionTimestamp());
    String buildTime = format(stamp.getBuildTimestamp());

    Properties properties = new Properties();
    properties.put("build-revision", stamp.getRevision());
    properties.put("build-revision-timestamp", versionTime);
    properties.put("build-timestamp", buildTime);

    try (BufferedWriter writer = Files.newBufferedWriter(output)){
      properties.store(writer, "Buck build info");
    } catch (IOException e) {
      context.logError(e, "Failed to create build stamp: %s", getDescription(context));
      return StepExecutionResult.ERROR;
    }

    return StepExecutionResult.SUCCESS;
  }

  private String format(long timestamp) {
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    return DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
  }

  @Override
  public String getShortName() {
    return "build stamp";
  }

  @Override
  public String getDescription(ExecutionContext context) {
    return "build stamp";
  }
}
