/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package io.vertx.core.eventbus.impl.clustered;

import io.vertx.core.spi.cluster.ChoosableIterable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompoundChoosableIterable<T> implements ChoosableIterable<T> {
  private ChoosableIterable<T> iterables[];
  private Iterator<T> iter;
  private AtomicInteger initialPos;

  @SafeVarargs
  public CompoundChoosableIterable(AtomicInteger initialPos, ChoosableIterable<T>... iterables) {
    this.iterables = iterables;
    this.initialPos = initialPos;
  }

  @Override
  public boolean isEmpty() {
    return Arrays.stream(iterables).allMatch(it -> it == null || it.isEmpty());
  }

  @Override
  public T choose() {
    if (iter == null || !iter.hasNext()) {
      iter = iterator();
    }
    try {
      return iter.next();
    } catch (NoSuchElementException e) {
      return null;
    }
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private LinkedList<Iterator<T>> iterators;
      private Iterator<T> curIterator = null;
      private boolean initialized = false;
      {
        iterators = Arrays.stream(iterables)
            .filter(Objects::nonNull)
            .map(Iterable::iterator)
            .collect(Collectors.toCollection(LinkedList::new));
        curIterator = iterators.pollFirst();
      }

      @Override
      public boolean hasNext() {
        initialize();
        return (curIterator != null && curIterator.hasNext()) ||
            (!iterators.isEmpty() && iterators.getFirst().hasNext());
      }

      private void initialize() {
        if (!initialized) {
          initialized = true;
          IntStream.range(0, initialPos.get()).forEach(i -> next());
        }
      }

      @Override
      public T next() {
        initialize();
        if (curIterator != null && curIterator.hasNext()) {
          return curIterator.next();
        }
        if (!iterators.isEmpty()) {
          curIterator = iterators.pollFirst();
          return curIterator.next();
        }
        throw new NoSuchElementException();
      }
    };
  }
}
