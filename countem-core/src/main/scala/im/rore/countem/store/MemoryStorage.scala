package im.rore.countem.store

import scala.collection.mutable.HashMap
import im.rore.countem.Collector
import java.util.Date

/**
 * An example storage implementation that keeps the counters
 * in a map in memory.
 * Can be used for testing purposes only, not intended for production usage.
 */
trait MemoryStorage extends AbstractStorage {

	val countersMap = new HashMap[String, HashMap[String, Long]]

	// we set the parallelism to 1 since we hold the counters in memory
	def storageParallelism = 1

	def storeCounters(collection: String, counters: Map[String, Long]) {
		if (null != collection && null != counters) {
			var colMap = countersMap.get(collection).getOrElse(null);
			if (null == colMap) {
				colMap = new HashMap[String, Long];
				countersMap(collection) = colMap;
			}
			counters.foreach(f => {
				val metric = f._1
				val count = f._2
				val c : Long = colMap.get(metric).getOrElse(0);
				colMap(metric) = c + count
			})
		}
	}
	
	def getAllCounters(day:Date) = countersMap.map(f => (f._1, f._2.toMap)).toMap
	
	def getCounters(collection:String, day:Date) = {
		if (null != collection) {
			var colMap = countersMap.get(collection).getOrElse(null);
			if (null != colMap) colMap.toMap else null
		}
		else null
	}

	def clearAll {
		countersMap.clear
	}
}

object MemoryCollector extends Collector with MemoryStorage