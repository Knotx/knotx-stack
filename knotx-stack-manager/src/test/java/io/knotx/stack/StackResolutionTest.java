/*
 *  Copyright (c) 2011-2015 The original author or authors
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.fail;

import com.jayway.awaitility.Awaitility;
import io.knotx.stack.model.Dependency;
import io.knotx.stack.model.DependencyConflictException;
import io.knotx.stack.model.Stack;
import io.knotx.stack.model.StackResolution;
import io.knotx.stack.model.StackResolutionOptions;
import io.knotx.stack.utils.FileUtils;
import io.knotx.stack.utils.LocalArtifact;
import io.knotx.stack.utils.LocalDependency;
import io.knotx.stack.utils.LocalRepoBuilder;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.maven.model.Exclusion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class StackResolutionTest {

  private File root = new File("target/stack");

  private final static StackResolutionOptions STRICT = new StackResolutionOptions()
      .setFailOnConflicts(true);

  @BeforeEach
  public void setUp() {
    FileUtils.delete(root);
    Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !root.exists());
  }

  @AfterEach
  public void tearDown() {
    System.clearProperty("vertx.version");
  }

  @Test
  public void testTheResolutionOfAVerySmallStack() {
    Stack stack = new Stack().addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"));
    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
  }

  @Test
  public void testTheResolutionOfAVerySmallStackWithFiltering() {
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "${vertx.version}"))
        .addVariable("vertx.version", "3.1.0");
    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
  }

  @Test
  public void testTheResolutionOfVertxCoreWithoutTransitive() {
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0").setTransitive(false));
    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKeys("io.vertx:vertx-core:jar:3.1.0").hasSize(1);
  }

  @Test
  public void testNoConflictWhenADependencyIsDeclaredTwice() {
    Dependency dependency = new Dependency("io.vertx", "vertx-core", "3.1.0");
    Stack stack = new Stack()
        .addDependency(dependency)
        .addDependency(new Dependency("com.fasterxml.jackson.core", "jackson-databind", "2.6.1"));

    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
    assertThat(map).containsKey("com.fasterxml.jackson.core:jackson-databind:jar:2.6.1");
  }

  @Test
  public void testConflictOnDependencyVersionMismatch() {
    Dependency dependency = new Dependency("io.vertx", "vertx-core", "3.1.0");
    Stack stack = new Stack()
        .addDependency(dependency)
        .addDependency(new Dependency("com.fasterxml.jackson.core", "jackson-databind", "2.4.1.3"));

    StackResolution resolution = new StackResolution(stack, root, STRICT);
    assertThatExceptionOfType(DependencyConflictException.class)
        .isThrownBy(() -> resolution.resolve());
  }

  @Test
  public void testConflictManagementUsingExclusions() {
    Dependency dependency = new Dependency("io.vertx", "vertx-core", "3.1.0");
    Exclusion exclusion1 = new Exclusion();
    exclusion1.setGroupId("com.fasterxml.jackson.core");
    exclusion1.setArtifactId("jackson-databind");
    Exclusion exclusion2 = new Exclusion();
    exclusion2.setGroupId("com.fasterxml.jackson.core");
    exclusion2.setArtifactId("jackson-core");
    dependency.addExclusion(exclusion1);
    dependency.addExclusion(exclusion2);
    Stack stack = new Stack()
        .addDependency(dependency)
        // Not the version used by vert.x
        .addDependency(new Dependency("com.fasterxml.jackson.core", "jackson-databind", "2.4.1.3"));
    StackResolution resolution = new StackResolution(stack, root, STRICT);
    resolution.resolve();
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
    assertThat(map).containsKey("com.fasterxml.jackson.core:jackson-databind:jar:2.4.1.3");
  }

  @Test
  public void testModificationOfStack() {
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "vertx-stomp", "3.1.0").setIncluded(false));

    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> resolved = resolution.resolve();
    assertThat(resolved).doesNotContainKeys("io.vertx:vertx-stomp:jar:3.1.0");
    int numberOfArtifacts = root.listFiles().length;

    // include stomp
    stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "vertx-stomp", "3.1.0").setIncluded(true));
    resolution = new StackResolution(stack, root, STRICT);
    resolved = resolution.resolve();
    assertThat(resolved).containsKey("io.vertx:vertx-stomp:jar:3.1.0");

    // remove stomp
    stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "knotx-stomp", "3.1.0").setIncluded(false));

    resolution = new StackResolution(stack, root, STRICT);
    resolved = resolution.resolve();
    assertThat(resolved).doesNotContainKeys("io.vertx:knotx-stomp:jar:3.1.0");
    int numberOfArtifacts2 = root.listFiles().length;
    assertThat(numberOfArtifacts).isEqualTo(numberOfArtifacts2);
  }

  @Test
  public void testTheResolutionWhenAnArtifactIsMissing() {
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "vertx-missing", "3.1.0"));
    StackResolution resolution = new StackResolution(stack, root, STRICT);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> resolution.resolve());
  }

  @Test
  public void testTheResolutionWhenATransitiveDependencyIsMissing() {
    File local = new File("target/test-repos/incomplete");
    new LocalRepoBuilder(local)
        .addArtifact(new LocalArtifact("org.acme", "acme", "1.0").generateMainArtifact()
            .addDependency(new LocalDependency("org.acme", "acme-missing", "1.0"))).build();
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("org.acme", "acme", "1.0", "txt"));
    StackResolutionOptions options = new StackResolutionOptions().setFailOnConflicts(true)
        .setLocalRepository(local.getAbsolutePath())
        .setCacheDisabled(true);
    StackResolution resolution = new StackResolution(stack, root, options);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> resolution.resolve());
  }

  @Test
  public void testTheResolutionWhenATransitiveDependencyIsMissingButExcluded() {
    File local = new File("target/test-repos/incomplete");
    new LocalRepoBuilder(local)
        .addArtifact(new LocalArtifact("org.acme", "acme", "1.0").generateMainArtifact()
            .addDependency(new LocalDependency("org.acme", "acme-missing", "1.0"))).build();

    Exclusion exclusion = new Exclusion();
    exclusion.setArtifactId("acme-missing");
    exclusion.setGroupId("org.acme");

    Dependency dependency = new Dependency("org.acme", "acme", "1.0", "txt");
    dependency.addExclusion(exclusion);

    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(dependency);
    StackResolutionOptions options = new StackResolutionOptions().setFailOnConflicts(true)
        .setLocalRepository(local.getAbsolutePath());
    StackResolution resolution = new StackResolution(stack, root, options);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
    assertThat(map).containsKey("org.acme:acme:txt:1.0");
  }

  @Test
  public void testTheResolutionWhenATransitiveDependencyIsMissingButOptional() {
    File local = new File("target/test-repos/incomplete");
    new LocalRepoBuilder(local)
        .addArtifact(new LocalArtifact("org.acme", "acme", "1.0").generateMainArtifact()
            .addDependency(new LocalDependency("org.acme", "acme-missing", "1.0").optional(true)))
        .build();

    Exclusion exclusion = new Exclusion();
    exclusion.setArtifactId("acme-missing");
    exclusion.setGroupId("org.acme");

    Dependency dependency = new Dependency("org.acme", "acme", "1.0", "txt");
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(dependency);
    StackResolutionOptions options = new StackResolutionOptions().setFailOnConflicts(true)
        .setLocalRepository(local.getAbsolutePath());
    StackResolution resolution = new StackResolution(stack, root, options);
    Map<String, File> map = resolution.resolve();
    assertThat(map).containsKey("io.vertx:vertx-core:jar:3.1.0");
    assertThat(map).containsKey("org.acme:acme:txt:1.0");
  }

  @Test
  public void testModificationOfStackIntroducingConflict() {
    Stack stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "vertx-stomp", "3.1.0").setIncluded(false))
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.0.0").setIncluded(false));

    StackResolution resolution = new StackResolution(stack, root, STRICT);
    Map<String, File> resolved = resolution.resolve();
    assertThat(resolved).doesNotContainKeys("io.vertx:vertx-core:jar:3.0.0");

    stack = new Stack()
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.1.0"))
        .addDependency(new Dependency("io.vertx", "vertx-stomp", "3.1.0").setIncluded(true))
        .addDependency(new Dependency("io.vertx", "vertx-core", "3.0.0").setIncluded(true));
    resolution = new StackResolution(stack, root, STRICT);
    try {
      resolved = resolution.resolve();
      fail("Conflict expected");
    } catch (DependencyConflictException e) {
      // OK
    }

    assertThat(resolved).containsKey("io.vertx:vertx-core:jar:3.1.0");
    assertThat(resolved).doesNotContainKeys("io.vertx:vertx-core:jar:3.0.0");
    assertThat(resolved).doesNotContainKeys("io.vertx:vertx-stomp:jar:3.1.0");

  }

}
