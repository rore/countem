package im.rore.countem.utils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import com.typesafe.config.ConfigException

/**
 * @author Rotem Hermon
 *
 */
object Configuration extends Logging {

	protected val config = ConfigFactory.load()

	def getActorSystem(name: String) = {
		ActorSystem(name, config.getConfig("countem"));
	}
	
	def getConfig = config
}