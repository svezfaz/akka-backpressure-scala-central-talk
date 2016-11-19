package eu.svez.backpressuredemo.local

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.util.Try

object HelloWorldBackpressured extends App {

  implicit val system = ActorSystem("backpressure-local-demo")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sourceRate = Agent(1)
  val sinkRate = Agent(1)

  Source.repeat("world")
    .via(valve(sourceRate.future()))
    .via(meter("source"))
    .map(x => s"Hello $x!")
    .buffer(16, OverflowStrategy.backpressure)
    .via(valve(sinkRate.future()))
    .via(meter("sink"))
    .runWith(Sink.ignore)

  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

  Iterator.continually(io.StdIn.readLine()).foreach {
    case ln if ln.startsWith("source=") =>
      Try(sourceRate.send(ln.replace("source=", "").toInt)).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case ln if ln.startsWith("sink=") =>
      Try(sinkRate.send(ln.replace("sink=", "").toInt)).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case _ => println("I don't understand")
  }
}

