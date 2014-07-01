package im.rore.countem.actors

import akka.actor.Actor
import im.rore.countem.utils.Logging
import im.rore.countem.utils.Helpers
import im.rore.countem.store.AbstractStorage

case class StoreMessage(collection:String, counters:Map[String,Long])


class StorageActor(val storage:AbstractStorage) extends Actor with Logging {

	import Helpers._
	
	    // actor main function
	def receive = {
		case s : StoreMessage => {
			debug("received store message: " + s + " - " + self.path.toSerializationFormat)
			store(s)
		}
		case m  => {
			warn("received unknown message: " + m + " - " + self.path.toSerializationFormat)
		}
	}

	private def store(msg:StoreMessage){
		try{
			storage.storeCounters(msg.collection , msg.counters)
		}
		catch {
			case e:Throwable => {
				error("failed storing counters", e)
				throw e
			}
		}
	}
}