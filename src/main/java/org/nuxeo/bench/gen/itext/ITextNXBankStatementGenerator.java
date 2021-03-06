package org.nuxeo.bench.gen.itext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.CountingOutputStream;
import org.nuxeo.bench.gen.PDFFileGenerator;
import org.nuxeo.bench.gen.smt.SmtMeta;
import org.nuxeo.bench.rnd.RandomDataGenerator;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;

public class ITextNXBankStatementGenerator implements PDFFileGenerator {

	protected byte[] template;

	protected Map<Integer, Integer> index = new HashMap<Integer, Integer>();

	public boolean computeDigest = false;

	protected RandomDataGenerator rndGen;

	public String getName() {
		return "Template based generation with Index pre-processing using iText";
	}

	public void setRndGenerator(RandomDataGenerator rndGen) {
		this.rndGen = rndGen;
	}

	public void init(InputStream pdf, String[] keys) throws Exception {

		template = new byte[pdf.available()];
		pdf.read(template);
		
		PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(template));
		PdfDocument doc = new PdfDocument(pdfReader);

		PdfPage page = doc.getFirstPage();
		PdfDictionary dict = page.getPdfObject();

		PdfObject object = dict.get(PdfName.Contents);

		if (object instanceof PdfStream) {
			PdfStream stream = (PdfStream) object;
			byte[] data = stream.getBytes();
			String txt = new String(data);
			for (int k = 0; k < keys.length; k++) {
				String key = keys[k];
				int idx = 0;
				do {
					idx = txt.indexOf(key, idx);
					if (idx > 0) {
						index.put(idx, k);
					}
					idx++;
				} while (idx > 0);
			}
		}
		doc.close();
	}

	
	public SmtMeta generate(OutputStream buffer) throws Exception {
		String[] tokens = rndGen.generate();
		return generate(buffer, tokens);
	}
	
	public SmtMeta generate(OutputStream buffer, String[] tokens) throws Exception {

		DigestOutputStream db = null;
		
		CountingOutputStream cout = null;
		
		PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(template));

		WriterProperties wp = new WriterProperties();
		wp.setPdfVersion(PdfVersion.PDF_1_4);
		wp.useSmartMode();

		PdfWriter writer;
		if (computeDigest) {			
			db = new DigestOutputStream(buffer, MessageDigest.getInstance("MD5"));
			cout = new CountingOutputStream(db);			
		} else {
			cout = new CountingOutputStream(buffer);
		}
		writer = new PdfWriter(cout, wp);

		PdfDocument doc = new HackedPDFDocument(pdfReader, writer);

		PdfPage page = doc.getFirstPage();
		PdfDictionary dict = page.getPdfObject();
		PdfObject object = dict.get(PdfName.Contents);

		if (object instanceof PdfStream) {
			PdfStream stream = (PdfStream) object;
			byte[] data = stream.getBytes();

			for (Integer idx : index.keySet()) {
				byte[] chunk = tokens[index.get(idx)].getBytes();
				System.arraycopy(chunk, 0, data, idx, chunk.length);
			}
			stream.setData(data);
		}
		doc.close();
		writer.flush();
		writer.close();
		
		String fileName = "stmt-"+tokens[5].trim() + ".pdf";
		long fileSize = cout.getByteCount();
		String md5 = "n/a";
		
		if (db != null) {
			byte[] digest = db.getMessageDigest().digest();
			md5 = toHexString(digest).toUpperCase();
		}
		return new SmtMeta(md5, fileName, fileSize, tokens);
	}

	public static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}
}
