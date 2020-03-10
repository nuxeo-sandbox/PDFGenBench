package org.nuxeo.bench.gen;

public interface BlobWriter {

	void write(byte[] data, String digest) throws Exception;
}
