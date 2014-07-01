package im.rore.countem

import akka.actor._
import akka.routing.RoundRobinPool
import im.rore.countem.utils.Configuration
import im.rore.countem.actors.CollectActor
import im.rore.countem.actors.IncrementMsg
import im.rore.countem.actors.StorageActor
import im.rore.countem.store.AbstractStorage
import akka.routing.DefaultResizer
import im.rore.countem.utils.Helpers
import im.rore.countem.utils.Logging

/**
 * @author Rotem Hermon
 *
 * Main API for the event collector framework.
 */
trait Collector extends Logging {

	this : AbstractStorage => 
	// The internal actor system used by the collector
	private val _system = Configuration.getActorSystem("countem");
	// the storage actor. 
	val resizer = DefaultResizer(lowerBound = 1, upperBound = storageParallelism)
	private val storageRouter: ActorRef =
		_system.actorOf(RoundRobinPool(5, Some(resizer), routerDispatcher = "router-dispatcher")
				.props(Props(classOf[StorageActor], this)), "storageActor")
	// the collector router actor. It will pass the messages to the CollectActor actors
	private val collectorRouter: ActorRef =
		_system.actorOf(RoundRobinPool(5, routerDispatcher = "router-dispatcher")
				.props(Props(classOf[CollectActor], storageRouter)), "collectActor")
		
	/**
	 * This is just a dummy method for allowing separate initialization of the actor framework 
	 */
	def init() {
	}
		
	/**
	 * Increments a metric with the count of 1
	 */
	def Increment(collection: String, metric: String) {
		Increment(collection, metric, 1)
	}

	/**
	 * Increments a metric with a count
	 */
	def Increment(collection: String, metric: String, count: Int) {
		collectorRouter ! IncrementMsg(collection, metric, count)
	}
	
	/**
	 * Closes the collector actor system. Should be called when the application closes 
	 * to release all threads and allow the application to close. 
	 */
	def Shutdown = _system.shutdown;

}