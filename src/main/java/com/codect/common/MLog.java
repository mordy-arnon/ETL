package com.codect.common;

import org.apache.log4j.Logger;

/**
 * logger wrapper.
 * better performance logger in one line.
 * check the logger level before concatenate the out String.
 * <code>MLog.debug(this, "firstObject:%s. secondObject:%s.",firstObject,secondObject);</code>
 * 
 * @author Mordy
 */
public class MLog {

    /**
     * log message in level info.
     * build the string only if needed.
     *  
     * @param logInstance
     * @param msg
     * @param params
     */
    public static void info(Object logInstance, String msg, Object... params) {
        Logger log = Logger.getLogger(logInstance.getClass().getName());
        if (log.isInfoEnabled())
            log.info(String.format(msg, params));
    }

    /**
     * log message in level error. 
     * 
     * @param logInstance
     * @param e - the exception to be logged
     * @param msg
     * @param params
     */
    public static void error(Object logInstance, Exception e, String msg, Object... params) {
        Logger log = Logger.getLogger(logInstance.getClass().getName());
        log.error(String.format(msg, params), e);
    }

    /**
     * log message in level info.
     * build the string only if needed.
     * 
     * @param logInstance
     * @param msg
     * @param params
     */
    public static void debug(Object logInstance, String msg, Object... params) {
        Logger log = Logger.getLogger(logInstance.getClass().getName());
        if (log.isDebugEnabled())
            log.info(String.format(msg, params));
    }

    /**
     * @param logInstance
     * @param e
     * @param string
     */
    public static void warn(Object logInstance, Exception e, String msg,Object ... params) {
        Logger log = Logger.getLogger(logInstance.getClass().getName());
        log.warn(String.format(msg, params),e);
    }

    /**
     * @param logInstance
     * @param msg
     * @param params
     */
    public static void trace(Object logInstance, String msg, Object... params) {
        Logger log = Logger.getLogger(logInstance.getClass().getName());
        if (log.isTraceEnabled())
            log.trace(String.format(msg, params));
    }

}
