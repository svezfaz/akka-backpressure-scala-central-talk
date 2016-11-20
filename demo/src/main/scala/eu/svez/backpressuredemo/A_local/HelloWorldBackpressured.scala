package eu.svez.backpressuredemo.A_local

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source}
import eu.svez.backpressuredemo.Flows._
import eu.svez.backpressuredemo.StreamDemo

object HelloWorldBackpressured extends StreamDemo {

  Source.repeat("world")
    .via(valve(sourceRate.future()))
    .via(meter("source"))
    .map(x => s"Hello $x!")
//    .buffer(16, OverflowStrategy.backpressure)
    .via(valve(sinkRate.future()))
    .via(meter("sink"))
    .runWith(Sink.ignore)

  readFromStdIn()
}

