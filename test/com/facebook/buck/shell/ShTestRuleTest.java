/*
 * Copyright 2012-present Facebook, Inc.
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

package com.facebook.buck.shell;

import static org.junit.Assert.assertTrue;

import com.facebook.buck.model.BuildTargetFactory;
import com.facebook.buck.model.BuildTargetPattern;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.util.ProjectFilesystem;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Test;

public class ShTestRuleTest extends EasyMockSupport {

  @After
  public void tearDown() {
    // I don't understand why EasyMockSupport doesn't do this by default.
    verifyAll();
  }

  @Test
  public void testHasTestResultFiles() {
    ShTestRule shTest = new ShTestRule(
        createBuildRuleParams(),
        "run_test.sh",
        /* labels */ ImmutableSet.<String>of());

    ProjectFilesystem filesystem = createMock(ProjectFilesystem.class);
    EasyMock.expect(filesystem.isFile(shTest.getPathToTestOutputResult())).andReturn(true);
    BuildContext buildContext = createMock(BuildContext.class);
    EasyMock.expect(buildContext.getProjectFilesystem()).andReturn(filesystem);

    replayAll();

    assertTrue("hasTestResultFiles() should return true if result.json exists.",
        shTest.hasTestResultFiles(buildContext));
  }

  private static BuildRuleParams createBuildRuleParams() {
    return new BuildRuleParams(
        BuildTargetFactory.newInstance("//test/com/example:my_sh_test"),
        /* deps */ ImmutableSortedSet.<BuildRule>of(),
        /* visibilityPatterns */ ImmutableSet.<BuildTargetPattern>of());
  }
}