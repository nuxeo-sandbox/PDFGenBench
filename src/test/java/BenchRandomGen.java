import static org.junit.Assert.assertEquals;

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
import org.nuxeo.bench.blob.KeyCodec;
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
	public void testEncodeNumbersAsString() {
		
		String max = KeyCodec.encode(Long.MAX_VALUE);		
		//System.out.println(max);
		assertEquals(Long.MAX_VALUE, KeyCodec.decode(max));
		
		String min = KeyCodec.encode(Long.MIN_VALUE);		
		//System.out.println(min);
		assertEquals(Long.MIN_VALUE, KeyCodec.decode(min));
		
		
		max = KeyCodec.encode(Integer.MAX_VALUE);		
		//System.out.println(max);
		assertEquals(Integer.MAX_VALUE, KeyCodec.decode(max));
		
		min = KeyCodec.encode(Integer.MIN_VALUE);		
		//System.out.println(min);
		assertEquals(Integer.MIN_VALUE, KeyCodec.decode(min));
		
		
		Random rnd = new Random();
		for (int x = 0; x < 1000; x++) {
			long l = rnd.nextLong();
			String k = KeyCodec.encode(l);
			//System.out.println(k);
			assertEquals(l, KeyCodec.decode(k));
			
			int i = rnd.nextInt();
			k = KeyCodec.encode(i);
			//System.out.println(k);
			assertEquals(i, KeyCodec.decode(k));						
		}
			
		
		// "1677235490412433516:0709902258027561473:0018"
		long s1 = 1677235490412433516L;
		long s2 = 709902258027561473L;
		int m = 18;
		
		String key = KeyCodec.encodeSeeds(s1, s2, m);
		System.out.println("1677235490412433516:0709902258027561473:0018");
		System.out.println(key);
		long[] s = KeyCodec.decodeSeeds(key);
		assertEquals(s1, s[0]);
		assertEquals(s2, s[1]);
		assertEquals(m, s[2]);
		
		

	}
}
