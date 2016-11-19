package eu.svez.backpressuredemo

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.Flow
import kamon.Kamon

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Flows {

  def meter[T](name: String): Flow[T, T, NotUsed] = {
    val msgCounter = Kamon.metrics.counter(name)

    Flow[T].map { x =>
      msgCounter.increment()
      x
    }
  }

  def valve[T](f: => Future[FiniteDuration])(implicit system: ActorSystem): Flow[T, T, NotUsed] = {
    implicit val ec = system.dispatcher

    Flow[T].mapAsync(1) { x =>
      f.flatMap { pause =>
        after(pause, system.scheduler)(Future.successful(x))
      }
    }
  }

}
