package leo.test.scala

import akka.actor._
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._
import akka.util.Timeout
import akka.routing._
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import com.typesafe.config.ConfigFactory

object Listener {
	case class Start
}
/**
* To distribute tasks, and recieve the final result
*/
class Listener(value: Double, step: Double) extends Actor with ActorLogging {
	import Listener._
	import Worker._

	val beginTime = System.currentTimeMillis
	val router = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(100)), "router")
	var pi = 0.0
	var donePieces = 0.0

	val totalPieces = value / step
	log.info(s"Listener started!  totalPieces=$totalPieces")

	def receive = LoggingReceive {
		case ResultPiece(subsum) => 
			pi += subsum
			donePieces += 1.0
			log.info(s"\tReceived $donePieces")
			if (donePieces >= totalPieces) {
				val endTime = System.currentTimeMillis
				log.info(s"Done: pi=$pi, Pi=${pi*4.0}, it takes ${endTime - beginTime}")
				context.stop(self)
			}

		case Start =>
			var i = 0.0
			while (i < totalPieces) {
				log.info(s"\tdispatched one piece: $i => (${i*step + 1}, ${(i+1)*step})")
				router ! TaskPiece( i*step + 1, (i+1) * step )
				i += 1
			}
	}
}

object Worker {
	case class TaskPiece(start: Double, end: Double)
	case class ResultPiece(subsum: Double)
}

class Worker extends Actor with ActorLogging {
	import Worker._

	def receive = LoggingReceive {
		case TaskPiece(start, end) => 
			sender ! ResultPiece(calPiece(start, end))
		
	}

	def calPiece(begin: Double, end: Double): Double = {
		var i = begin
		var pi = 0.0; 
		while (i <= end) { 
			pi += Math.pow(-1, i+1)/(2*i-1)
			i += 1.0 
		}
		pi
	}
}


object PiCalculatorApp extends App {
	import Listener._
	val config = ConfigFactory.parseString("""
		akka.loglevel = "INFO"
		akka.actor.debug {
			receive = on
			lifecycle = on
		}
	""")
	val system = ActorSystem("PICalculatorApp", config)

	val step = 1.0E7
	val value = step * 1.0E3
	val listener = system.actorOf(Props(classOf[Listener], value, step), name = "listener")
	listener ! Start

}