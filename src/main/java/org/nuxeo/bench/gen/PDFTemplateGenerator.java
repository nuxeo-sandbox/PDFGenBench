package org.nuxeo.bench.gen;

import java.io.InputStream;
import java.io.OutputStream;

public interface PDFTemplateGenerator {
	
	void init(InputStream input) throws Exception;
	
	void generate(OutputStream pdf) throws Exception;
	
}
