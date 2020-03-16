package org.nuxeo.bench.gen.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class FolderBatchWriterWithCmdCB extends AbstractFolderBatchWriter {

	protected ProcessBuilder pbuilder = new ProcessBuilder();
	
	public FolderBatchWriterWithCmdCB(String folder, int batchSize, int total) {
		super(folder, batchSize, total);
	}

	String[] getCmd() {
		return null;
	}
	
	@Override
	protected void batchCompledtedCB(int batch, String path) {
		
		pbuilder.command(getCmd());
		pbuilder.directory(new File(path));		
		try {
			Process process = pbuilder.start();		
			Executors.newSingleThreadExecutor().submit(new Runnable() {
				@Override
				public void run() {
					Stream<String> lines = new BufferedReader(new InputStreamReader(process.getInputStream())).lines();
				}
			});
			int exitCode = process.waitFor();
		} catch (Exception e ) {
			
		}
	}

}
