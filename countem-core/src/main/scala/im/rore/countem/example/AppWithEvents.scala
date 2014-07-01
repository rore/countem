package im.rore.countem.example

import im.rore.countem.Collector
import im.rore.countem.store.MemoryStorage
import im.rore.countem.store.MemoryCollector

object AppWithEvents extends App {
	// set the update interval so the counters will persist in memory
	System.setProperty("countem.countersUpdateIntervalSeconds", "10");
	Console.println("Starting");
	for (n <- 1 to 200) {
		for (i <- 1 to 10) {
			MemoryCollector.Increment("AppWithEvents", (i).toString);
		}
	}
	MemoryCollector.Increment("AppWithEvents", "close", 3);
	Console.println("Done");
	Thread.sleep(1000 * 20)
	Console.println("Counters: " + MemoryCollector.getAllCounters(null))
	MemoryCollector.Shutdown
}