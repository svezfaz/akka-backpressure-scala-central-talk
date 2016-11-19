package eu.svez.backpressuredemo.ws

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.util.Try

object MessageServer extends App{

  implicit val system = ActorSystem("message-server")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sinkRate = Agent(1)

  val sink = Flow[Message]
    .via(valve(sinkRate.future()))
    .via(meter("sinkWS"))
    .to(Sink.ignore)

  val handlerFlow = Flow.fromSinkAndSource(sink, Source.maybe)

  val route = get {
    path("messages") {
      extractUpgradeToWebSocket{ wsUpgrade =>
        complete(wsUpgrade.handleMessages(handlerFlow))
      }
    }
  }

  val host = "0.0.0.0"
  val port = 9090

  Http().bindAndHandle(route, host, port).map { _ =>
    println(s"Websocket server started on $host:$port")
  }






  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

  Iterator.continually(io.StdIn.readLine()).foreach {
    case ln if ln.startsWith("sink=") =>
      Try(sinkRate.send(ln.replace("sink=", "").toInt)).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case _ => println("I don't understand")
  }
}
