package com.facebook.buck.step;

import com.facebook.buck.timing.DefaultClock;
import com.facebook.buck.util.versioncontrol.VersionControlCmdLineInterface;
import com.facebook.buck.util.versioncontrol.VersionControlCmdLineInterfaceFactory;
import com.facebook.buck.util.versioncontrol.VersionControlCommandFailedException;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class BuildStamper {

  private final VersionControlCmdLineInterfaceFactory vcsFactory;
  private final Supplier<BuildStamp> stamp = Suppliers.memoize(new Supplier<BuildStamp>() {
    @Override
    public BuildStamp get() {
      try {
        VersionControlCmdLineInterface vcs = vcsFactory.createCmdLineInterface();
        String revisionId = vcs.currentRevisionId();
        long revisionTimestamp = vcs.timestampSeconds(revisionId);
        long currentTimestamp = new DefaultClock().currentTimeMillis();

        return BuildStamp.builder()
            .setRevision(revisionId)
            .setVersionTimestamp(revisionTimestamp)
            .setBuildTimestamp(currentTimestamp)
            .build();
      } catch (VersionControlCommandFailedException e) {
        return BuildStamp.builder()
            .setRevision("UNKNOWN")
            .setVersionTimestamp(0)
            .setBuildTimestamp(0)
            .build();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  });

  public BuildStamper(VersionControlCmdLineInterfaceFactory vcsFactory) {
    this.vcsFactory = vcsFactory;
  }

  public BuildStamp getBuildStamp() {
    return stamp.get();
  }
}
