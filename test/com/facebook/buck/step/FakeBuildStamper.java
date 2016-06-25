package com.facebook.buck.step;

import com.facebook.buck.util.versioncontrol.NoOpCmdLineInterface;
import com.facebook.buck.util.versioncontrol.VersionControlCmdLineInterface;
import com.facebook.buck.util.versioncontrol.VersionControlCmdLineInterfaceFactory;

public class FakeBuildStamper extends BuildStamper {
  public FakeBuildStamper() {
    super(new VersionControlCmdLineInterfaceFactory() {
      @Override
      public VersionControlCmdLineInterface createCmdLineInterface() throws InterruptedException {
        return new NoOpCmdLineInterface();
      }
    });
  }
}
