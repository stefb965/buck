package com.facebook.buck.util.versioncontrol;

import com.facebook.buck.log.Logger;
import com.facebook.buck.util.ProcessExecutor;
import com.facebook.buck.util.ProcessExecutorFactory;
import com.facebook.buck.util.ProcessExecutorParams;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

public class GitCmdLineInterface implements VersionControlCmdLineInterface {

  private final static Logger LOG = Logger.get(GitCmdLineInterface.class);

  private final ProcessExecutorFactory processExecutorFactory;
  private final Path projectRoot;
  private final String gitCmd;
  private final ImmutableMap<String, String> environment;

  public GitCmdLineInterface(
      ProcessExecutorFactory processExecutorFactory,
      Path projectRoot,
      String gitCmd,
      ImmutableMap<String, String> environment) {
    this.processExecutorFactory = processExecutorFactory;
    this.projectRoot = projectRoot;
    this.gitCmd = gitCmd;
    this.environment = environment;
  }

  @Override
  public String getHumanReadableName() {
    return "Git";
  }

  @Override
  public boolean isSupportedVersionControlSystem() {
    return true;  // Sure. Let's say that Git is supported.
  }

  @Override
  public String revisionId(String name)
      throws VersionControlCommandFailedException, InterruptedException {
    return executeCommand(gitCmd, "rev-parse", "--abbrev-ref", "HEAD");
  }

  @Override
  public String currentRevisionId()
      throws VersionControlCommandFailedException, InterruptedException {
    return executeCommand(gitCmd, "rev-parse", "--short", "HEAD", "--");
  }

  @Override
  public String commonAncestor(
      String revisionIdOne,
      String revisionIdTwo) throws VersionControlCommandFailedException, InterruptedException {
    return executeCommand(
        gitCmd,
        "merge-base",
        getRevisionId(revisionIdOne),
        getRevisionId(revisionIdTwo));
  }

  @Override
  public ImmutableSet<String> changedFiles(String fromRevisionId)
      throws VersionControlCommandFailedException, InterruptedException {
    String rawData = executeCommand(
        gitCmd,
        "diff",
        "--name-only",
        getRevisionId(fromRevisionId),
        "HEAD");
    return ImmutableSet.copyOf(Splitter.on(File.separator).omitEmptyStrings().split(rawData));
  }

  @Override
  public long timestampSeconds(String revisionId)
      throws VersionControlCommandFailedException, InterruptedException {
    String rawResult = executeCommand(
        gitCmd,
        "log",
        "--pretty=format:%ct",
        "-1",
        getRevisionId(revisionId),
        "--");
    try {
      return Long.parseLong(rawResult);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private String getRevisionId(String possibleMercurialRef) {
    if (".".equals(possibleMercurialRef)) {
      return "HEAD";
    }
    return possibleMercurialRef;
  }

  private String executeCommand(String... command)
      throws VersionControlCommandFailedException, InterruptedException {
    String commandString = Joiner.on(" ").join(command);
    LOG.debug("Executing command: " + commandString);

    ProcessExecutorParams processExecutorParams = ProcessExecutorParams.builder()
        .setCommand(Lists.newArrayList(command))
        .setDirectory(projectRoot)
        .setEnvironment(environment)
        .build();

    ProcessExecutor.Result result;
    try (
        PrintStream stdout = new PrintStream(new ByteArrayOutputStream());
        PrintStream stderr = new PrintStream(new ByteArrayOutputStream())) {

      ProcessExecutor processExecutor =
          processExecutorFactory.createProcessExecutor(stdout, stderr);

      result = processExecutor.launchAndExecute(processExecutorParams);
    } catch (IOException e) {
      throw new VersionControlCommandFailedException(e);
    }

    Optional<String> resultString = result.getStdout();

    if (!resultString.isPresent()) {
      throw new VersionControlCommandFailedException(
          "Received no output from launched process for command: " + commandString
      );
    }

    if (result.getExitCode() != 0) {
      Optional<String> stderr = result.getStderr();
      String stdErrString = stderr.isPresent() ? stderr.get() : "";
      throw new VersionControlCommandFailedException(
          "Received non-zero exit for for command:" + commandString +
              "\nStdErr: " + stdErrString
      );
    }

    return cleanResultString(resultString.get());
  }

  private static String cleanResultString(String result) {
    return result.trim().replace("\'", "").replace("\n", "");
  }
}
