package org.nuxeo.bench.rnd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RandomDataGenerator {

	protected ArrayList<String> firstNames = new ArrayList<String>();
	protected ArrayList<String> lastNames = new ArrayList<String>();
	protected ArrayList<String> streets = new ArrayList<String>();
	protected ArrayList<String> cities = new ArrayList<String>();
	protected ArrayList<String> states = new ArrayList<String>();

	protected SimpleDateFormat df = new SimpleDateFormat("MMM DD YYYY");
	protected static final int DR = 5 * 365 * 24 * 3600 * 1000;

	protected final boolean generateOperations;

	public RandomDataGenerator(boolean generateOperations) {
		this.generateOperations = generateOperations;
	}

	protected String clean(String input) {
		input = input.trim();
		if (input.length() > 20) {
			input = input.substring(0, 20);
		}
		return input;
	}

	public void init(InputStream csv) throws Exception {
		init(new InputStreamReader(csv));
	}

	public void init(File csv) throws Exception {
		init(new FileReader(csv));
	}

	protected void init(Reader csvReader) throws Exception {

		try (BufferedReader reader = new BufferedReader(csvReader)) {

			String line = reader.readLine(); // skip first line
			line = reader.readLine();
			do {
				String parts[] = line.split(",");

				if (parts[0] != null)
					firstNames.add(clean(parts[0]));
				if (parts.length > 1 && parts[1] != null && !parts[1].isEmpty())
					lastNames.add(clean(parts[1]));
				if (parts.length > 2 && parts[2] != null && !parts[2].isEmpty())
					streets.add(clean(parts[2]));
				if (parts.length > 3 && parts[3] != null && !parts[3].isEmpty()) {
					cities.add(clean(parts[3]));
					if (parts.length > 5 && parts[5] != null && !parts[5].isEmpty()) {
						states.add(clean(parts[5]));
					} else {
						states.add("NY");
					}
				}
				line = reader.readLine();
			} while (line != null);
		}
	}

	public String[] generate() {
		String[] result=null;
		
		if (generateOperations) {
			result = new String [6+14*2 + 1];
		} else {
			result = new String [6];
		}
		
		fillUserInfo(result);

		if (generateOperations) {
			fillOperations(result);
		}

		return result;
	}

	protected void fillUserInfo(String[] result) {

		result[0] = firstNames.get((int) Math.round(Math.random() * (firstNames.size() - 1))) + " "
				+ lastNames.get((int) Math.round(Math.random() * (lastNames.size() - 1)));
		result[1] = streets.get((int) Math.round(Math.random() * (streets.size() - 1)));
		int idx = (int) Math.round(Math.random() * (cities.size() - 1));
		result[2] = cities.get(idx);
		result[3] = states.get(idx);

		result[4] = df
				.format(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - Math.round(Math.random() * DR))));
		result[5] = String.format("%016d", Math.round(Math.random() * 10000000000000000L));

		result[0] = result[0] + " ".repeat(41 - result[0].length());

		for (int i = 1; i < 4; i++) {
			result[i] = result[i] + " ".repeat(20 - result[i].length());
		}
		result[4] = " ".repeat(20 - result[4].length()) + result[4];
		result[5] = " ".repeat(20 - result[5].length()) + result[5];

	}

	protected String getFormatedRandomAmount(int size) {
		double amount = (Math.random() * Math.pow(10, 8)) / 100;
		String formattedValue = NumberFormat.getCurrencyInstance(new Locale("en", "US")).format(amount);
		return pad(formattedValue, size, false);		
	}

	protected String pad(String v, int size, boolean left) {
		if (left) {
			return v + " ".repeat(size - v.length()) ;
		} else {
			return " ".repeat(size - v.length()) + v;	
		}
	}
	
	protected void fillOperations(String[] result) {
		int idx = 6;
		result[idx] = getFormatedRandomAmount(12);
		idx++;

		for (int i = 1; i < 15; i++) {
			result[idx] = pad("Operation whatever", 20, true);
			idx++;
			result[idx] = getFormatedRandomAmount(12);
			idx++;
		}
	}
}
