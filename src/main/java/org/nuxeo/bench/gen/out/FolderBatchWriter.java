package org.nuxeo.bench.gen.out;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.nuxeo.bench.gen.BlobWriter;

public abstract class FolderBatchWriter implements BlobWriter {

	public static final String NAME = "file:";

	protected File rootFolder;

	protected final int batchSize;

	protected AtomicInteger counter;

	protected int margin = 0;
	
	protected ThreadPoolExecutor completionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	public FolderBatchWriter(String folder, int batchSize, int total) {
		this.rootFolder = new File(folder);
		this.batchSize = batchSize;
		counter = new AtomicInteger(0);
		allocateBatchFolder(batchSize, total);
		margin = batchSize/2;
		
		completionExecutor.setCorePoolSize(1);
		completionExecutor.setMaximumPoolSize(1);
		completionExecutor.prestartAllCoreThreads();

	}

	protected String getFolderName(int batch) {
		return String.format("batch-%010d", batch);
	}

	protected void allocateBatchFolder(int batchSize, int total) {
		int nbBatches = total / batchSize + 1;

		for (int i = 1; i <= nbBatches; i++) {
			String folderName = getFolderName(i);
			File batchFolder = new File(rootFolder, folderName);
			batchFolder.mkdir();
		}
	}

	protected abstract void batchCompledtedCB(int batch, String path);
	
	protected void postProcessBatch(int batch, String path) {
		completionExecutor.execute( new Runnable() {				
			@Override
			public void run() {				
				batchCompledtedCB(batch, path);				
			}});
	}

	protected class Batch {
		int idx;
		int batchId;
		
		public Batch(int idx, int batchId) {
			this.idx = idx;
			this.batchId = batchId;
		}		
	}
	
	protected Batch getCurrentBatchId() {
		int idx = counter.incrementAndGet();
		int batchId = 1 +  idx / batchSize;		
		return new Batch(idx, batchId);
	}
	
	
	
	protected void fileCompleted(Batch batch) {		
		if ((batch.idx > margin) && (batch.idx % batchSize == margin)) {
			postProcessBatch(batch.batchId - 1, getBatchFolderPath(batch.batchId-1));
		}
	}

	protected String getBatchFolderPath(int batchId) {
		return rootFolder.getAbsolutePath().concat("/" + getFolderName(batchId));
	}
	
	@Override
	public void write(byte[] data, String digest) throws Exception {
		Batch batch = getCurrentBatchId();
		String directory = getBatchFolderPath(batch.batchId);
		Path path = Path.of(directory, digest);
		Files.copy(new ByteArrayInputStream(data), path);		
		fileCompleted(batch);	
	}

	@Override
	public void flush() {
		Batch batch = getCurrentBatchId();
		postProcessBatch(batch.batchId, getBatchFolderPath(batch.batchId));
		
		completionExecutor.shutdown();
		 
		try {
			boolean finished =completionExecutor.awaitTermination(10, TimeUnit.SECONDS);
			while (!finished) {
				System.out.println("Waiting for CB execution to finish - " + completionExecutor.getQueue().size() + " pending CB");
				finished =completionExecutor.awaitTermination(10, TimeUnit.SECONDS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
