package eu.svez.backpressuredemo.ws

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Try

object MessageClient extends App{

  implicit val system = ActorSystem("message-client")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sourceValve = Agent(1.second)

  val source = Source.repeat("hello")
    .via(valve(sourceValve.future()))
    .via(meter("sourceWS"))
    .map(TextMessage(_))

  val host = "0.0.0.0"
  val port = 9090

  val clientFlow = Http().singleWebSocketRequest(WebSocketRequest(s"ws://$host:$port/messages"),
    Flow.fromSinkAndSource(Sink.ignore, source))




  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

  Iterator.continually(io.StdIn.readLine()).foreach {
    case ln if ln.startsWith("source=") =>
      Try(sourceValve.send(FiniteDuration((1000 / ln.replace("source=", "").toDouble).toLong, TimeUnit.MILLISECONDS))).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case _ => println("I don't understand")
  }

}
