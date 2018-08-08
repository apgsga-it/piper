import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.DEBUG 


appender("Console-Appender", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%msg%n"
	}
}

logger("Aether Query on Local Repo", DEBUG, ["Console-Appender"])
root(INFO, ["Console-Appender"])