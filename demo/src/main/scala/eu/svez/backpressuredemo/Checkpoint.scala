package eu.svez.backpressuredemo

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet, Supervision}
import kamon.Kamon

import scala.util.control.NonFatal

final case class Checkpoint[T](label: String) extends GraphStage[FlowShape[T, T]] {
  val in = Inlet[T]("Meter.in")
  val out = Outlet[T]("Meter.out")
  override val shape = FlowShape(in, out)

  private val pullMeter        = Kamon.metrics.counter(label + "_pull")
  private val pushMeter        = Kamon.metrics.counter(label + "_push")
  private val latencyHistogram = Kamon.metrics.histogram(label + "_latency")

  override def initialAttributes: Attributes = Attributes.name("checkpoint")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with OutHandler {

      private var lastPulled: Long = _

      private def decider =
        inheritedAttributes.get[SupervisionStrategy].map(_.decider).getOrElse(Supervision.stoppingDecider)

      override def onPush(): Unit = {
        try {
          val latency = System.nanoTime() - lastPulled
          // TODO send to actor instead?
          latencyHistogram.record(latency)

          pushMeter.increment()
          push(out, grab(in))
        } catch {
          case NonFatal(ex) ⇒ decider(ex) match {
            case Supervision.Stop ⇒ failStage(ex)
            case _                ⇒ pull(in)
          }
        }
      }

      override def onPull(): Unit = {
        lastPulled = System.nanoTime()
        pullMeter.increment()
        pull(in)
      }

      setHandlers(in, out, this)
    }
}
