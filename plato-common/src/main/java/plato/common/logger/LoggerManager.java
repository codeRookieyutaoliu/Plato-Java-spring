package plato.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerManager {
    private static final Logger SYSTEM_LOGGER = LoggerFactory.getLogger("system");
    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("access");
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("error");
    private static final Logger BUSINESS_LOGGER = LoggerFactory.getLogger("business");

    public void info(String message) {
        SYSTEM_LOGGER.info(message);
    }

    public void info(String format, Object... arguments) {
        SYSTEM_LOGGER.info(format, arguments);
    }

    public void warn(String message) {
        SYSTEM_LOGGER.warn(message);
    }

    public void warn(String format, Object... arguments) {
        SYSTEM_LOGGER.warn(format, arguments);
    }

    public void error(String message) {
        ERROR_LOGGER.error(message);
    }

    public void error(String message, Throwable throwable) {
        ERROR_LOGGER.error(message, throwable);
    }

    public void error(String format, Object... arguments) {
        ERROR_LOGGER.error(format, arguments);
    }

    public void access(String message) {
        ACCESS_LOGGER.info(message);
    }

    public void access(String format, Object... arguments) {
        ACCESS_LOGGER.info(format, arguments);
    }

    public void business(String message) {
        BUSINESS_LOGGER.info(message);
    }

    public void business(String format, Object... arguments) {
        BUSINESS_LOGGER.info(format, arguments);
    }

    public Logger getSystemLogger() {
        return SYSTEM_LOGGER;
    }

    public Logger getAccessLogger() {
        return ACCESS_LOGGER;
    }

    public Logger getErrorLogger() {
        return ERROR_LOGGER;
    }

    public Logger getBusinessLogger() {
        return BUSINESS_LOGGER;
    }
} 