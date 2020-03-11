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
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator2;
import org.nuxeo.bench.gen.out.FolderWriter;
import org.nuxeo.bench.gen.out.TmpWriter;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class EntryPoint {

	protected static LoggerContext initLogger() {
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		
		AppenderComponentBuilder console = builder.newAppender("stdout", "Console");		
		builder.add(console);

		AppenderComponentBuilder file1 = builder.newAppender("metadata", "File");
		file1.addAttribute("fileName", "metadata.csv");
		builder.add(file1);

		AppenderComponentBuilder file2 = builder.newAppender("injector", "File");
		file2.addAttribute("fileName", "injector.log");
		builder.add(file2);

		// Use Async Logger
		RootLoggerComponentBuilder rootLogger = builder.newAsyncRootLogger(Level.INFO);
		//rootLogger.add(builder.newAppenderRef("stdout"));
		//rootLogger.add(builder.newAppenderRef("log"));		
		builder.add(rootLogger);
						
		// Use Async Logger
		LoggerComponentBuilder logger1 = builder.newAsyncLogger("metadataLogger", Level.DEBUG);
		logger1.add(builder.newAppenderRef("metadata"));
		logger1.addAttribute("additivity", false);
		builder.add(logger1);
		
		// Use Async Logger
		LoggerComponentBuilder logger2 = builder.newAsyncLogger("importLogger", Level.DEBUG);
		logger2.add(builder.newAppenderRef("injector"));
		logger2.addAttribute("additivity", false);
		builder.add(logger2);

		return Configurator.initialize(builder.build());
	}

	public static void main(String[] args) {

		LoggerContext ctx = initLogger();
				
		Logger importLogger = ctx.getLogger("importLogger");		
		Logger metadataLogger = ctx.getLogger("metadataLogger");		

		Options options = new Options();
		options.addOption("t", "threads", true, "Number of threads");
		options.addOption("n", "nbThreads", true, "Number of PDF to generate");
		options.addOption("m", "template", true, "Template: 1 or 2 (default)");
		options.addOption("o", "output", true, "output: mem(default), tmp, file:<path>, s3:<url>");
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
		int template = Integer.parseInt(cmd.getOptionValue('m', "2"));
		
		String out = cmd.getOptionValue('o', "mem");
		BlobWriter writer = null;
		if (TmpWriter.NAME.equalsIgnoreCase(out)) {
			importLogger.log(Level.INFO, "Inititialize Tmp Writer");
			writer = new TmpWriter();
		} if (out.startsWith(FolderWriter.NAME)) {
			String folder = out.substring(FolderWriter.NAME.length());
			importLogger.log(Level.INFO, "Inititialize Folder Writer in " + folder);
			writer = new FolderWriter(folder);
		}
		
		if (cmd.hasOption('h')) {
			 HelpFormatter formatter = new HelpFormatter();
		     formatter.printHelp("PDFGenerator", options);
		     return;
		}
		
		importLogger.log(Level.INFO, "Init Injector");
		importLogger.log(Level.INFO, "  Threads:" + nbThreads);
		importLogger.log(Level.INFO, "  pdfs:" + nbPdfs);

		try {
			runInjector(nbPdfs, nbThreads, template, importLogger, metadataLogger, writer);
		} catch (Exception e) {
			System.err.println("Error while running Injector " + e);
			e.printStackTrace();
		}
	}

	protected static void runInjector(int total, int threads, int template, Logger importLogger, Logger metadataLogger, BlobWriter writer) throws Exception {

		// Data Generator
		RandomDataGenerator rnd = null;
		ITextNXBankTemplateCreator templateGen = null;
		
		importLogger.log(Level.INFO, "using template " + template);
		
		if (template==1) {
			rnd = new RandomDataGenerator(false);
			templateGen = new ITextNXBankTemplateCreator();
		} else {
			rnd = new RandomDataGenerator(true);
			templateGen= new ITextNXBankTemplateCreator2();
		}

		// init random data generator
		InputStream csv = EntryPoint.class.getResourceAsStream("/data.csv");
		rnd.init(csv);

		// Generate the template
		InputStream logo = EntryPoint.class.getResourceAsStream("/NxBank3.png");
		templateGen.init(logo);

		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
		templateGen.generate(templateOut);
		byte[] templateData = templateOut.toByteArray();

		// Init PDF generator
		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), templateGen.getKeys());
		gen.computeDigest = true;
		gen.setRndGenerator(rnd);

		Injector injector = new Injector(gen, total, threads, importLogger, metadataLogger);
		
		injector.setWriter(writer);
		
		injector.run();

	}
}
