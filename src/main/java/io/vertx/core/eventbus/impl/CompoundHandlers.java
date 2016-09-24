package io.vertx.core.eventbus.impl;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class CompoundHandlers {
  private final Handlers standardHandlers;
  private final Handlers regexHandlers;
  private AtomicBoolean isStandard;

  public CompoundHandlers(AtomicBoolean isStandard, Handlers standardHandlers, Handlers regexHandlers) {
    this.standardHandlers = standardHandlers;
    this.regexHandlers = regexHandlers;
    this.isStandard = isStandard;
  }
  public int size() {
    return (standardHandlers != null ? standardHandlers.list.size() : 0)
        + (regexHandlers != null ? regexHandlers.list.size() : 0);
  }

  public boolean isNull() {
    return standardHandlers == null && regexHandlers == null;
  }

  public HandlerHolder choose() {
    while (true) {
      if (size() == 0) {
        return null;
      }
      HandlerHolder holder = null;
      if(isStandard.get() && standardHandlers != null) {
        holder = standardHandlers.choose();
      } else if (regexHandlers != null) {
        holder = regexHandlers.choose();
      }
      isStandard.set(!isStandard.get());
      if (holder != null) {
        return holder;
      }
    }
  }

  public Stream<HandlerHolder> stream() {
    return Stream.concat(
        standardHandlers != null ? standardHandlers.list.stream() : Stream.empty(),
        regexHandlers != null ? regexHandlers.list.stream() : Stream.empty()
    );
  }
}
