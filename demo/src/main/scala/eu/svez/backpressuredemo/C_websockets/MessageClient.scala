package eu.svez.backpressuredemo.C_websockets

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Sink, Source}
import eu.svez.backpressuredemo.Flows._
import eu.svez.backpressuredemo.StreamDemo

object MessageClient extends StreamDemo {

  sourceRate.send(5)

  val source = Source.repeat("hello")
    .via(valve(sourceRate.get()))
    .via(meter("sourceWS"))
    .map(TextMessage(_))

  val host = "0.0.0.0"
  val port = 9090

  val clientFlow = Http().singleWebSocketRequest(WebSocketRequest(s"ws://$host:$port/messages"),
    Flow.fromSinkAndSource(Sink.ignore, source))

  readRatesFromStdIn()
}
