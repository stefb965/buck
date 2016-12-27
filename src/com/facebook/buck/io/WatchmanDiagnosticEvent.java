/*
 * Copyright 2016-present Facebook, Inc.
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

package com.facebook.buck.io;

import com.facebook.buck.event.AbstractBuckEvent;
import com.facebook.buck.event.EventKey;

public class WatchmanDiagnosticEvent extends AbstractBuckEvent {

  private final WatchmanDiagnostic watchmanDiagnostic;

  public WatchmanDiagnosticEvent(WatchmanDiagnostic diagnostic) {
    super(EventKey.unique());
    this.watchmanDiagnostic = diagnostic;
  }

  public WatchmanDiagnostic getDiagnostic() {
    return watchmanDiagnostic;
  }

  @Override
  public String getEventName() {
    return "watchman-diagnostic";
  }

  @Override
  public String getValueString() {
    return watchmanDiagnostic.getMessage();
  }
}