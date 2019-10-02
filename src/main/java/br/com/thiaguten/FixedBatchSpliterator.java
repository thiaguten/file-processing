package br.com.thiaguten;

import static java.util.stream.StreamSupport.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {

  private static final int FIXED_BATCH_SIZE = 10;

  private final Spliterator<T> spliterator;

  public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize) {
    super(toWrap.characteristics(), batchSize, toWrap.estimateSize());
    this.spliterator = toWrap;
  }

  public static <T> FixedBatchSpliterator<T> batchedSpliterator(Spliterator<T> toWrap,
      int batchSize) {
    return new FixedBatchSpliterator<>(toWrap, batchSize);
  }

  public static <T> Stream<T> withBatchSize(Stream<T> in) {
    return withBatchSize(in, FIXED_BATCH_SIZE);
  }

  public static <T> Stream<T> parallelWithBatchSize(Stream<T> in) {
    return parallelWithBatchSize(in, FIXED_BATCH_SIZE);
  }

  public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize) {
    return withBatchSize(in, batchSize, false);
  }

  public static <T> Stream<T> parallelWithBatchSize(Stream<T> in, int batchSize) {
    return withBatchSize(in, batchSize, true);
  }

  public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize, boolean parallel) {
    return stream(batchedSpliterator(in.spliterator(), batchSize), parallel);
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    return spliterator.tryAdvance(action);
  }

  @Override
  public void forEachRemaining(Consumer<? super T> action) {
    spliterator.forEachRemaining(action);
  }
}
