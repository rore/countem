package im.rore.countem;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import scala.Option;
import scala.collection.immutable.Map;
import im.rore.countem.store.*;

public class CountemTest {

	@BeforeClass
	public static void setUp() throws Exception {
		// set the update interval for writing to the storage to 0.5 second
		System.setProperty("countem.countersUpdateIntervalSeconds", "0.5");
		MemoryCollector.init();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		MemoryCollector.Shutdown();
	}

	@Test
	public void testIncrement() throws InterruptedException {
		MemoryCollector.clearAll();
		MemoryCollector.Increment("test", "testIncrement");
		Thread.sleep(1000);
		Map<String, Object> counters = MemoryCollector.getCounters("test", null);
		Option opt = counters.get("testIncrement");
		long val = (Long) opt.get();
		assertEquals (1, val);
	}

	@Test
	public void testIncrementWithCount() throws InterruptedException {
		MemoryCollector.clearAll();
		MemoryCollector.Increment("test", "testIncrementWithCount", 2);
		Thread.sleep(1000);
		Map<String, Object> counters = MemoryCollector.getCounters("test",null);
		Option opt = counters.get("testIncrementWithCount");
		long val = (Long) opt.get();
		assertEquals (2, val);
	}

	@Test
	public void testMultipleCounters() throws InterruptedException {
		MemoryCollector.clearAll();
		MemoryCollector.Increment("test", "testMultipleCounters-1", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-2", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-2", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-3", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-3", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-3", 1);
		Thread.sleep(1000);
		Map<String, Object> counters = MemoryCollector.getCounters("test",null);
		Option opt = counters.get("testMultipleCounters-1");
		long val = (Long) opt.get();
		assertEquals (1, val);
		opt = counters.get("testMultipleCounters-2");
		val = (Long) opt.get();
		assertEquals (2, val);
		opt = counters.get("testMultipleCounters-3");
		val = (Long) opt.get();
		assertEquals (3, val);
	}

	@Test
	public void testMultipleCountersAndGetAll() throws InterruptedException {
		MemoryCollector.clearAll();
		MemoryCollector.Increment("test", "testMultipleCounters-1", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-2", 1);
		MemoryCollector.Increment("test", "testMultipleCounters-2", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-3", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-3", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-3", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-4", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-4", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-4", 1);
		MemoryCollector.Increment("test2", "testMultipleCounters-4", 1);
		Thread.sleep(1000);
		Map<String, Map<String, Object>> counters = MemoryCollector.getAllCounters(null);
		Map<String, Object> map = (Map<String, Object>) counters.get("test").get();
		Option opt = map.get("testMultipleCounters-1");
		long val = (Long) opt.get();
		assertEquals (1, val);
		opt = map.get("testMultipleCounters-2");
		val = (Long) opt.get();
		assertEquals (2, val);
		map = (Map<String, Object>) counters.get("test2").get();
		opt = map.get("testMultipleCounters-3");
		val = (Long) opt.get();
		assertEquals (3, val);
		opt = map.get("testMultipleCounters-4");
		val = (Long) opt.get();
		assertEquals (4, val);
	}

	@Test
	public void testALotOfMessages() throws InterruptedException {
		MemoryCollector.clearAll();
		for (int i = 0; i < 2000; i++) {
			MemoryCollector.Increment("test", "testALotOfMessages");
		}
		Thread.sleep(1000);
		Map<String, Object> counters = MemoryCollector.getCounters("test",null);
		Option opt = counters.get("testALotOfMessages");
		long val = (Long) opt.get();
		assertEquals (2000, val);
	}
}
