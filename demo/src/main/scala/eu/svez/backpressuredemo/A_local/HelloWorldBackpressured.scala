package eu.svez.backpressuredemo.A_local

import akka.NotUsed
import akka.pattern.after
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import eu.svez.backpressuredemo.StreamDemo
import kamon.Kamon

import scala.concurrent.Future
import scala.concurrent.duration._

object HelloWorldBackpressured extends StreamDemo {

  sourceRate.send(5)
  sinkRate.send(5)

  def valve[T](rate: => Int): Flow[T, T, NotUsed] =
    Flow[T].mapAsync(1) { x =>
      after(1.second / rate, system.scheduler)(Future.successful(x))
    }

  def meter[T](name: String): Flow[T, T, NotUsed] = {
    val msgCounter = Kamon.metrics.counter(name)

    Flow[T].map { x =>
      msgCounter.increment()
      x
    }
  }

  Source.repeat("Hello world!")
    .via(valve(sourceRate.get))
    .via(meter("source"))

    .buffer(100, OverflowStrategy.backpressure)
    .via(valve(sinkRate.get))
    .via(meter("sink"))
    .runWith(Sink.ignore)



  readRatesFromStdIn()
}

