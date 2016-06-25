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

package com.facebook.buck.util.versioncontrol;

import com.facebook.buck.log.Logger;
import com.facebook.buck.util.ProcessExecutorFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

public class DefaultVersionControlCmdLineInterfaceFactory
    implements VersionControlCmdLineInterfaceFactory {
  private static final Logger LOG = Logger.get(DefaultVersionControlCmdLineInterfaceFactory.class);

  private final Path projectRoot;
  private final ProcessExecutorFactory processExecutorFactory;
  private final String gitCmd;
  private final String hgCmd;
  private final ImmutableMap<String, String> environment;

  public DefaultVersionControlCmdLineInterfaceFactory(
      Path projectRoot,
      ProcessExecutorFactory processExecutorFactory,
      VersionControlBuckConfig buckConfig,
      ImmutableMap<String, String> environment) {
    this.projectRoot = projectRoot;
    this.processExecutorFactory = processExecutorFactory;
    this.hgCmd = buckConfig.getHgCmd();
    this.gitCmd = buckConfig.getGitCmd();
    this.environment = environment;
  }

  @Override
  public VersionControlCmdLineInterface createCmdLineInterface() throws InterruptedException {
    return findWorkableVcs(
        new NoOpCmdLineInterface(),
        new GitCmdLineInterface(processExecutorFactory, projectRoot, gitCmd, environment),
        new HgCmdLineInterface(processExecutorFactory, projectRoot, hgCmd, environment));
  }

  private VersionControlCmdLineInterface findWorkableVcs(
      VersionControlCmdLineInterface fallback,
      VersionControlCmdLineInterface... possibilities) throws InterruptedException {

    Set<String> vcsNames = new TreeSet<>();
    for (VersionControlCmdLineInterface vcs : possibilities) {
      try {
        vcs.currentRevisionId();
        LOG.debug("Using " + vcs.getHumanReadableName());
        return vcs;
      } catch (VersionControlCommandFailedException e) {
        vcsNames.add(vcs.getHumanReadableName());
      }
    }

    LOG.warn("The only supported VCSs are: %s, however the " +
        "current project (which has enabled VCS stats generation in its .buckconfig) " +
        "does not appear to any of those", Joiner.on(", ").join(vcsNames));

    LOG.debug("Using NoOpCmdLineInterface.");
    return fallback;
  }
}
