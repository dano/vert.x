/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package io.vertx.core.eventbus.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * TODO comment me
 */
public class CompoundHandlers {
  private List<Handlers> handlersList;
  private final AtomicInteger pos = new AtomicInteger(0);

  public CompoundHandlers(Handlers... handlers) {
    handlersList = Arrays.asList(handlers);
  }
  public int size() {
    return handlersList.stream()
        .filter(Objects::nonNull)
        .mapToInt(handlers -> handlers.list.size())
        .sum();
  }

  public boolean isNull() {
    return handlersList.get(0) == null && handlersList.get(1) == null;
  }

  public HandlerHolder choose() {
    while (true) {
      int size = size();
      if (size == 0) {
        return null;
      }
      int p = pos.getAndIncrement();
      if (p >= size - 1) {
        pos.set(0);
      }

      // Go through each list of handlers.
      for (Handlers h : handlersList) {
        if (h == null) {
          continue;
        }
        if (p > (h.list.size() - 1)) {
          p -= h.list.size();
          continue;
        }
        try {
          return h.list.get(p);
        } catch (IndexOutOfBoundsException e) {
          // Can happen
          pos.set(0);
        }
      }
    }
  }

  public Stream<HandlerHolder> stream() {
    return handlersList.stream()
        .filter(Objects::nonNull)
        .flatMap(handlers -> handlers.list.stream());
  }
}
