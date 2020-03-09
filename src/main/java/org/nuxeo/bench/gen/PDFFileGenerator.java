package org.nuxeo.bench.gen;

import java.io.InputStream;
import java.io.OutputStream;

import org.nuxeo.bench.gen.smt.SmtMeta;

public interface PDFFileGenerator {

	void init(InputStream pdfTemplate, String[] keys) throws Exception;

	SmtMeta generate(OutputStream pdf) throws Exception;

}
