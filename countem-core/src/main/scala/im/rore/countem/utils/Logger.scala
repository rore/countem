package im.rore.countem.utils
/**
 * Scala front-end to SLF4J API.
 */

import org.slf4j.{Logger => SLF4JLogger}

/**
 * Scala front-end to a SLF4J logger.
 */
class Logger(val logger: SLF4JLogger)
{
    /**
     * Get the name associated with this logger.
     *
     * @return the name.
     */
    def name = logger.getName

    /**
     * Determine whether trace logging is enabled.
     */
    def isTraceEnabled = logger.isTraceEnabled

    /**
     * Issue a trace logging message.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     */
    def trace(msg: => AnyRef): Unit =
        if (isTraceEnabled) logger.trace(msg.toString)

    /**
     * Issue a trace logging message, with an exception.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     * @param t    the exception to include with the logged message.
     */
    def trace(msg: => AnyRef, t: => Throwable): Unit =
        if (isTraceEnabled) logger.trace(msg.toString, t)

    /**
     * Determine whether debug logging is enabled.
     */
    def isDebugEnabled = logger.isDebugEnabled

    /**
     * Issue a debug logging message.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     */
    def debug(msg: => AnyRef): Unit =
        if (isDebugEnabled) logger.debug(msg.toString)

    /**
     * Issue a debug logging message, with an exception.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     * @param t    the exception to include with the logged message.
     */
    def debug(msg: => AnyRef, t: => Throwable): Unit =
        if (isDebugEnabled) logger.debug(msg.toString, t)

    /**
     * Determine whether trace logging is enabled.
     */
    def isErrorEnabled = logger.isErrorEnabled

    /**
     * Issue a trace logging message.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     */
    def error(msg: => AnyRef): Unit =
        if (isErrorEnabled) logger.error(msg.toString)

    /**
     * Issue a trace logging message, with an exception.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     * @param t    the exception to include with the logged message.
     */
    def error(msg: => AnyRef, t: => Throwable): Unit =
        if (isErrorEnabled) logger.error(msg.toString, t)

    /**
     * Determine whether trace logging is enabled.
     */
    def isInfoEnabled = logger.isInfoEnabled

    /**
     * Issue a trace logging message.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     */
    def info(msg: => AnyRef): Unit =
        if (isInfoEnabled) logger.info(msg.toString)

    /**
     * Issue a trace logging message, with an exception.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     * @param t    the exception to include with the logged message.
     */
    def info(msg: => AnyRef, t: => Throwable): Unit =
        if (isInfoEnabled) logger.info(msg.toString, t)

    /**
     * Determine whether trace logging is enabled.
     */
    def isWarnEnabled = logger.isWarnEnabled

    /**
     * Issue a trace logging message.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     */
    def warn(msg: => AnyRef): Unit =
        if (isWarnEnabled) logger.warn(msg.toString)

    /**
     * Issue a trace logging message, with an exception.
     *
     * @param msg  the message object. `toString()` is called to convert it
     *             to a loggable string.
     * @param t    the exception to include with the logged message.
     */
    def warn(msg: => AnyRef, t: => Throwable): Unit =
        if (isWarnEnabled) logger.warn(msg.toString, t)
}

/**
 * A factory for retrieving an SLF4JLogger.
 */
object Logger
{
    /**
     * The name associated with the root logger.
     */
    //val RootLoggerName = SLF4JLogger.ROOT_LOGGER_NAME
	val RootLoggerName = "serendip";

    /**
     * Get the logger with the specified name. Use `RootName` to get the
     * root logger.
     *
     * @param name  the logger name
     *
     * @return the `Logger`.
     */
    def apply(name: String): Logger =
        new Logger(org.slf4j.LoggerFactory.getLogger(name))

    /**
     * Get the logger for the specified class, using the class's fully
     * qualified name as the logger name.
     *
     * @param cls  the class
     *
     * @return the `Logger`.
     */
    def apply(cls: Class[_]): Logger = apply(cls.getName)
    def apply(obj: AnyRef): Logger = apply(obj.getClass)
    /**
     * Get the root logger.
     *
     * @return the root logger
     */
    val rootLogger = apply(RootLoggerName)
    var reportToErrorLog = true;
    
    def trace(msg:String)= rootLogger.trace(msg)
    def trace(msg:String, t:Throwable) = rootLogger.trace(msg,t)
    def debug(msg:String) = rootLogger.debug(msg)
    def debug(msg:String, t:Throwable) = rootLogger.debug(msg,t)
    def error(msg:String) = {
    	rootLogger.error(msg)
    }
    def error(msg:String, t:Throwable) = {
		rootLogger.error(msg,t)
	}
    def info(msg:String) = rootLogger.info(msg)
    def info(msg:String, t:Throwable) = rootLogger.info(msg,t)
    def warn(msg:String) = rootLogger.warn(msg)
    def warn(msg:String, t:Throwable) = rootLogger.warn(msg,t)

    // Java interface
    
    def logError(msg:String, t:Throwable=null){
    	rootLogger.error(msg,t);
    }
    def logInfo(msg:String) = rootLogger.info(msg)
    def logWarn(msg:String) = rootLogger.warn(msg)
    def logDebug(msg:String) = rootLogger.debug(msg)
}

trait Logging {
	private val logger = Logger(getClass)
	protected def reportToErrorLog = true;
	
    def trace(msg: => AnyRef)= logger.trace(msg)
    def trace(msg: => AnyRef, t: => Throwable) = logger.trace(msg,t)
    def debug(msg: => AnyRef) = logger.debug(msg)
    def debug(msg: => AnyRef, t: => Throwable) = logger.debug(msg,t)
    def error(msg:String) = {
		val s = msg.toString;
    	logger.error(s)
    }
    def error(t: => Throwable) = {
		val s = "exception";
    	logger.error(s, t)
    }
    def error(msg: => AnyRef, t: => Throwable) = {
		val s = msg.toString;
		logger.error(msg,t)
	}
    def info(msg: => AnyRef) = logger.info(msg)
    def info(msg: => AnyRef, t: => Throwable) = logger.info(msg,t)
    def warn(msg: => AnyRef) = logger.warn(msg)
    def warn(msg: => AnyRef, t: => Throwable) = logger.warn(msg,t)
}

