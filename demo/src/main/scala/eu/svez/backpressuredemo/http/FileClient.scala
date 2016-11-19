package eu.svez.backpressuredemo.http

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Try

object FileClient extends App{

  implicit val system = ActorSystem("file-client")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sourceValve = Agent(1.second)

  val byteSource = FileIO
    .fromPath(Paths.get("/tmp/bigfile.zip"))
    .via(valve(sourceValve.future))
    .via(meter("sourceHttp"))

  val host = "0.0.0.0"
  val port = 8080

  val request = HttpRequest(
    uri = Uri(s"http://$host:$port/file"),
    entity = HttpEntity(ContentTypes.`application/octet-stream`, byteSource)
  )

  Http().singleRequest(request).onComplete{ _ =>
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
