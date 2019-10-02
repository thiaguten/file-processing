package br.com.thiaguten

import java.nio.file.Files.lines
import java.util.concurrent.TimeUnit.SECONDS
import java.util.stream.{Stream => JStream}

import br.com.thiaguten.FixedBatchSpliterator.{parallelWithBatchSize, withBatchSize}
import br.com.thiaguten.Resource.autoClose
import br.com.thiaguten.SpliteratorBenchmark._
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source
import scala.util.Try

object Main extends App with LazyLogging {

  val path = createInput()

  logger.info("Start processing Scala scala.io.Source")
  measureProcessing(Source.fromFile(path.toFile))

  //logger.info("Start processing JDK stream")
  //measureProcessing(lines(path))

  //logger.info("Start processing fixed-batch stream")
  //measureProcessing(withBatchSize(lines(path)))

  logger.info("Start processing fixed-batch parallel stream")
  measureProcessing(parallelWithBatchSize(lines(path)))

  def measureProcessing(source: Source): Unit = {
    val start = System.nanoTime
    autoClose(source)(s => printSummary(start, s.getLines.map(processLine).sum))
  }

  def measureProcessing(stream: JStream[String]): Unit = {
    val start: Long = System.nanoTime
    autoClose(stream)(s => printSummary(start, s.mapToLong(processLine).sum))
  }

  def printSummary(start: Long, cpuTime: Double): Unit = {
    val realTime: Double = System.nanoTime - start
    val cores = Runtime.getRuntime.availableProcessors()
    logger.info("          Cores: {}", "%d".format(cores))
    logger.info("       CPU time: {} s", "%.2f".format(cpuTime / SECONDS.toNanos(1)))
    logger.info("      Real time: {} s", "%.2f".format(realTime / SECONDS.toNanos(1)))
    logger.info("CPU utilization: {}\n", "%.2f%%".format(100.0 * cpuTime / realTime / cores))
  }

  def timed[A](block: => A): Long = {
    val localStart = System.nanoTime
    Try {
      block
      System.nanoTime - localStart
    } recover {
      case e =>
        logger.error("error", e)
        System.nanoTime - localStart
    } get
  }

  def processLine(line: String): Long = timed(heavyProcess(line))

}
