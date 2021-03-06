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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RandomDataGenerator {

	protected ArrayList<String> firstNames = new ArrayList<String>();
	protected ArrayList<String> lastNames = new ArrayList<String>();
	protected ArrayList<String> streets = new ArrayList<String>();
	protected ArrayList<String> cities = new ArrayList<String>();
	protected ArrayList<String> states = new ArrayList<String>();
	protected ArrayList<String> companies = new ArrayList<String>();

	protected SimpleDateFormat df = new SimpleDateFormat("MMM dd YYYY");
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
				if (parts.length > 6 && parts[6] != null && !parts[6].isEmpty())
					companies.add(clean(parts[6]));
				
				line = reader.readLine();
			} while (line != null);
		}
	}

	public String[] generate() {
		return generate(null, null, null);
	}
	
	public String seeds2Id(Long seed1, Long seed2, Integer dm) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(String.format("%019d", seed1));
		if (seed2!=null) {
			sb.append(":");
			sb.append(String.format("%019d", seed2));
		}
		sb.append(":");
		sb.append(String.format("%04d", dm));
		return sb.toString();
	}
	
	protected Long[] Id2Seeds(String key) {
		String[] parts = key.split(":");		
		return new Long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2])};		
	}
	
	public String[] generate(String key) {		
		Long[] seeds = Id2Seeds(key);
		return generate(seeds[0], seeds[1], seeds[2].intValue());
	}
	
	public String[] generate(Long seed1, Long seed2, Integer dm) {
		String[] result=null;
		
		if (seed1==null) {
			seed1 = new Random().nextLong();
		}		
		Random rndSeq1 = new Random(seed1);
				
		if (dm==null) {
			dm = new Random().nextInt(48);
		}
		
		if (generateOperations) {
			result = new String [6+14*2 + 1+1+1+1];
		} else {
			result = new String [6+1];
		}
		
		fillUserInfo(result, rndSeq1);

		Date date = getDateWithOffset(dm);
		result[5] = pad(df.format(date), 20, false);

		if (generateOperations) {
			if (seed2==null) {
				seed2 = new Random().nextLong();
			}		
			Random rndSeq2 = new Random(seed2);
			fillOperations(result,rndSeq2);
		}

		// store seed key
		result[result.length-1]=seeds2Id(seed1, seed2, dm);
		
		return result;
	}

	
	public List<String[]> generateSerie(int length) {
		return generateSerie(length, null);
	}
	
	protected Date getDateWithOffset(int dm) {
		int dy = dm/12;
		int m = dm - dy*12;
		return new GregorianCalendar(2020-dy, m, 01).getTime();		
	}
	
	public List<String[]> generateSerie(int length, Long seed1) {
		
		List<String[]> serie = new ArrayList<String[]>();

		if (seed1==null) {
			seed1 = new Random().nextLong();
		}		
		Random rndSeq1 = new Random(seed1);

		String[] userInfo = new String [6];
		fillUserInfo(userInfo, rndSeq1);
		
		for (int dm = 0; dm < length; dm++) {
			
			Long seed2 = rndSeq1.nextLong();
			
			String[] data = new String [6+14*2 + 1+1+1+1];			
			System.arraycopy(userInfo, 0, data, 0, 6);
						
			Date date = getDateWithOffset(dm);
			data[5] = pad(df.format(date), 20, false);

			Random rndSeq2 = new Random(seed2);
			fillOperations(data, rndSeq2);
			
			data[data.length-1]=seeds2Id(seed1, seed2, dm);
			
			serie.add(data);
		}
		
		return serie;
	}
	
	protected void fillUserInfo(String[] result, Random rndSeq ) {

		
		result[0] = firstNames.get((int) Math.round(rndSeq.nextDouble() * (firstNames.size() - 1))) + " "
				+ lastNames.get((int) Math.round(rndSeq.nextDouble() * (lastNames.size() - 1)));
		result[1] = streets.get((int) Math.round(rndSeq.nextDouble() * (streets.size() - 1)));
		int idx = (int) Math.round(rndSeq.nextDouble() * (cities.size() - 1));
		result[2] = cities.get(idx);
		result[3] = states.get(idx);

		result[5] = df
				.format(Date.from(Instant.ofEpochMilli(System.currentTimeMillis() - Math.round(rndSeq.nextDouble() * DR))));

		result[4] = String.format("%016d", Math.round(rndSeq.nextDouble() * 10000000000000000L));

		result[0] = result[0] + " ".repeat(41 - result[0].length());

		for (int i = 1; i < 4; i++) {
			result[i] = pad(result[i], 20, true);
		}
		result[4] = pad(result[4], 20, false);
		result[5] = pad(result[5], 20, false);
	}

	
	protected double getRandomAmount(Random rndSeq) {
		return (rndSeq.nextDouble() * Math.pow(10, 5)) / 100;
	}
	
	protected String getFormatedRandomAmount(double amount, int size) {
		String formattedValue = NumberFormat.getCurrencyInstance(new Locale("en", "US")).format(amount);
		return pad(formattedValue, size, false);		
	}

	protected String pad(String v, int size, boolean left) {
		if (v.length()> size) {
			v = v.substring(0, size-1);
		}
		if (left) {
			return v + " ".repeat(size - v.length()) ;
		} else {
			return " ".repeat(size - v.length()) + v;	
		}
	}
	
	protected void fillOperations(String[] result, Random rndSeq) {
		int idx = 6;

		// init month from the same date as the statement!
		result[idx]=pad(result[5].trim().substring(0,3), 5, false);
		idx++;
				
		double total = getRandomAmount(rndSeq)*30;
		result[idx] = getFormatedRandomAmount(total, 12);
		idx++;

		String opName ="";
		for (int i = 1; i < 15; i++) {
			opName = companies.get((int) Math.round(rndSeq.nextDouble() * (companies.size() - 1))); 
			result[idx] = pad(opName, 30, true);
			idx++;
			double op = getRandomAmount(rndSeq);
			result[idx] = getFormatedRandomAmount(op, 12);
			total = total - op;
			idx++;
		}
		
		result[idx] = getFormatedRandomAmount(total, 12);
	}
}
