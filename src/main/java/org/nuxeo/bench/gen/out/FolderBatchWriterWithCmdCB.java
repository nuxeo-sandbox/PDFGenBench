package org.nuxeo.bench.gen.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

public class FolderBatchWriterWithCmdCB extends AbstractFolderBatchWriter {

	protected ProcessBuilder pbuilder = new ProcessBuilder();

	protected final String[] cmd;

	protected List<String> stdOut;

	
	protected boolean cleanup = false;

	public FolderBatchWriterWithCmdCB(String folder, int batchSize, int total, String[] cmd, boolean cleanup) {
		super(folder, batchSize, total);
		this.cmd = cmd;
		stdOut = new ArrayList<String>();
		this.cleanup=cleanup;
	}

	public FolderBatchWriterWithCmdCB(String folder, int batchSize, int total, String shellCmd, boolean cleanup) {
		this(folder, batchSize, total, new String[] {"bash", "-c", shellCmd}, cleanup);		
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
		
		final String targetPath = path;
		try {
			Process process = pbuilder.start();										
			ProcessHandle ph = process.toHandle();

			ph.onExit().thenAccept(handle -> {
				if (process.exitValue()==0) {
					//System.out.println("Finished:" + handle.info().commandLine().get());
					//System.out.println("Finished:");
					try {
						String line = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
						stdOut.add(line);
						//System.out.println(" ==>" + line);
					} catch (IOException e) {
						e.printStackTrace();
					}					
					if (cleanup) {
						try {
							//System.out.println(handle.info().commandLine().get());
							//System.out.println("delete " + targetPath);
							FileUtils.deleteDirectory(new File(targetPath));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}					
				}
				else {
					System.out.println("Error while executing CB command: exit=" + process.exitValue());
					Stream<String> errs=new BufferedReader(new InputStreamReader(process.getErrorStream())).lines();
					errs.forEach(e -> System.out.println(e)); 
				}
				
				});
			ph.onExit().get();
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				System.out.println(" Execution failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
