/**
 * 
 */
package im.rore.countem.store.mongodb;

import static org.junit.Assert.*;
import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Option;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map;


/**
 * @author Rotem Hermon
 *
 */
public class MongoDBStorageTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// set the update interval for writing to the storage to 0.5 second
		System.setProperty("countem.countersUpdateIntervalSeconds", "0.5");
		System.setProperty("countem.mongodbConnectionUri", "mongodb://127.0.0.1");
		MongoDBCollector.init();
		MongoDBCollector.dropDatabase();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		MongoDBCollector.Shutdown();
	}

	 public static <A, B> Map<A, B> toScalaMap(HashMap<A, B> m) {
		    return JavaConverters.mapAsScalaMapConverter(m).asScala().toMap(
		      Predef.<Tuple2<A, B>>conforms()
		    );
		  }
	@Test
	public void testStorage() throws InterruptedException {
		HashMap<String,Object> update = new HashMap<String,Object>(); 
		update.put("testFlat", 1L);
		update.put("test.nested", 1L);
		update.put("test.deep.1", 1L);
		update.put("test.deep.2", 1L);
		MongoDBCollector.storeCounters("manual", toScalaMap(update));
		Map<String, Object> counters = MongoDBCollector.getCounters("manual",null);
		Option opt = counters.get("testFlat");
		long val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.nested");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.1");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.2");
		val = (Long) opt.get();
		assertEquals (1, val);
	}

	@Test
	public void testGetAllTodayCounters() throws InterruptedException {
		HashMap<String,Object> update = new HashMap<String,Object>(); 
		update.put("testFlat", 1L);
		update.put("test.nested", 1L);
		update.put("test.deep.1", 1L);
		update.put("test.deep.2", 1L);
		MongoDBCollector.storeCounters("t1", toScalaMap(update));
		MongoDBCollector.storeCounters("t2", toScalaMap(update));
		Map<String,Map<String,Object>> allCounters = MongoDBCollector.getAllCounters(null);
		Option col = allCounters.get("t1");
		Map<String,Object> counters = (Map<String,Object>)col.get();
		Option opt = counters.get("testFlat");
		long val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.nested");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.1");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.2");
		val = (Long) opt.get();
		assertEquals (1, val);
		col = allCounters.get("t2");
		counters = (Map<String,Object>)col.get();
		opt = counters.get("testFlat");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.nested");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.1");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.deep.2");
		val = (Long) opt.get();
		assertEquals (1, val);
	}

	@Test
	public void testIncrement() throws InterruptedException {
		MongoDBCollector.Increment("test", "testFlat");
		MongoDBCollector.Increment("test", "test.nested");
		MongoDBCollector.Increment("test", "test.first.second");
		Thread.sleep(1000);
		Map<String, Object> counters = MongoDBCollector.getCounters("test",null);
		Option opt = counters.get("testFlat");
		long val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.nested");
		val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("test.first.second");
		val = (Long) opt.get();
		assertEquals (1, val);
	}
	
	@Test
	public void testIncrementWithCount() throws InterruptedException {
		MongoDBCollector.Increment("test", "testIncrementWithCount", 2);
		Thread.sleep(1000);
		Map<String, Object> counters = MongoDBCollector.getCounters("test",null);
		Option opt = counters.get("testIncrementWithCount");
		long val = (Long) opt.get();
		assertEquals (2, val);
	}

}
