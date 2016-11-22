package eu.svez.backpressuredemo

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.stream.ActorMaterializer
import kamon.Kamon

import scala.util.Try

trait StreamDemo extends App {

  implicit val system = ActorSystem("stream-demo")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sourceRate = Agent(1)
  val sinkRate = Agent(1)

  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

  def readRatesFromStdIn() = {
    Iterator.continually(io.StdIn.readLine()).foreach {
      case ln if ln.startsWith("source=") =>
        Try(sourceRate.send(ln.replace("source=", "").toInt)).recover {
          case e => println(s"Error: ${e.getMessage}")
        }
      case ln if ln.startsWith("sink=") =>
        Try(sinkRate.send(ln.replace("sink=", "").toInt)).recover {
          case e => println(s"Error: ${e.getMessage}")
        }
      case _ => println("I don't understand")
    }
  }
}
