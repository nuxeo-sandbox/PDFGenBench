package org.nuxeo.bench.gen.out;

import org.nuxeo.bench.gen.BlobWriter;

public class S3Writer implements BlobWriter {

	public static final String NAME= "s3";
	
	protected String url;
	
	public S3Writer (String url) {
		this.url = url;		
	}	
	
	@Override
	public void write(byte[] data, String digest) throws Exception {
		// NOP
		
	}
	
}
