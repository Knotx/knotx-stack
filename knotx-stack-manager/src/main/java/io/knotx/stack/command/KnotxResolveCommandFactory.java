package io.knotx.stack.command;

import io.vertx.core.spi.launcher.DefaultCommandFactory;

public class KnotxResolveCommandFactory extends DefaultCommandFactory<KnotxResolveCommand> {

  /**
   * Creates a new {@link KnotxResolveCommandFactory}.
   */
  public KnotxResolveCommandFactory() {
    super(KnotxResolveCommand.class);
  }
}
