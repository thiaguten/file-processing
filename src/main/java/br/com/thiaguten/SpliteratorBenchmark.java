package br.com.thiaguten;

import static br.com.thiaguten.FixedBatchSpliterator.parallelWithBatchSize;
import static br.com.thiaguten.FixedBatchSpliterator.withBatchSize;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

final class SpliteratorBenchmark {

  static double sink;

  /*public static void main(String[] args) throws Exception {
    final Path inputPath = createInput();
    for (int i = 0; i < 3; i++) {
      System.out.println("Start processing JDK stream" + i);
      measureProcessing(Files.lines(inputPath));
      System.out.println("Start processing fixed-batch stream" + i);
      measureProcessing(withBatchSize(Files.lines(inputPath)));
      System.out.println("Start processing fixed-batch parallel stream" + i);
      measureProcessing(parallelWithBatchSize(Files.lines(inputPath)));
    }
  }*/

  private static void measureProcessing(Stream<String> input) {
    final long start = System.nanoTime();
    try (Stream<String> stream = input) {
      final int cores = Runtime.getRuntime().availableProcessors();
      final double cpuTime = stream.mapToLong(SpliteratorBenchmark::processLine).sum(), realTime = System.nanoTime() - start;
      System.out.format("          Cores: %d\n", cores);
      System.out.format("       CPU time: %.2f s\n", cpuTime / SECONDS.toNanos(1));
      System.out.format("      Real time: %.2f s\n", realTime / SECONDS.toNanos(1));
      System.out.format("CPU utilization: %.2f%%\n\n", 100.0 * cpuTime / realTime / cores);
    }
  }

  public static void heavyProcess(final String value) {
    // some heavy process...
    double d = 0;
    for (int i = 0; i < value.length(); i++) {
      for (int j = 0; j < value.length(); j++) {
        d += Math.pow(value.charAt(i), value.charAt(j) / 32.0);
      }
    }
    sink += d;
  }

  private static long processLine(String line) {
    final long localStart = System.nanoTime();
    heavyProcess(line);
    return System.nanoTime() - localStart;
  }

  public static Path createInput() throws IOException {
    final Path inputPath = Files.createTempFile("input", ".txt");
    // mark the file to be deleted when the JVM terminates.
    inputPath.toFile().deleteOnExit();
    try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(inputPath))) {
      for (int i = 0; i < 1_000_000; i++) {
        final String text = String.valueOf(System.nanoTime());
        for (int j = 0; j < 2; j++) {
          w.print(text);
        }
        w.println();
      }
    }
    return inputPath;
  }

}
