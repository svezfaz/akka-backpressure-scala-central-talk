package eu.svez.backpressuredemo

import akka.NotUsed
import akka.actor.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.Flow
import kamon.Kamon

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object Flows {

  def meter[T](name: String): Flow[T, T, NotUsed] = {
    val msgCounter = Kamon.metrics.counter(name)

    Flow[T].map { x =>
      msgCounter.increment()
      x
    }
  }

  def valve[T](rate: => Future[Int])(implicit system: ActorSystem, ec: ExecutionContext): Flow[T, T, NotUsed] =
    Flow[T].mapAsync(1) { x =>
      rate.flatMap { r =>
        after(1.second / r, system.scheduler)(Future.successful(x))
      }
    }

}
