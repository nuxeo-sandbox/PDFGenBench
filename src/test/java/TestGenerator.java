import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.Test;
import org.nuxeo.bench.gen.itext.ITextNXBankStatementGenerator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator;
import org.nuxeo.bench.gen.itext.ITextNXBankTemplateCreator2;
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class TestGenerator {
	
	protected byte[] getTemplate(ITextNXBankTemplateCreator templateGen) throws Exception {
		// Generate the template
		URL logourl = this.getClass().getResource("NxBank3.png");
		File logo = new File(logourl.toURI());
		
		templateGen.init(new FileInputStream(logo));

		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
		templateGen.generate(templateOut);
		return templateOut.toByteArray();
	}

	protected RandomDataGenerator getRndGenerator(boolean generateOperations) throws Exception {
		// Data Generator
		RandomDataGenerator rnd = new RandomDataGenerator(generateOperations);
		URL csvurl = this.getClass().getResource("data.csv");
		File csv = new File(csvurl.toURI());
		rnd.init(csv);
		return rnd;
	}
	

	@Test
	public void canGenerateRandomData() throws Exception {

		RandomDataGenerator rnd = getRndGenerator(false);

		String[] data = rnd.generate();
		String[] keys = new ITextNXBankTemplateCreator().getKeys();

		assertEquals(keys.length, data.length);
		for (var i = 0; i < keys.length; i++) {
			assertFalse(data[i].isEmpty());
		}
		
		rnd = getRndGenerator(true);
		String[] data2 = rnd.generate();
		assertTrue(data2.length> data.length);
		for (var i = 0; i < data2.length; i++) {
			assertFalse(data2[i].isEmpty());
			//System.out.println(data2[i]);
		}
	}

	@Test
	public void canGenerateTemplate() throws Exception {

		ITextNXBankTemplateCreator templateGen = new ITextNXBankTemplateCreator();
				
		assertEquals("#NAME-----------------------------------#", ITextNXBankTemplateCreator.mkTag("NAME", 41));
		assertEquals("#STREET------------#", ITextNXBankTemplateCreator.mkTag("STREET", 20));
		
		byte[] templateData = getTemplate(templateGen);
		String[] keys = templateGen.getKeys();
		
		assertTrue(templateData.length > 0);

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(templateData)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < keys.length; i++) {
			assertTrue(txt.contains(keys[i]));
		}
	}

	@Test
	public void canGenerateTemplate2() throws Exception {
		
		ITextNXBankTemplateCreator2 templateGen = new ITextNXBankTemplateCreator2();
		byte[] templateData = getTemplate(templateGen);
		String[] keys = templateGen.getKeys();
		
		assertTrue(templateData.length > 0);

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(templateData)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < keys.length; i++) {
			assertTrue(txt.contains(keys[i]));
		}
	}

	@Test
	public void canGenerateFromTemplate() throws Exception {

		ITextNXBankTemplateCreator templateGen = new ITextNXBankTemplateCreator();
		byte[] templateData = getTemplate(templateGen);
		String[] keys = templateGen.getKeys();
		
		RandomDataGenerator rnd = getRndGenerator(false);

		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), keys);
		gen.computeDigest = true;
		gen.setRndGenerator(rnd);

		ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
		gen.generate(pdfOut);

		byte[] pdf = pdfOut.toByteArray();

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(pdf)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < keys.length; i++) {
			assertFalse(txt.contains(keys[i]));
		}

	}

	@Test
	public void canGenerateFromTemplate2() throws Exception {

		ITextNXBankTemplateCreator2 templateGen = new ITextNXBankTemplateCreator2();
		byte[] templateData = getTemplate(templateGen);
		String[] keys = templateGen.getKeys();
		
		RandomDataGenerator rnd = getRndGenerator(true);
		
		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), keys);
		gen.computeDigest = true;
		gen.setRndGenerator(rnd);

		ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
		gen.generate(pdfOut);

		byte[] pdf = pdfOut.toByteArray();

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(pdf)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < keys.length; i++) {
			assertFalse(txt.contains(keys[i]));
		}

	}

}
