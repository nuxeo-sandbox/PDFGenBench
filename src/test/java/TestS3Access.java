import org.junit.Test;
import org.nuxeo.bench.gen.out.S3Writer;

public class TestS3Access {

	//@Test
	public void testWrite() throws Exception {
		
		//S3Writer writer = new S3Writer("tiry-bench-storage",aws_access_key_id, aws_secret_access_key, aws_session_token);
		S3Writer writer = new S3Writer("tiry-bench-storage");	
		writer.write("Hello S3".getBytes(), "testMe");
	}
	
}
