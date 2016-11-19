package eu.svez.backpressuredemo.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.agent.Agent
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import eu.svez.backpressuredemo.Flows._
import kamon.Kamon

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Try

object FileServer extends App{

  implicit val system = ActorSystem("file-server")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  val sinkValve = Agent(1.second)

  val flow = Flow[ByteString]
    .via(valve(sinkValve.future))
    .via(meter("sinkHttp"))

  val route = path("file") {
    extractRequest { request =>
      val done = request.entity.dataBytes.via(flow).runWith(Sink.ignore)
      complete(done)
    }
  }

  val host = "0.0.0.0"
  val port = 8080

  Http().bindAndHandle(route, host, port).map { _ =>
    println(s"Server started on $host:$port")
  }








  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

  Iterator.continually(io.StdIn.readLine()).foreach {
    case ln if ln.startsWith("sink=") =>
      Try(sinkValve.send(FiniteDuration((1000 / ln.replace("sink=", "").toDouble).toLong, TimeUnit.MILLISECONDS))).recover{
        case e => println(s"Error: ${e.getMessage}")
      }
    case _ => println("I don't understand")
  }

}
