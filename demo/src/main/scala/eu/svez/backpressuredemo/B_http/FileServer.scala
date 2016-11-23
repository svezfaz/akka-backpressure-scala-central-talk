package eu.svez.backpressuredemo.B_http

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import eu.svez.backpressuredemo.Flows._
import eu.svez.backpressuredemo.StreamDemo

object FileServer extends StreamDemo{

  val flow = Flow[ByteString]
    .via(valve(sinkRate.get))
    .via(meter("sinkHttp"))

  val route = path("file") {
    extractRequest { request =>
      val done = request.entity.dataBytes.via(flow).runWith(Sink.ignore)
      complete(done)
    }
  }

  val host = "0.0.0.0"
  val port = 8080

  sinkRate.send(5)

  Http().bindAndHandle(route, host, port).map { _ =>
    println(s"Server started on $host:$port")
  }

  readRatesFromStdIn()
}
