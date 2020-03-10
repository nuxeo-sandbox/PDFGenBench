package org.nuxeo.bench.gen.itext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.nuxeo.bench.gen.PDFTemplateGenerator;

import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;

public class ITextNXBankTemplateCreator2 extends ITextNXBankTemplateCreator implements PDFTemplateGenerator {

	protected ArrayList<String> keys = new ArrayList<String>();
	
	@Override
	protected void initOperations() {

		for (String k : _keys) {
			keys.add(k);
		}
		
		Operation obb = new Operation();
		obb.label="Beginning Balance";
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 0);
		obb.date = cal.getTime();

		obb.strValue=mkTag("Balance",12);		
		keys.add(obb.strValue);
		operations.add(obb);

		for (int i = 1; i < 15; i++) {
			Operation op = new Operation();

			
			op.label = mkTag("OP-" + String.format("%02d", i), 20) ;
			keys.add(op.label);			
			
			cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, i);
			op.date = cal.getTime();

			op.strValue=mkTag("OPVal" + String.format("%02d", i) ,12);
			keys.add(op.strValue);			
			
			operations.add(op);
		}
		
		//System.out.println(keys.size());
		
	}

	@Override
	public String[] getKeys() {
		return keys.toArray(new String[keys.size()]);
	}

	@Override
	protected void printOperation(Table table) {

		for (Operation op : operations) {
			table.addCell(createCell(new SimpleDateFormat("MMM dd, YYYY").format(op.date)))
					.setTextAlignment(TextAlignment.LEFT);
			table.addCell(createCell(op.label).setTextAlignment(TextAlignment.CENTER));

			if (op.label.startsWith("#OP")) {
				table.addCell(createCell(op.strValue)).setTextAlignment(TextAlignment.RIGHT);
				table.addCell(createCell(""));				
			} else {
				table.addCell(createCell(""));
				table.addCell(createCell(op.strValue)).setTextAlignment(TextAlignment.RIGHT);
			} 
		}
		
	}

}
