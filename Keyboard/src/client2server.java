import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Scanner;
//import sun.misc.BASE64Encoder; 

public class client2server {
	Robot robot = null;
	/**
	 * 启动前的延时
	 */
	int startDelay = 5000;
	/**
	 * 每次按键延时(ms)
	 */
	int Invtime = 10;
	client2server()
	{
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	void click(char key)
	{
		int keyid = -1;
		int shift = -1;
		switch(key)
		{
			case ' '  : keyid =  KeyEvent.VK_SPACE;break;
			case '\t' : keyid =  KeyEvent.VK_TAB;break;
			case '\n' : keyid =  KeyEvent.VK_ENTER;break;
			case '`'  :
			case '~'  : keyid = 0xC0;if(key=='~')  shift=1;break;
			case '\'' :
			case '\"' : keyid = 0xDE;if(key=='\"') shift=1;break;
			case '+'  : 
			case '='  : keyid = KeyEvent.VK_EQUALS;if(key=='+') shift=1;break;
			case ','  :
			case '<'  : keyid = KeyEvent.VK_COMMA   ;if(key=='<') shift=1; break;
			case '-'  : 
			case '_'  : keyid = KeyEvent.VK_MINUS; if(key=='_') shift=1;break;
			case '.'  : 
			case '>'  : keyid =  KeyEvent.VK_PERIOD ;if(key=='>') shift=1; break;
			case '/'  : 
			case '?'  : keyid = KeyEvent.VK_SLASH;if(key=='?') shift=1; break;
			case ';'  :
			case ':'  : keyid = KeyEvent.VK_SEMICOLON;if(key==':') shift=1; break; 
			case '['  :
			case '{'  : keyid = KeyEvent.VK_OPEN_BRACKET;if(key=='{') shift=1; break; 
			case '\\' :
			case '|'  : keyid = KeyEvent.VK_BACK_SLASH;if(key=='|') shift=1;break; 
			case ']'  : 
			case '}'  : keyid = KeyEvent.VK_CLOSE_BRACKET;if(key=='}') shift=1; break;//ok
			case '!'  : 
			case '1'  : keyid = KeyEvent.VK_1;if(key=='!') shift=1; break;
			case '@'  : 
			case '2'  : keyid = KeyEvent.VK_2;if(key=='@') shift=1; break;
			case '#'  : 
			case '3'  : keyid = KeyEvent.VK_3;if(key=='#') shift=1; break;
			case '$'  : 
			case '4'  : keyid = KeyEvent.VK_4;if(key=='$') shift=1; break;
			case '%'  : 
			case '5'  : keyid = KeyEvent.VK_5;if(key=='%') shift=1; break;
			case '^'  : 
			case '6'  : keyid = KeyEvent.VK_6;if(key=='^') shift=1; break;
			case '&'  : 
			case '7'  : keyid = KeyEvent.VK_7;if(key=='&') shift=1; break;
			case '*'  : 
			case '8'  : keyid = KeyEvent.VK_8;if(key=='*') shift=1; break;
			case '('  : 
			case '9'  : keyid = KeyEvent.VK_9;if(key=='(') shift=1; break;
			case ')'  : 
			case '0'  : keyid = KeyEvent.VK_0;if(key==')') shift=1; break;
		}
		if(key<='z'&&key>='a') 
		{
			keyid = KeyEvent.VK_A + key-'a';
		}
		if(key<='Z'&&key>='A')
		{
			keyid = KeyEvent.VK_A + key-'A';
			shift = 1;
		}
		if(keyid==-1) return;
		if(shift==1)  
		{
			robot.keyPress(KeyEvent.VK_SHIFT); 
			robot.delay(3);
		}
		Invtime = 5;
		robot.keyPress(keyid); 
        robot.keyRelease(keyid); 
        if(shift==1)  
        {
        	robot.delay(3);
        	robot.keyRelease(KeyEvent.VK_SHIFT); 
        }
        robot.delay(Invtime);
	}
	String file2base64(String fileName)
	{
		File file = new File(fileName);
		FileInputStream inputFile;
		String ret = "";
		try {
			inputFile = new FileInputStream(file);
			byte buffer[] = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			ret = ( new sun.misc.BASE64Encoder() ).encode( buffer ); 
			//System.out.println( "file: " + new String( buffer) );	
			//System.out.println( "base64: " + ret );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
		//return new BASE64Encoder().encode(buffer);
	}
	void clickString(String s){
		robot.delay( startDelay );
		int n = s.length();
		for (int i=0; i<n; i++ )
		{
			click( s.charAt(i) );
		}
	}
	public void clickfile(String fileName){
		String res = file2base64(fileName);
		clickString(res);
	}
	void command_io(){
		System.out.println("输入要编码文件的绝对路径");
		String fileName = "";
		Scanner cin = new Scanner( System.in );
		fileName = cin.nextLine();
		cin.close();
		String res = file2base64(fileName);
		System.out.println("编码成功，5s后开始自动输入，马上切换回虚拟机。");
		clickString(res);		
	}
	static public void main(String av[]) throws AWTException
	{
		client2server test = new client2server();
		test.command_io();
		//test.charset_test();
		//test.input_file_test();
		//test.encode_decode_test();
	}
	
	void charset_test(){
		String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
		System.out.println(charset);
		clickString(charset);
	}
	void input_file_test(){
		clickfile("read_from.txt");
	}
	void encode_decode_test(){
		String tmp = file2base64("read_from.txt");
		String s = "";
		try {
			s = new String( (new sun.misc.BASE64Decoder() ).decodeBuffer( tmp ) );
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(s);
	}
}
