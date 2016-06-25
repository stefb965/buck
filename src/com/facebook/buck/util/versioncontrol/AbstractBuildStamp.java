package com.facebook.buck.util.versioncontrol;

import com.facebook.buck.util.immutables.BuckStyleImmutable;

import org.immutables.value.Value;

@Value.Immutable
@BuckStyleImmutable
public interface AbstractBuildStamp {
  String getRevision();
  long getVersionTimestamp();
  long getBuildTimestamp();
}
