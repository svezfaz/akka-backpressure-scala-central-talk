package eu.svez.backpressuredemo.B_http

import java.nio.file.Paths

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.FileIO
import eu.svez.backpressuredemo.Flows._
import eu.svez.backpressuredemo.StreamDemo

object FileClient extends StreamDemo {

  val byteSource = FileIO
    .fromPath(Paths.get("/tmp/bigfile.zip"))
    .via(valve(sourceRate.future))
    .via(meter("sourceHttp"))

  val host = "0.0.0.0"
  val port = 8080

  val request = HttpRequest(
    uri = Uri(s"http://$host:$port/file"),
    entity = HttpEntity(ContentTypes.`application/octet-stream`, byteSource)
  )

  sourceRate.send(5)

  Http().singleRequest(request).onComplete{ _ =>
    println("All sent!")
  }

  readRatesFromStdIn()
}
