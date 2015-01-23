import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
public class decoder {
	static public void main(String av[]){
		String fileName = "receive.base64";
		String outfile = "receive.rename";
		if ( av.length > 0 ) fileName = av[0];
		if ( av.length > 1 ) outfile = av[1];
		File file = new File(fileName);
		FileInputStream inputFile;
		try {
			inputFile = new FileInputStream(file);
			byte buffer[] = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			byte []src = (new sun.misc.BASE64Decoder() ).decodeBuffer( new String(buffer) );
			//System.out.println( src.length );
			//System.out.println( "file: " + new String( buffer) );	
			FileOutputStream writer = new FileOutputStream( outfile );			
			writer.write(src);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
