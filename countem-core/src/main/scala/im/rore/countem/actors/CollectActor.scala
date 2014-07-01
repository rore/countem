package im.rore.countem.actors

import akka.actor._
import akka.routing.FromConfig
import akka.routing.RoundRobinPool
import im.rore.countem.utils.Logging
import scala.collection.mutable.HashMap
import java.util.Date
import im.rore.countem.utils.Helpers
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

/**
 * Base class for collector messages
 */
abstract class CollectMessage
case class IncrementMsg(collection: String, metric: String, count: Int) extends CollectMessage
case class UpdateTickMsg() extends CollectMessage

/**
 * @author Rotem Hermon
 *
 * The collector actor, one doing the actual work...
 */
class CollectActor(val storageActor: ActorRef) extends Actor with Logging {

	import Helpers._

	// We keep an internal map of counters. We do not need to lock this map since akka guarantees 
	// a single actor will process messages in sequence
	protected val counters = new HashMap[String, HashMap[String, Long]];
	// Interval for updating counters to the persistent store. Can be configured via the configuration file  
	val UPDATE_COUNTS_INTERVAL_SECONDS = context.system.settings.config.getDouble("countersUpdateIntervalSeconds", 30);
    val UPDATE_COUNTS_INTERVAL_MILLIS = (UPDATE_COUNTS_INTERVAL_SECONDS * 1000).toLong 
	import context.dispatcher

	// create the update scheduler 
	val cancellable = context.system.scheduler.schedule(Duration(UPDATE_COUNTS_INTERVAL_MILLIS, TimeUnit.MILLISECONDS),
		Duration(UPDATE_COUNTS_INTERVAL_MILLIS, TimeUnit.MILLISECONDS), self, UpdateTickMsg())

	// actor main function
	def receive = {
		case msg: IncrementMsg => {
			debug("received increment: " + msg.collection + " : " + msg.metric + " - " + self.path.toSerializationFormat)
			incCount(msg)
		}
		case msg: UpdateTickMsg => {
			updateCounts
		}
		case m => {
			warn("received unknown message: " + m + " - " + self.path.toSerializationFormat)
		}
	}

    override def postStop() {
    	// if the actor was stopped we need to persist the existing counters as it won't get the update tick
    	updateCounts
    }
    
	protected def incCount(msg: IncrementMsg) = {
		if (null != msg && null != msg.collection && null != msg.metric) {
			// update the counter in the local counter map
			var colMap = counters.get(msg.collection).getOrElse(null);
			if (null == colMap) {
				colMap = new HashMap[String, Long];
				counters(msg.collection) = colMap;
			}
			val c:Long = colMap.get(msg.metric).getOrElse(0);
			colMap(msg.metric) = c + msg.count;
		}
	}

	private def updateCounts: Unit = {
		counters.foreach(f => {
			debug("updating counters" + " - " + self.path.toSerializationFormat)
			storageActor ! StoreMessage(f._1, f._2.toMap)
		});
		counters.clear;
	}

}

