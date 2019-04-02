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
 *
 *  Modifications Copyright (C) 2019 Knot.x Project
 */

package io.knotx.stack;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.awaitility.Awaitility;
import io.knotx.stack.command.ResolveCommand;
import io.knotx.stack.utils.FileUtils;
import io.vertx.core.Launcher;
import io.vertx.core.impl.launcher.VertxCommandLauncher;
import io.vertx.core.spi.launcher.ExecutionContext;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 * @author <a href="http://github.com/marcinczeczko">Marcin Czeczko</a>
 */
public class DescriptorTest {

  private File root = new File("target/stack");

  @BeforeEach
  public void setUp() {
    FileUtils.delete(root);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !root.exists());
  }

  @Test
  public void testResolutionOfCore() {
    ResolveCommand cmd = new ResolveCommand();
    cmd.setFailOnConflict(false);
    cmd.setDirectory(root.getAbsolutePath());
    cmd.setStackDescriptor(new File("src/test/resources/stacks/core.json").getAbsolutePath());
    cmd.setUp(new ExecutionContext(cmd, new VertxCommandLauncher(), null));
    cmd.run();
    assertThat(new File(root, "vertx-core-3.1.0.jar")).isFile();
  }

  @Test
  public void testResolutionOfCoreWithVariable() {
    ResolveCommand cmd = new ResolveCommand();
    cmd.setFailOnConflict(false);
    cmd.setDirectory(root.getAbsolutePath());
    cmd.setStackDescriptor(new File("src/test/resources/stacks/core-with-variable.json").getAbsolutePath());
    cmd.setUp(new ExecutionContext(cmd, new VertxCommandLauncher(), null));
    cmd.run();
    assertThat(new File(root, "vertx-core-3.1.0.jar")).isFile();
  }

  @Test
  public void testResolutionOfCoreUsingSystemVariable() {
    List<String> args = new ArrayList<>();
    args.add("resolve");
    args.add("--dir=" + root.getAbsolutePath());
    args.add("-Dvertx.version=3.1.0");
    args.add(new File("src/test/resources/stacks/core-with-system-variable.json").getAbsolutePath());
    Launcher.main(args.toArray(new String[args.size()]));

    assertThat(new File(root, "vertx-core-3.1.0.jar")).isFile();
  }

  @Test
  public void testResolutionWithDefaultDescriptor() {
    File defaultStack = new File("knotx-stack.json");
    FileUtils.copyFile(new File("src/test/resources/stacks/core-with-system-variable.json"),
        defaultStack);

    List<String> args = new ArrayList<>();
    args.add("resolve");
    args.add("--dir=" + root.getAbsolutePath());
    args.add("-Dvertx.version=3.1.0");
    Launcher.main(args.toArray(new String[args.size()]));

    assertThat(new File(root, "vertx-core-3.1.0.jar")).isFile();
    defaultStack.delete();
  }

  @Test
  public void testResolutionWithDefaultDescriptorInVertxHome() {
    File home = new File("target/home");
    home.mkdirs();
    System.setProperty("knotx.home", home.getAbsolutePath());

    FileUtils.copyFile(new File("src/test/resources/stacks/core-with-system-variable.json"),
        new File(home, "knotx-stack.json"));

    List<String> args = new ArrayList<>();
    args.add("resolve");
    args.add("-Dvertx.version=3.1.0");
    Launcher.main(args.toArray(new String[args.size()]));

    assertThat(new File(home, "lib/vertx-core-3.1.0.jar")).isFile();
    System.clearProperty("knotx.home");
  }


}
