import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.junit.Test;
import org.nuxeo.bench.gen.Injector;
import org.nuxeo.bench.gen.itext.ITextNXBankStatementGenerator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class TestInjector {

	public static final int NB_DOC = 500000;
	
	@Test
	public void testInjector() throws Exception {
		
		// Data Generator
		RandomDataGenerator rnd = new RandomDataGenerator();
		URL csvurl = this.getClass().getResource("data.csv");
		File csv = new File(csvurl.toURI());
		rnd.init(csv);
				
		// Generate the template
		URL logourl = this.getClass().getResource("NxBank3.png");
		File logo = new File(logourl.toURI());		
		ITextNXBankTemplateCreator templateGen = new ITextNXBankTemplateCreator();
		templateGen.init(new FileInputStream(logo));
			
		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();				
		templateGen.generate(templateOut);		
		byte[] templateData = templateOut.toByteArray();
		
		// Init PDF generator
		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), ITextNXBankTemplateCreator.KEYS);
		gen.computeDigest=true;
		gen.setRndGenerator(rnd);		
		
		Injector injector = new Injector(gen, NB_DOC);		
		injector.run();
	
	}
}
