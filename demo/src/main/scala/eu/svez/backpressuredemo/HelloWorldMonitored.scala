package eu.svez.backpressuredemo

import akka.NotUsed
import akka.stream.DelayOverflowStrategy
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.duration._

object HelloWorldMonitored extends StreamDemo {

  Source.repeat(NotUsed)
    .zipWithIndex
    .via(Checkpoint("A"))
    .delay(5.seconds, DelayOverflowStrategy.backpressure)
    .via(Checkpoint("B"))
    .runWith(Sink.foreach(println))
}

