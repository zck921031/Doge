import java.io.File;
import java.io.FileInputStream;


public class decoder {
	static public void main(String av[]){
		File file = new File("receive.base64");
		FileInputStream inputFile;
		String rev = "";
		try {
			inputFile = new FileInputStream(file);
			byte buffer[] = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			rev = new String( (new sun.misc.BASE64Decoder() ).decodeBuffer( new String(buffer) ) );
			//System.out.println( "file: " + new String( buffer) );	
			System.out.println( "file: " + rev );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
