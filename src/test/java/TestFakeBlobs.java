import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.junit.Test;
import org.nuxeo.bench.blob.FakeBlobGenerator;
import org.nuxeo.bench.gen.itext.ITextNXBankStatementGenerator;
import org.nuxeo.bench.gen.smt.SmtMeta;

public class TestFakeBlobs {

	
	@Test
	public void canGenerateFakeBlobs() throws Exception {
		
		FakeBlobGenerator bg = new FakeBlobGenerator();
		bg.init();
		
		String key = bg.getRandomKey();
		
		String[] meta = bg.getMetaDataForBlobKey(key);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();			
		SmtMeta smeta= bg.getStream(key, out);		

		
		byte[] pdfData = out.toByteArray();
		PDFTextStripper stripper = new PDFTextStripper();
		String txt = stripper.getText(PDDocument.load(new ByteArrayInputStream(pdfData)));

		assertTrue(txt.contains(meta[0]));
		assertTrue(txt.contains(smeta.getKeys()[0]));


		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(pdfData);
	    byte[] digest1 = md.digest();
	    
	    String hexd1= ITextNXBankStatementGenerator.toHexString(digest1);

	    
	    out = new ByteArrayOutputStream();			
		bg.getStream(key, out);		
		
		pdfData = out.toByteArray();		
		String txt2 = stripper.getText(PDDocument.load(new ByteArrayInputStream(pdfData)));

		assertEquals(txt, txt2);
		
		md.reset();
		md.update(pdfData);
	    byte[] digest2 = md.digest();

	    String hexd2= ITextNXBankStatementGenerator.toHexString(digest2);
	    	    
	    //assertEquals(hexd1, hexd2);
	    
	    
		
		
		
	}
	
}
