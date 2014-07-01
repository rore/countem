package im.rore.countem.store

import java.util.Date

/***
 * @author Rotem Hermon
 * 
 * Abstract class for implementing storage containers for the countem framework
 */
trait AbstractStorage {
	
	/**
	 * Define the amount of parallelism allowed for this storage type. 
	 * Set to 1 for no parallelism.
	 */
	def storageParallelism : Int
	
	/**
	 * Stores the provided counters. 
	 * Counters are considered to be current (so current time is taken).
	 * @param collection The collection name for these counters
	 * @param counters The counters to store (metric name -> count).
	 */
	protected[countem] def storeCounters(collection:String, counters:Map[String,Long])
	
	/**
	 * Gets all the counters under all collections.
	 * @param day The day to get. If null, current day is taken.
	 */
	def getAllCounters(day:Date) : Map[String, Map[String, Long]]

	/**
	 * Get the counters counted today. 
	 * @param collection The collection of the counters.
	 * @param day The day to get. If null, current day is taken. 
	 */
	def getCounters(collection:String, day:Date) : Map[String, Long]
}