package io.vertx.core.eventbus.impl;

import java.util.Random;

public class WildcardHandlers extends Handlers {

  @Override
  public HandlerHolder choose() {
    while (true) {
      int size = list.size();
      if (size == 0) {
        return null;
      }
      try {
        return list.get(new Random().nextInt(list.size()));
      } catch (IndexOutOfBoundsException e) {
        // Try again.
      }
    }
  }
}

