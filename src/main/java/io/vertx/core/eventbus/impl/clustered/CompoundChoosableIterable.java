package io.vertx.core.eventbus.impl.clustered;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.spi.cluster.ChoosableIterable;

public class CompoundChoosableIterable<T> implements ChoosableIterable<T> {
  private ChoosableIterable<T> standardIterable;
  private ChoosableIterable<T> regexIterable;
  private AtomicBoolean isStandard;

  public CompoundChoosableIterable(AtomicBoolean isStandard, ChoosableIterable<T> standardIterable,
                                   ChoosableIterable<T> regexIterable) {
    this.standardIterable = standardIterable;
    this.regexIterable = regexIterable;
    this.isStandard = isStandard;
  }

  @Override
  public boolean isEmpty() {
    return standardIterable.isEmpty() && (regexIterable == null || regexIterable.isEmpty());
  }

  @Override
  public T choose() {
    if (isEmpty()) {
      return null;
    }
    T val = null;
    if (!isStandard.get() && (regexIterable == null || regexIterable.isEmpty())) {
      isStandard.set(true);
    }
    if (isStandard.get()) {
      val = standardIterable.choose();
    } else {
      val = regexIterable.choose();
    }
   return val;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private LinkedList<Iterator<T>> iterators;
      private Iterator<T> curIterator = null;
      {
        iterators = Stream.of(standardIterable, regexIterable)
            .filter(Objects::nonNull)
            .map(Iterable::iterator)
            .collect(Collectors.toCollection(LinkedList::new));
        curIterator = iterators.pollFirst();
      }

      @Override
      public boolean hasNext() {
        return (curIterator != null && curIterator.hasNext()) ||
            (!iterators.isEmpty() && iterators.getFirst().hasNext());
      }


      @Override
      public T next() {
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
