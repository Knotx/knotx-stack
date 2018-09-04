/*
 *  Copyright (c) 2011-2018 The original author or authors
 *  ------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.knotx.stack;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.awaitility.Awaitility;
import io.knotx.stack.model.Stack;
import io.knotx.stack.model.StackResolution;
import io.knotx.stack.model.StackResolutionOptions;
import io.knotx.stack.utils.FileUtils;
import io.vertx.core.impl.launcher.commands.VersionCommand;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VertxStacksTest {

  private File root = new File("target/stack");

  @BeforeEach
  public void setUp() {
    FileUtils.delete(root);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !root.exists());
    String vertxVersion = VersionCommand.getVersion();
    assertThat(vertxVersion).isNotEmpty();
    System.setProperty("vertx.version", vertxVersion);
  }

  @AfterEach
  public void tearDown() {
    System.clearProperty("knotx.version");
  }

  @Test
  public void testKnotxStack() {
    Stack stack = Stack.fromDescriptor(new File("src/test/resources/stacks/knotx-stack.json"));

    StackResolution resolution = new StackResolution(stack, root,
        new StackResolutionOptions().setFailOnConflicts(true));
    Map<String, File> resolved = resolution.resolve();
    assertThat(resolved).isNotEmpty();
  }

}
