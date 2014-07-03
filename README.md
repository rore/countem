countem - For counting events in a distributed system 
=======

countem is a Scala library for counting events in a distributed system.

### Yet another counting framework? ###
Yes, well, there are quite a few alternatives and frameworks for event reporting, logging etc. But sometimes what you need is something small, simple and easy to maintain, without too much overhead and administration. This is what countem was intended for.

A very similar concept was used in production for event reporting in [serendip](http://serendip.me). You can read some more about it [here](http://rore.im/posts/building-serendip/).  

### Concept ###
Counting application events in a distributed system has some pain-points.   
If you want a full view of the entire system and not just per-node data, you need the counters to be aggregated in a central location. Since there can be *a lot* of events, this central location can be overwhelmed and become a bottleneck. 

To deal with that, countem uses the following approach - 
- Persist the counters to a storage that supports atomic increments, for example MongoDB.
- Aggregate the counters in memory on each node and update them in the persistent storage on a regular interval. 
- Local aggregation is not persistent. If a node fails the counters that were counted on that node since the last storage update will be lost.

This approach means that the overhead of reporting events with countem is rather small, and it can be used on a lot of nodes for counting a lot of events without affecting the storage too much. 

There is, of course, some balance between the interval of persisting the counters and the reliability of the reports. A bigger interval means less load on the storage, but increases the danger of loosing some data if a node fails. So countem **should not** be used where data loss cannot be tolerated. 

## Usage ##
countem was designed to support multiple storage options. 

To use countem you need to include [countem-core](countem-core), in addition to a persistent storage module.
    
Currently a MongoDB module is available at [countem-store-mongodb](countem-store-mongodb).

To use countem as a Maven dependency add the following repository:
```
<repositories>
	<repository>
				<id>rore-releases</id>
				<url>https://github.com/rore/rore-repo/raw/master/releases</url>
	</repository>
</repositories>
```
Add the countem-core dependency:

```
<dependencies>
	<dependency>
				<groupId>im.rore</groupId>
				<artifactId>countem-core</artifactId>
				<version>0.0.1-SNAPSHOT</version>
	</dependency>
</dependencies>
```
And add a dependency for the desired storage module.

#### Incrementing counters ####
Updating counters is done using the *Increment(collection, metric)* method. *Collection* is a logical container for counters of the same system. *Metric* is the name of a specific counter.

For example:
```scala
// Increment by 1
MongoDBCollector.Increment("app-events", "MyMethod.calls");
// Increment by something
MongoDBCollector.Increment("app-events", "MyMethod.fetched", 5);
```

#### Getting counters ####
By default counters are collected per day (this can be changed by implementing a different storage module).

You can get counters for a specific collection and day - 
```scala
val counters = MongoDBCollector.getCounters("app-events", date);
```
Passing *null* as the date will get the counters for the current day.

You can get counters in all collections with -  
```scala
val counters = MongoDBCollector.getAllCounters(null);
```



### Some internal details ###
countem uses [akka](http://akka.io) for handling the collection of the counters.
There are two groups of actors - 
- *Collector actors* - These actors receive the increment requests. They keep an in-memory map of counters. Since an actor processes requests in a serialized order no locking is needed.
- *Storage actors* - Handles the updates in the persistent storage. Each *collector actor* sends an update request to the *storage actors* on a timed interval with the counters that where aggregated in memory.

The *collector actors* uses a resizable router. The maximum number of *collectors* can be overridden by providing setting for the "/collectActor" path using a custom akka configuration file. (See the default configuration for countem [here](countem-core/src/main/resources/reference.conf)).

A bigger number of *collectors* allows handling more events in parallel (with less queuing), but also means more hits on the storage on each update (every collector updates its own aggregated counts). 
Since updating the counters in memory by each *collector* is very fast, usually there is no need for increasing the number of *collectors*. 

