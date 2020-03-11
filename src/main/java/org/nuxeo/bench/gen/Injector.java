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

	protected int nbThreads = 10;
	protected static final int BUFFER_SIZE = 10 * 1024;

	protected static final int MAX_PAUSE = 60 * 5;

	protected int total;
	protected int callsPerThreads = 5000;
	protected final PDFFileGenerator gen;

	protected BlobWriter writer;

	protected Logger importLogger;
	protected Logger metadataLogger;

	public Injector(PDFFileGenerator gen, int total) {
		this(gen, total, 10, null, null);
	}

	public Injector(PDFFileGenerator gen, int total, int nbThreads, Logger importLogger, Logger metadataLogger) {
		this.gen = gen;
		this.total = total;
		this.nbThreads = nbThreads;
		this.callsPerThreads = Math.round(total / nbThreads) + 1;
		this.metadataLogger = metadataLogger;
		this.importLogger = importLogger;
	}

	public BlobWriter getWriter() {
		return writer;
	}

	public void setWriter(BlobWriter writer) {
		this.writer = writer;
	}

	protected void log(String message) {
		if (importLogger != null) {
			importLogger.log(Level.INFO, message);
		} else {
			System.out.println(message);
		}
	}

	protected void collect(SmtMeta meta) {
		if (metadataLogger != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(meta.getDigest());
			sb.append(",");
			sb.append(meta.getFileName());
			sb.append(",");
			sb.append(meta.getFileSize());
			for (String key : meta.getKeys()) {
				sb.append(",");
				sb.append(key);
			}
			metadataLogger.log(Level.DEBUG, sb.toString());
		}
	}

	protected void logDuration(Duration d, String message) {
		StringBuilder sb = new StringBuilder(message);
		if (d.toDaysPart() > 0)
			sb.append(d.toDaysPart()).append(" days,");
		if (d.toHoursPart() > 0)
			sb.append(d.toHoursPart()).append(" h,");
		if (d.toMinutesPart() > 0)
			sb.append(d.toMinutesPart()).append(" m,");
		sb.append(d.toSecondsPart()).append(" s");
		log(sb.toString());
	}

	public int run() throws Exception {

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nbThreads);
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
						if (writer != null) {
							writer.write(buffer.toByteArray(), meta.getDigest());
							//if (i%100==0) {
							//	writer.flush();
							//}
						}
						collect(meta);
						counter.incrementAndGet();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				writer.flush();
			}
		}

		for (int i = 0; i < nbThreads; i++) {
			executor.execute(new Task());
		}

		executor.shutdown();
		boolean finished = false;
		Long throughput;
		long pauseTimeS = 2;
		while (!finished) {

			long t1 = System.currentTimeMillis();
			long count = counter.get();
			int threads = executor.getActiveCount();

			long elapsed = (t1 - t0);
			throughput = Math.round(counter.get() * 1.0 / (elapsed / 1000));
			long percentCompleted = Math.round((count * 100.0) / total);

			log(String.format("%02d %% - %d / %d", percentCompleted, count, total));

			// log(percentCompleted + "% - " + count + "/" + total );
			log("   Throughput:" + throughput + " d/s using " + threads + " threads");

			if (throughput.intValue() > 0) {
				Duration d = Duration.ofSeconds((total - count) / throughput.intValue());
				logDuration(d, "   Projected remaining time: ");

				pauseTimeS = 1 + Math.round(d.toSeconds() / 100);
				if (pauseTimeS > MAX_PAUSE) {
					pauseTimeS = MAX_PAUSE;
				}
			}

			finished = executor.awaitTermination(pauseTimeS, TimeUnit.SECONDS);
		}

		long t1 = System.currentTimeMillis();
		throughput = Math.round(counter.get() * 1.0 / ((t1 - t0) / 1000));

		log("---=------------------------------------------------------");
		log("----------------------------------------------------------");

		log(counter.get() + " pdfs files generated.");

		Duration d = Duration.ofSeconds(((t1 - t0) / 1000));
		logDuration(d, "  Execution time: ");

		log("  Average throughput:" + throughput.intValue() + " docs/s");

		d = Duration.ofSeconds(10000000000L / throughput.intValue());
		log("[  Projected generation time for 10B files: " + d.toDaysPart() + " day(s) and " + d.toHoursPart()
				+ " hour(s)]");

		return throughput.intValue();
	}

}
