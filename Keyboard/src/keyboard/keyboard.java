package keyboard;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.*;

 
public class keyboard
{
	static Robot robot = null;
	static int Invtime = 10;
	public static void clk(char key)
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
    	robot.keyPress(keyid); 
        robot.keyRelease(keyid); 
        if(shift==1)  
        {
        	robot.delay(3);
        	robot.keyRelease(KeyEvent.VK_SHIFT); 
        }
        robot.delay(Invtime);
	}
    public static void keyboardline(String st)
    {
    	for(int i=0;i<st.length();i++)
    	{
    		clk(st.charAt(i));
    	}
    	clk('\n');
    }
    static void ReadAndWrite()
    {
    	try
    	{
    		//FileReader fle = new FileReader("read_from.txt");
    		BufferedReader fle = new BufferedReader(new InputStreamReader(new FileInputStream("read_from.txt")));
    		String line = null;
    		while(true)
    		{
    			try
    			{
    				line = fle.readLine();
    				if(line==null) break;
    				System.out.println("one line ^_^ ");
    				keyboardline(line);
    			}
    			catch(IOException e)
    			{
    				return;
    			}
    		}
    		try{
    			fle.close();
    		}
    		catch(IOException e)
			{
				
				return;
			}
    	}
    	catch(FileNotFoundException e)
    	{
    	}
    }
    public static void main(String[] args) 
    {
    	if(args.length==1) Invtime = Integer.parseInt(args[0]);
    	try{
    		robot = new Robot();
    	}
    	catch(AWTException e)
    	{
    	}
    	robot.delay(5000);
    	//keyboardline("1234567890-= !@#$%^&*()_+ ~` qwertyuiop[]\\QWERTYUIOP{}| asdfghjkl;'ASDFGHJKL:\" zxcvbnm,./ZXCVBNM<>?");
    	ReadAndWrite();
    	System.out.println("ok! finish! ^_^ ");
    } 
}