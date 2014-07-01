package im.rore.countem.store.mongodb.helpers

import scala.collection.JavaConversions.bufferAsJavaList
import scala.collection.mutable.ListBuffer
import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.ServerAddress
import im.rore.countem.utils.Logging
import com.mongodb.MongoClientURI

class MongoDBConnection(dbName: String) extends Logging {

	def getCollection(collectionName: String): DBCollection = {
			// Create the connection
			val mongoConn = MongoDBConnection.getConnection(false, MongoDBConnection.defaultOptions)
			// Create the DB
			val mongoDB = mongoConn.getDB(dbName)
			var col = mongoDB.getCollection(collectionName);
			return col;
		}

	def getDB : DB = {
		getDB(true, MongoDBConnection.defaultOptions);
	}
	
	def getDB(reuse: Boolean, options: MongoClientOptions.Builder): DB = {
		// Create the connection
		val mongoConn = MongoDBConnection.getConnection(!reuse, options)
		// Create the DB
		val mongoDB = mongoConn.getDB(dbName)
		mongoDB;
	}

	def closeConnection = MongoDBConnection.closeConnection

}

object MongoDBConnection extends Logging {

	val defaultOptions = MongoClientOptions.builder()
		.connectionsPerHost(25)
		.maxWaitTime(1000 * 30)
		.connectTimeout(1000 * 60)
		.socketTimeout(1000 * 60)

	private var mongoConnection: MongoClient = null;
	private var mongoUri : MongoClientURI = null;

	def setConnectionString(conStr: String) {
		if (null != mongoConnection) {
			closeConnection
		}
		// create here the URI to verify it's valid
		// we'll create it again later with the options
		mongoUri = new MongoClientURI(conStr)
	}

	def closeConnection =
		{
			if (null != mongoConnection) {
				try {
					mongoConnection.close();
				}
				catch {
					case e: Throwable =>
				}
			}
		}

	private def getConnection(createNew: Boolean = false, options: MongoClientOptions.Builder): MongoClient =
		{
			var newCon: MongoClient = null;
			if (!createNew && null != mongoConnection) {
				return mongoConnection;
			}
			if (null == mongoUri) throw new Exception("MongoDB Connection URI is empty")
			try {
				val uri = new MongoClientURI(mongoUri.getURI(), options)
				newCon = new MongoClient(mongoUri);
			}
			catch {
				case e: Throwable => throw e
			}
			if (!createNew) mongoConnection = newCon;
			return newCon;
		}

}