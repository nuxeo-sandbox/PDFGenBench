import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.nuxeo.bench.gen.out.AbstractFolderBatchWriter;

public class TestBatchFolders {

	protected static final int NB_CALLS = 162437;
	protected static final int NB_THREADS = 50;		
	protected static final int BATCH_SIZE = 25;
	
	protected class TestFolderBatchWriter extends AbstractFolderBatchWriter {

		AtomicInteger deleteCounter;
		
		public TestFolderBatchWriter(String folder, int batchSize, int total) {
			super(folder, batchSize, total);
			deleteCounter = new AtomicInteger(0);			
		}

		protected void batchCompledtedCB(int batch, String path) {
			File directory = new File(path);			
			String[] items = directory.list();
			
			if (items==null) {
				System.out.println("NO Items!!!!");
			}
			deleteCounter.addAndGet(items.length);
			
			try {
				FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testGen() throws Exception {

		Path folder = Files.createTempDirectory("S3Batch");
		System.out.println("Running tests in:" + folder.toString());
		
		TestFolderBatchWriter fbw = new TestFolderBatchWriter(folder.toString(), BATCH_SIZE, NB_CALLS);
		
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		executor.prestartAllCoreThreads();
		AtomicInteger counter = new AtomicInteger();
	
		final class Task implements Runnable {

			int nb;
			
			@Override
			public void run() {
				for (int i = 0; i < nb; i++) {

					int c = counter.incrementAndGet();
					String digest = String.format("TEST-%09d", c);				
					try {						
						fbw.write(digest.getBytes(), digest);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}			
		}
			
		int remainingCalls = NB_CALLS;
		int batchSize = 1+ NB_CALLS / NB_THREADS;
	
		for (int i = 0; i < NB_THREADS; i++) {
			Task t = new Task();
			if (remainingCalls> batchSize) {
				remainingCalls-=batchSize;
				t.nb = batchSize;
			} else {
				t.nb = remainingCalls;
				remainingCalls=0;
			}
			executor.execute(t);
		}		
			
		executor.shutdown();
		boolean finished = executor.awaitTermination(3*60, TimeUnit.SECONDS);
		if (!finished) {
			System.out.println("Timeout after " + counter.get() + " generations");
		}
		
		// call Flush
		fbw.flush();
		
		// check executed writes
		System.out.println("Executed Calls:" + counter.get());
		assertEquals(NB_CALLS,  counter.get());		
		System.out.println("Processed files :" + fbw.deleteCounter.get());
		assertEquals(NB_CALLS, fbw.deleteCounter.get());
		
	}

}
