package org.nuxeo.bench.blob;

public class KeyCodec {

	protected static final String CHARS="0123456789"
			+ "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	static final int B = CHARS.length();

	public static Number decode(String s) {
		if (s.length()==LSIZE) {
			return decodeLong(s);
		} else {
			return decodeInt(s);
		}
	}

	protected static long decodeLong(String s) {				
		s = clean(s);
		boolean invert = false;
		if (s.startsWith("*")) {
			s=s.substring(1);
			invert=true;
		}
		
		long num = 0;
	    for (char ch : s.toCharArray()) {
	        num *= B;
	        num += CHARS.indexOf(ch);
	    }
	    if (invert) {
	    	num = -num -1;
	    }
	    return num;
	}

	protected static int decodeInt(String s) {				
		s = clean(s);
		boolean invert = false;
		if (s.startsWith("*")) {
			s=s.substring(1);
			invert=true;
		}
		
		int num = 0;
	    for (char ch : s.toCharArray()) {
	        num *= B;
	        num += CHARS.indexOf(ch);
	    }
	    if (invert) {
	    	num = -num -1;
	    }
	    return num;
	}
	
	protected static final int LSIZE = 12;
	protected static final int SIZE = 7;
	
	protected static String pad(String s, int size) {
		return "#".repeat(size-s.length())+ s;
	}
	
	protected static String clean(String s) {		
		int idx = s.lastIndexOf("#");
		if (idx >=0) {
			return s.substring(idx+1);
		}
		return s;
	}

	public static String encode(long num) {
		return encode(num, LSIZE);
	}

	public static String encode(int num) {
		return encode(num, SIZE);
	}

	protected static String encode(long num, int padSize) {
		String prefix =null;
		if (num<0) {
			prefix = "*";
			num = -(num+1);
		}
	    StringBuilder sb = new StringBuilder();
	    while (num != 0) {
	        sb.append(CHARS.charAt((int) (num % B)));
	        num /= B;
	    }
	    if (prefix!=null) {
	    	sb.append(prefix);
	    }
	    String encoded= sb.reverse().toString();
	    return pad(encoded, padSize);
	}
	
	public static String encodeSeeds(long s1, long s2, int m) {
		StringBuilder sb = new StringBuilder();		
		sb.append(encode(s1));
		sb.append(":");
		sb.append(encode(s2));
		sb.append(":");
		sb.append(encode(m));		
		return sb.toString();
	}

	public static long[] decodeSeeds(String key) {		
		String[] parts = key.split(":");		
		return new long[] {decodeLong(parts[0]), decodeLong(parts[1]), decodeInt(parts[2])};
	}

}
