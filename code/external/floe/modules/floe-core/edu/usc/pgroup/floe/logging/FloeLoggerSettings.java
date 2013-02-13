package edu.usc.pgroup.floe.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Class to initialize Java {@link java.util.logging.Logger} settings.
 * 
 * @author Srivathsan (srivathr@usc.edu)
 * 
 */
public class FloeLoggerSettings {
	static private FileHandler fileHandler;
	static private SimpleFormatter simpleFormatter;
	static {
		simpleFormatter = new SimpleFormatter();
	}

	/**
	 * Method to initialize logger settings by specifying a log file.
	 * 
	 * @param logFile
	 * @param logLevel
	 * @throws SecurityException
	 * @throws IOException
	 */
	public static void init(String logFile, Level logLevel)
			throws SecurityException, IOException {
		Logger logger = Logger.getLogger("");
		logger.setLevel(logLevel);

		if (logFile != null) {
			fileHandler = new FileHandler(logFile);
			fileHandler.setFormatter(simpleFormatter);
			logger.addHandler(fileHandler);
		}
	}
}
