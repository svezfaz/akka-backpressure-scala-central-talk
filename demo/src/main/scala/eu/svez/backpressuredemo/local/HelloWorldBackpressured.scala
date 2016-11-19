package eu.svez.backpressuredemo.local

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Try

object HelloWorldBackpressured extends App {

  implicit val system = ActorSystem("backpressure-local-demo")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sourceValve = Agent(1.second)
  val sinkValve = Agent(1.second)

  Source.repeat("world")
    .via(valve(sourceValve.future()))
    .via(meter("source"))
    .map(x => s"Hello $x!")
    .via(valve(sinkValve.future()))
    .via(meter("sink"))
    .runWith(Sink.ignore)

  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }








  Iterator.continually(io.StdIn.readLine()).foreach {
    case ln if ln.startsWith("source=") =>
      Try(sourceValve.send(FiniteDuration((1000 / ln.replace("source=", "").toDouble).toLong, TimeUnit.MILLISECONDS))).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case ln if ln.startsWith("sink=") =>
      Try(sinkValve.send(FiniteDuration((1000 / ln.replace("sink=", "").toDouble).toLong, TimeUnit.MILLISECONDS))).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case _ => println("I don't understand")
  }
}

