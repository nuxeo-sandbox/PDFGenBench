import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.nuxeo.bench.gen.out.FolderBatchWriterWithCmdCB;

public class TestBatchFoldersCB {

	protected static final int NB_CALLS = 5000;
	protected static final int NB_THREADS = 10;		
	protected static final int BATCH_SIZE = 25;
	
	protected static final String[] cmd = {"bash", "-c", "ls -l %dir% | wc -l"};
	
	@Test
	public void testBatchFolderCB() throws Exception {

		Path folder = Files.createTempDirectory("S3Batch");
		System.out.println("Running tests in:" + folder.toString());
		
		FolderBatchWriterWithCmdCB fbw = new FolderBatchWriterWithCmdCB(folder.toString(), BATCH_SIZE, NB_CALLS, cmd);
		
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
		
		List<String> out = fbw.getStdOut();
		int linesCounter = 0;
		for (String line : out) {
			linesCounter+= Integer.parseInt(line);
		}
		assertEquals(NB_CALLS,  linesCounter);			
		//System.out.print(Arrays.toString(out.toArray()));
		
	}

}
