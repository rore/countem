package im.rore.countem.utils

import com.typesafe.config.Config
import com.typesafe.config.ConfigException

object Helpers {
	implicit class ConfigExtension(config: Config) {

		def getString(key: String): String = {
			try {
				return config.getString(key)
			}
			catch {
				case e: ConfigException.Missing => return null;
			}
		}

		def getString(key: String, defVal:String): String = {
			try {
				return config.getString(key)
			}
			catch {
				case e: ConfigException.Missing => return defVal;
			}
		}
		def getLong(key: String, defVal: Long): Long = {
			try {
				return config.getLong(key)
			}
			catch {
				case e: ConfigException.Missing => return defVal;
			}
		}
		
		def getDouble(key: String, defVal: Double): Double = {
			try {
				return config.getDouble(key)
			}
			catch {
				case e: ConfigException.Missing => return defVal;
			}
		}

	}
}