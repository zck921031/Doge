package changeDataToBMP;
import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

public class BMP_UI extends JFrame  {
		
	private static JComboBox jcb1 = null;
	private static JComboBox jcb2 = null;
	
	private static int partID = 0;	
		
	private static BMP_UI self = null;
	
	private static BufferedReader br = null;
	private static BufferedWriter bw = null;
	
	private static int[] data = null;
		
	private static JPanel worldPanel = null;
	private static JPanel infoPanel = null; 
	
	public static int toid(String s){
		if("#".equals (s)) return 0; /// because node 0 is null
		///System.out.println("toid: "+s);
		return Integer.parseInt( s.substring(2) );
	}
	public static int toInt(String s){
		return Integer.parseInt( s );
	}
	private static void getDataFromTxt(String txt)
	{
		File file = new File(txt);
        BufferedReader reader = null;
        if(data==null) data = new int[1682*155];
        int pos = 0;
        try {
            System.out.println("read file started !!");
            reader = new BufferedReader(new FileReader(file));
            String context = null;
            int line = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((context = reader.readLine()) != null) {
                // 显示行号
                ///System.out.println("line " + line + ": " + context);
            	if("".equals(context)) continue;
                line++;
                
                String [] strs = context.trim().split(",");
                ///System.out.println((strs.length-2)+" | "+roadNum+" | id ");
                int atID   = toid(strs[0]);
                int fromID = toid(strs[1]);
                data[pos] = atID;pos++;
                data[pos] = fromID;pos++;
                
                ///System.out.println((strs.length-2)+" | "+roadNum+" | id "+id);
                for(int i=2;i<strs.length;i++)
                {
                	data[pos] = toInt(strs[i]);   
                	pos++;
                }
                
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	}
	
	BMP_UI()
	{
		init();
        setVisible(true);
	}
	
	private void init(){
		setTitle( "Mechanic II DataToBMP");
		setBounds(0,0,1550,850);
		super.setBackground(Color.WHITE);
		
		worldPanel = new WorldPanel();
		infoPanel = new JPanel();
		
		String[] chooseSet = {"./data/flow0901.txt","./data/flow0902.txt","./data/flow0903.txt","./data/flow0904.txt","./data/flow0905.txt",
				"./data/flow0906.txt","./data/flow0907.txt","./data/meanflow.txt","./data/flow0908_guess.txt","./data/Allmeanflow.txt"};
		jcb1 = new JComboBox(chooseSet);
		jcb1.setBounds(0,0,200,20);
		infoPanel.add(jcb1);
		
		JButton jb6 = new JButton("Build1");
		jb6.setBounds(0, 0, 100, 20);
		jb6.addActionListener( new RestartAction() );
		infoPanel.add(jb6);
		JButton jb7 = new JButton("Build2");
		jb7.setBounds(0, 0, 100, 20);
		jb7.addActionListener( new RestartAction() );
		infoPanel.add(jb7);

		
		this.setLayout(new BorderLayout());
		this.add(worldPanel, BorderLayout.CENTER);
		this.add(infoPanel, BorderLayout.SOUTH);
		
	}
        
	static private final class WorldPanel extends JPanel{
		/**
		 *	在此画图
		 */
		private static final long serialVersionUID = 5245994220665688411L;
		
		public void paint( Graphics   origin_g )
	    {
			super.paint(origin_g);
			
	    	Graphics2D g= (Graphics2D)origin_g;
	    	Dimension size = getSize();
	    	g.clearRect(0, 0, (int)size.getWidth(), (int)size.getHeight() ); 
    		g.setColor( Color.black );
    		
    		g.drawRect(5, 5, 10+1500, 10+750);
    		if(data!=null)
    		{
    			int len = data.length;
    			System.out.println(len+" "+(1500*750/8)+" "+partID);
    			int startID = (partID==1?0:(1500*750/8));
    			int endID = (partID==1?(1500*750/8):len);
    			int startX = 10;
        		int startY = 10;
        		int pos = 0;
        		int runID = startID;
        		while(runID<endID)
        		{
        				//if((i+j)%2==0) g.drawRect(startX+j, startY+i, 0, 0);;
        				int diffID = (runID-startID);
        				int r = (diffID*8+pos)/1500;
        				int c = (diffID*8+pos)%1500;
        				int b = (data[runID]&(1<<pos))>0?1:0;
        				if(b>0) g.drawRect(startX+c, startY+r, 0, 0);
        				pos++;
        				if(pos>=8) 
        				{
        					pos=0;
        					runID++;
        				}
        		}
    		}
    		/*
    		
    		*/
    			    	
	    }
	}
	
	private static final class RestartAction extends AbstractAction {
		private static final long serialVersionUID = -8603262894746675499L;
        public void actionPerformed(ActionEvent e) {
        	System.out.println("Button down: Build");
        	 int cutid=1;
             System.out.println(e.getSource().toString());
             String fromsource = e.getSource().toString();
             int textpos = fromsource.indexOf("text=Build");
             if(textpos!=-1)
             {
            	cutid = Integer.parseInt( fromsource.substring(textpos+10,textpos+11) );
             	System.out.println(cutid);
             }
             partID = cutid;
            String JudgeTxt = "./data/flow0901.txt";
            JudgeTxt = jcb1.getSelectedItem().toString();
        	self.repaint();
        	System.out.println(JudgeTxt);
        	getDataFromTxt(JudgeTxt);
        	worldPanel.repaint();
        	//restart all!
        }
	}
	
	
        	
	public static void main(String av[]) throws IOException{
		self = new BMP_UI();
	}
}

