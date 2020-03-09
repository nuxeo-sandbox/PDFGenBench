package org.nuxeo.bench.gen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.nuxeo.bench.gen.itext.ITextNXBankStatementGenerator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class EntryPoint {

	protected static LoggerContext initLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console");		
		builder.add(console);

		AppenderComponentBuilder file = builder.newAppender("log", "File");
		file.addAttribute("fileName", "injector.log");
		builder.add(file);

		// Use Async Logger
		RootLoggerComponentBuilder rootLogger = builder.newAsyncRootLogger(Level.INFO);
		rootLogger.add(builder.newAppenderRef("stdout"));
		rootLogger.add(builder.newAppenderRef("log"));		
		builder.add(rootLogger);
						
		//
		LoggerComponentBuilder logger = builder.newAsyncLogger("import", Level.DEBUG);
		logger.add(builder.newAppenderRef("log"));
		logger.addAttribute("additivity", false);

		builder.add(logger);

		return Configurator.initialize(builder.build());
	}

	public static void main(String[] args) {

		LoggerContext ctx = initLogger();
				
		Logger rootLogger = ctx.getRootLogger();		
		Logger logger = ctx.getLogger("import");		

		Options options = new Options();
		options.addOption("t", "threads", true, "Number of threads");
		options.addOption("n", "nbThreads", true, "Number of PDF to generate");
		options.addOption("h", "help", false, "Help");
		
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

		int nbThreads = Integer.parseInt(cmd.getOptionValue('t', "10"));
		int nbPdfs = Integer.parseInt(cmd.getOptionValue('n', "100000"));

		if (cmd.hasOption('h')) {
			 HelpFormatter formatter = new HelpFormatter();
		     formatter.printHelp("PDFGenerator", options);
		     return;
		}
		
		rootLogger.log(Level.INFO, "Init Injector");
		rootLogger.log(Level.INFO, "  Threads:" + nbThreads);
		rootLogger.log(Level.INFO, "  pdfs:" + nbPdfs);

		try {
			runInjector(nbPdfs, nbThreads, rootLogger, logger);
		} catch (Exception e) {
			System.err.println("Error while running Injector " + e);
			e.printStackTrace();
		}
	}

	protected static void runInjector(int total, int threads, Logger rootLogger, Logger logger) throws Exception {

		// Data Generator
		RandomDataGenerator rnd = new RandomDataGenerator();
		InputStream csv = EntryPoint.class.getResourceAsStream("/data.csv");
		rnd.init(csv);

		// Generate the template
		InputStream logo = EntryPoint.class.getResourceAsStream("/NxBank3.png");
		ITextNXBankTemplateCreator templateGen = new ITextNXBankTemplateCreator();
		templateGen.init(logo);

		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
		templateGen.generate(templateOut);
		byte[] templateData = templateOut.toByteArray();

		// Init PDF generator
		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), ITextNXBankTemplateCreator.KEYS);
		gen.computeDigest = true;
		gen.setRndGenerator(rnd);

		Injector injector = new Injector(gen, total, threads, rootLogger, logger);
		injector.run();

	}
}
