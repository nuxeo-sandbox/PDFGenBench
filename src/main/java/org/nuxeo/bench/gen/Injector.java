package org.nuxeo.bench.gen;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.nuxeo.bench.gen.smt.SmtMeta;

public class Injector {

	protected int NB_THREADS = 10;
	protected static final int BUFFER_SIZE = 10 * 2014;;

	protected int total;
	protected int callsPerThreads = 5000;
	protected final PDFFileGenerator gen;

	protected Logger rootLogger;
	protected Logger logger;

	public Injector(PDFFileGenerator gen, int total) {
		this(gen, total, 10, null, null);
	}

	public Injector(PDFFileGenerator gen, int total, int nbThreads, Logger rootLogger, Logger logger) {
		this.gen = gen;
		this.total = total;
		this.NB_THREADS = nbThreads;
		this.callsPerThreads = Math.round(total / NB_THREADS) + 1;
		this.logger = logger;
		this.rootLogger = rootLogger;
	}

	protected void log(String message) {
		if (rootLogger != null) {
			rootLogger.log(Level.INFO, message);
		} else {
			System.out.println(message);
		}
	}

	protected void log (SmtMeta meta) {
		if (logger!=null) {
			StringBuffer sb = new StringBuffer();
			sb.append(meta.getDigest());
			for (String key: meta.getKeys()) {
				sb.append(",");
				sb.append(key);
			}			
			logger.log(Level.DEBUG,sb.toString());
		}
	}
	
	public int run() throws Exception {

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		executor.prestartAllCoreThreads();
		AtomicInteger counter = new AtomicInteger();
		AtomicInteger genSize = new AtomicInteger();

		log("----------------------------------------------------------");

		long t0 = System.currentTimeMillis();

		final class Task implements Runnable {

			@Override
			public void run() {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream(BUFFER_SIZE);
				for (int i = 0; i < callsPerThreads; i++) {
					try {
						buffer.reset();
						SmtMeta meta = gen.generate(buffer);
						log(meta);
						counter.incrementAndGet();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (int i = 0; i < NB_THREADS; i++) {
			executor.execute(new Task());
		}

		executor.shutdown();
		boolean finished = false;
		Long throughput;
		while (!finished) {

			long t1 = System.currentTimeMillis();
			long count = counter.get();
			int threads = executor.getActiveCount();

			throughput = Math.round(counter.get() * 1.0 / ((t1 - t0) / 1000));
			log(count + "/" + total + " (" + throughput + " d/s using " + threads + " threads)");

			finished = executor.awaitTermination(20, TimeUnit.SECONDS);
		}

		long t1 = System.currentTimeMillis();
		throughput = Math.round(counter.get() * 1.0 / ((t1 - t0) / 1000));

		log("  Files: " + counter.get() + " pdfs --- " + throughput.intValue() + " docs/s");

		log("\n  Projected generation time for 10B files: ");
		Duration d = Duration.ofSeconds(10000000000L / throughput.intValue());
		log(d.toDaysPart() + " day(s) and " + d.toHoursPart() + " hour(s)");

		return throughput.intValue();
	}

}