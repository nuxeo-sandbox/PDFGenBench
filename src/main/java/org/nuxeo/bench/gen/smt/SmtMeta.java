package org.nuxeo.bench.gen.smt;

public class SmtMeta {

	protected String digest;

	protected  String[] keys;

	public SmtMeta(String digest, String[] keys) {
		super();
		this.digest = digest;
		this.keys = keys;
	}

	public String getDigest() {
		return digest;
	}

	public String[] getKeys() {
		return keys;
	}

}
