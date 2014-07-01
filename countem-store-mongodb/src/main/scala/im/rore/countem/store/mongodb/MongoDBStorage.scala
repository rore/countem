/**
 *
 */
package im.rore.countem.store.mongodb

import java.text.SimpleDateFormat
import java.util.Calendar
import com.mongodb.DBCollection
import com.mongodb.casbah.commons.MongoDBObject
import im.rore.countem.Collector
import im.rore.countem.store.AbstractStorage
import im.rore.countem.store.mongodb.helpers.MongoDBConnection
import im.rore.countem.utils.Configuration
import im.rore.countem.utils.Logging
import im.rore.countem.utils.Helpers
import scala.collection.mutable.HashMap
import java.util.Date
import scala.collection.JavaConversions._
import com.mongodb.DBObject
import com.mongodb.WriteConcern

/**
 * @author Rotem Hermon
 *
 * Storage class for the countem library for persisting counters in MongoDB.
 * By default we keep all the counters under a DB names "countem". 
 * Each collection is kept as a MongoDB collection. 
 * We create daily documents for the counters. 
 * Each metric is a field under the daily document.
 * 
 */
trait MongoDBStorage extends AbstractStorage with Logging {
	// allow parallel
	def storageParallelism: Int = 2
	// we keep the counters documents per day
	val DATE_FORMAT = "yyyyMMdd";
	val _dateFormat = new SimpleDateFormat(DATE_FORMAT);

	def storeCounters(collection: String, counters: Map[String, Long]) {
		val connection = new MongoDBConnection(MongoDBCollector.dbName);
		val col = connection.getCollection(collection);
		val date = getIDFromToday
		val query = MongoDBObject("_id" -> date);
		val builder = MongoDBObject.newBuilder
		counters.foreach(f => builder += f._1 -> f._2);
		val obj = MongoDBObject("$inc" -> builder.result);
		col.update(query, obj, true, false, MongoDBCollector.defaultWriteConcern);
	}

	def getAllCounters(day:Date): Map[String, Map[String, Long]] = {
		val counters = new HashMap[String, HashMap[String, Long]];
		val date = getIDFromDate(day)
		val query = MongoDBObject("_id" -> date);
		val connection = new MongoDBConnection(MongoDBCollector.dbName);
		val db = connection.getDB;
		db.getCollectionNames().foreach(colName => {
			if (null != colName && !colName.startsWith("system") && !colName.startsWith("error")) {
				val col = db.getCollection(colName);
				val obj = col.findOne(query);
				counters += (colName -> counterObjToMap(obj));
			}
		});
		return counters.map(f => (f._1, f._2.toMap)).toMap
	}

	def getCounters(collection:String, day:Date): Map[String, Long] = {
		val date = getIDFromDate(day)
		val query = MongoDBObject("_id" -> date);
		val connection = new MongoDBConnection(MongoDBCollector.dbName);
		val db = connection.getDB;
		val col = db.getCollection(collection)
		if (null != col) {
			val obj = col.findOne(query);
			val counters = counterObjToMap(obj)
			return counters.toMap
		}
		return null
	}

	protected def counterObjToMap(obj: DBObject) = {
		val map = new HashMap[String, Long]
		if (null != obj) {
			// remove the _id field
			obj.removeField("_id");
			addNestedCounters(obj, null, map)
		}
		map
	}

	protected def addNestedCounters(obj: DBObject, root: String, map: HashMap[String, Long]) {
		obj.toMap().foreach(f => {
			val fieldName = f._1.toString;
			var fieldVal = f._2;
			val rootName: String = root match {
				case null => fieldName
				case _ => root + "." + fieldName
			}
			if (fieldVal.isInstanceOf[DBObject]) {
				addNestedCounters(fieldVal.asInstanceOf[DBObject], rootName, map)
			}
			else {
				val counterVal = GetFieldVal(fieldVal)
				if (counterVal.isDefined) {
					map += rootName -> counterVal.get
				}
			}
		})
	}

	protected def GetFieldVal(fldVal: Any): Option[Long] = {
		if (null == fldVal) return None;
		if (fldVal.isInstanceOf[Int]) Some(fldVal.asInstanceOf[Int].toLong);
		else if (fldVal.isInstanceOf[Long]) Some(fldVal.asInstanceOf[Long]);
		else Some(0);
	}

	def dropDatabase = {
		val connection = new MongoDBConnection(MongoDBCollector.dbName);
		connection.getDB.dropDatabase()
	}

	def getIDFromDate(date: Date):String = {
		if (null == date) getIDFromToday
		else {
		val calendar = Calendar.getInstance();
		_dateFormat.format(calendar.getTime());
		}
	}
	def getIDFromToday:String = {
		val calendar = Calendar.getInstance();
		getIDFromDate(calendar.getTime());
	}
}

object MongoDBCollector extends Collector with MongoDBStorage {

	import Helpers._
	// connection URI to MongoDB. This should be in a MongoDB URI format - starts with mongodb:// . Can be configured using countem.mongodbConnectionUri 
	val connectionUri = Configuration.getConfig.getString("countem.mongodbConnectionUri", null);
	// The DB name to keep all counters under. Can be configured using countem.mongodbDBName
	val dbName = Configuration.getConfig.getString("countem.mongodbDBName", "countem");
	// We acknowledge all writes. If performance is preferred over error detection this can be changed
	val defaultWriteConcern = WriteConcern.ACKNOWLEDGED

	try {
		MongoDBConnection.setConnectionString(connectionUri)
	}
	catch {
		case e: Throwable => throw new Exception("Failed setting MongoDB connection Uri: " + connectionUri, e)
	}

}