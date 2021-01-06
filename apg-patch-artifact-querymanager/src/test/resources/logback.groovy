import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.INFO

appender("Console-Appender", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = "%msg%n"
	}
}

logger("Aether Query on Local Repo", INFO, ["Console-Appender"])
logger("Aether Transfer to Local Repo", INFO, ["Console-Appender"])
root(INFO, ["Console-Appender"]) 