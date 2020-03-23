package org.nuxeo.bench.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.nuxeo.bench.gen.EntryPoint;
import org.nuxeo.bench.gen.itext.ITextNXBankStatementGenerator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator2;
import org.nuxeo.bench.gen.smt.SmtMeta;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class FakeBlobGenerator {

	protected RandomDataGenerator rnd = null;
	protected ITextNXBankTemplateCreator templateGen = null;
	protected ITextNXBankStatementGenerator gen;
	
	public void init() throws Exception {
	
		rnd = new RandomDataGenerator(true);
		templateGen = new ITextNXBankTemplateCreator2();

		// init random data generator
		InputStream csv = EntryPoint.class.getResourceAsStream("/data.csv");
		rnd.init(csv);

		// Generate the template
		InputStream logo = EntryPoint.class.getResourceAsStream("/NxBank3.png");
		templateGen.init(logo);

		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
		templateGen.generate(templateOut);
		byte[] templateData = templateOut.toByteArray();

		// Init PDF generator
		gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), templateGen.getKeys());
		gen.computeDigest = false;
		//gen.setRndGenerator(rnd);
	}
		
	public String getRandomKey() {		
		String[] meta = rnd.generate();
		return meta[meta.length-1];
	}
	
	public String computeKey(Long userSeed, Long operationSeed, Integer month) {
		return rnd.seeds2Id(userSeed, operationSeed, month);		
	}
	
	public String[] getMetaDataForBlobKey(String key) {		
		return rnd.generate(key);
	}
	
	public SmtMeta getStream(String key, OutputStream out) throws Exception {
		String[] meta = getMetaDataForBlobKey(key);		
		return gen.generate(out, meta);
	}
	
}
