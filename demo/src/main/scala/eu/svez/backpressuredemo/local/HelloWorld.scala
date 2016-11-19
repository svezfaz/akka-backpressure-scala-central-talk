package eu.svez.backpressuredemo.local

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object HelloWorld extends App {

  implicit val system = ActorSystem("streams-local-demo")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val source = Source.single("world")

  val flow = Flow[String].map(x => s"Hello $x!")

  val sink = Sink.foreach[String](println)

  (source via flow runWith sink).onComplete { _ =>  system.terminate() }
}

