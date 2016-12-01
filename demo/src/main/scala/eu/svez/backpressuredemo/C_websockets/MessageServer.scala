package eu.svez.backpressuredemo.C_websockets

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import eu.svez.backpressuredemo.StreamDemo
import eu.svez.backpressuredemo.Flows._

object MessageServer extends StreamDemo {

  sinkRate.send(5)

  val sink = Flow[Message]
    .via(valve(sinkRate.get()))
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

  readRatesFromStdIn()
}
