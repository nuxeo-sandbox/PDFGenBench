import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class BenchRandomGen {

	protected static final int NB_CALLS = 250000;
	protected static final int NB_THREADS = 10;
		
	
	@Test
	public void testGen() throws Exception {

		RandomDataGenerator gen = new RandomDataGenerator(false);

		URL url = this.getClass().getResource("data.csv");
		File csv = new File(url.toURI());
		gen.init(csv);
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		executor.prestartAllCoreThreads();
		AtomicInteger counter = new AtomicInteger();

		
		long t0 = System.currentTimeMillis();
				
		final class Task implements Runnable {

			@Override
			public void run() {

				for (int i = 0; i < NB_CALLS; i++) {
					String[] result = gen.generate();
					counter.incrementAndGet();
				}
			}			
		}
				
		for (int i = 0; i < NB_THREADS; i++) {
			executor.execute(new Task());
		}		
			
		executor.shutdown();
		boolean finished = executor.awaitTermination(3*60, TimeUnit.SECONDS);
		if (!finished) {
			System.out.println("Timeout after " + counter.get() + " generations");
		}
		
		long t1 = System.currentTimeMillis();
		
		Double throughput = counter.get() * 1.0 /((t1-t0)/1000);		

		System.out.println("Throughput:" + throughput);
	}

	protected SimpleDateFormat df = new SimpleDateFormat("MMM dd YYYY");
	protected static final int DR = 5 * 365 * 24 * 3600 * 1000;

	@Test
	public void testDate() {
		
		for (int dm = 24; dm > 0; dm--) {
			
			int dy = dm/12;
			int m = dm - dy*12;
			System.out.println("dy =" + dy + " -- m =" + m);
			Date date = new GregorianCalendar(2020-dy, m, 27).getTime();
			System.out.println(df.format(date));
			System.out.println(LocalDate.of( 2020-dy, m+1, 27 ).format(DateTimeFormatter.ofPattern("MMM dd YYYY")));
			//System.out.println(df.format(LocalDate.of( 2020-dy, m+1, 27 )));
		}
		
	}
	
	@Test
	public void testRandomSequence() {
		
	    Random random = new Random(1000L);

	    System.out.println(random.nextDouble());
	    System.out.println(random.nextDouble());
	    System.out.println(random.nextDouble());
	}
}
