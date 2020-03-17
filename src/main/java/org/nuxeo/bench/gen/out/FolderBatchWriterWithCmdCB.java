package org.nuxeo.bench.gen.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FolderBatchWriterWithCmdCB extends AbstractFolderBatchWriter {

	protected ProcessBuilder pbuilder = new ProcessBuilder();

	protected final String[] cmd;

	protected List<String> stdOut;

	protected ExecutorService ste;

	public FolderBatchWriterWithCmdCB(String folder, int batchSize, int total, String[] cmd) {
		super(folder, batchSize, total);
		this.cmd = cmd;
		stdOut = new ArrayList<String>();
		ste = Executors.newSingleThreadExecutor();
	}

	protected String[] getCmd() {
		return cmd;
	}

	public List<String> getStdOut() {
		return stdOut;
	}

	@Override
	protected void batchCompledtedCB(int batch, String path) {

		String[] cmds = getCmd();

		for (int i = 0; i < cmds.length; i++) {
			cmds[i] = cmds[i].replace("%dir%", path);
		}

		pbuilder.command(getCmd());
		pbuilder.directory(new File(path));

		try {
			Process process = pbuilder.start();
			ProcessReader pr = new ProcessReader(process);
			ste.submit(pr);
			// boolean finished = ste.awaitTermination(1, TimeUnit.SECONDS);
			int exitCode = process.waitFor();
			// if (finished && exitCode == 0) {
			if (exitCode != 0) {
				System.out.println(" Execution failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected class ProcessReader implements Runnable {

		protected String line = null;

		protected Process process;

		public ProcessReader(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			// Stream<String> lines = new BufferedReader(new
			// InputStreamReader(process.getInputStream())).lines();
			try {
				line = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			stdOut.add(line);
			// System.out.println("Out:" + line);
		}

	}
}
