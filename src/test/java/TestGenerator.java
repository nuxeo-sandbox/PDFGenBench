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
import org.nuxeo.bench.rnd.RandomDataGenerator;

public class TestGenerator {

	protected byte[] getTemplate() throws Exception {
		// Generate the template
		URL logourl = this.getClass().getResource("NxBank3.png");
		File logo = new File(logourl.toURI());
		ITextNXBankTemplateCreator templateGen = new ITextNXBankTemplateCreator();
		templateGen.init(new FileInputStream(logo));

		ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
		templateGen.generate(templateOut);
		return templateOut.toByteArray();
	}

	protected RandomDataGenerator getRndGenerator() throws Exception {
		// Data Generator
		RandomDataGenerator rnd = new RandomDataGenerator();
		URL csvurl = this.getClass().getResource("data.csv");
		File csv = new File(csvurl.toURI());
		rnd.init(csv);
		return rnd;
	}

	@Test
	public void canGenerateRandomData() throws Exception {

		RandomDataGenerator rnd = getRndGenerator();

		String[] data = rnd.generate();

		assertEquals(ITextNXBankTemplateCreator.KEYS.length, data.length);
		for (var i = 0; i < ITextNXBankTemplateCreator.KEYS.length; i++) {
			assertFalse(data[i].isEmpty());
		}
	}

	@Test
	public void canGenerateTemplate() throws Exception {

		byte[] templateData = getTemplate();

		assertTrue(templateData.length > 0);

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(templateData)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < ITextNXBankTemplateCreator.KEYS.length; i++) {
			assertTrue(txt.contains(ITextNXBankTemplateCreator.KEYS[i]));
		}
	}

	@Test
	public void canGenerateFromTemplate() throws Exception {

		byte[] templateData = getTemplate();

		RandomDataGenerator rnd = getRndGenerator();

		ITextNXBankStatementGenerator gen = new ITextNXBankStatementGenerator();
		gen.init(new ByteArrayInputStream(templateData), ITextNXBankTemplateCreator.KEYS);
		gen.computeDigest = true;
		gen.setRndGenerator(rnd);

		ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
		gen.generate(pdfOut);

		byte[] pdf = pdfOut.toByteArray();

		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(pdf)));

		assertTrue(txt.contains(ITextNXBankTemplateCreator.ACCOUNT_LABEL));
		for (var i = 0; i < ITextNXBankTemplateCreator.KEYS.length; i++) {
			assertFalse(txt.contains(ITextNXBankTemplateCreator.KEYS[i]));
		}

	}

}
