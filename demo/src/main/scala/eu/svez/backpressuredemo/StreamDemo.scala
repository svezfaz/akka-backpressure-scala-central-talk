package eu.svez.backpressuredemo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import kamon.Kamon

trait StreamDemo extends App {

  implicit val system = ActorSystem("stream-demo")
  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  Kamon.start()

  scala.sys.addShutdownHook {
    Kamon.shutdown()
    system.terminate()
  }

}
