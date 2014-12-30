import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.Line2D;


public class gui extends JFrame  {
	private static String traffic_topo_str = "tl44,tl42,tl43,#,tl19;tl44,tl43,tl19,tl42,#;"
			+ "tl44,tl19,#,tl43,tl42;tl43,tl44,tl41,tl18,#;tl43,tl41,#,tl44,tl18;tl43,tl18,tl44,#,tl41;"
			+ "tl42,tl26,tl41,#,tl44;tl42,tl41,tl44,tl26,#;tl42,tl44,#,tl41,tl26;tl41,tl42,tl25,tl43,tl40;"
			+ "tl41,tl25,tl40,tl42,tl43;tl41,tl40,tl43,tl25,tl42;tl41,tl43,tl42,tl40,tl25;tl40,tl41,tl24,tl17,tl39;"
			+ "tl40,tl24,tl39,tl41,tl17;tl40,tl39,tl17,tl24,tl41;tl40,tl17,tl41,tl39,tl24;tl39,tl40,tl23,tl16,#;"
			+ "tl39,tl23,#,tl40,tl16;tl39,tl16,tl40,#,tl23;tl38,tl12,tl37,#,tl5;tl38,tl37,tl5,tl12,#;"
			+ "tl38,tl5,#,tl37,tl12;tl37,tl38,tl11,tl4,tl36;tl37,tl11,tl36,tl38,tl4;tl37,tl36,tl4,tl11,tl38;"
			+ "tl37,tl4,tl38,tl36,tl11;tl36,tl37,tl10,tl3,#;tl36,tl10,#,tl37,tl3;tl36,tl3,tl37,#,tl10;"
			+ "tl35,tl52,tl58,tl28,tl34;tl35,tl58,tl34,tl52,tl28;tl35,tl34,tl28,tl58,tl52;tl35,tl28,tl52,tl34,tl58;"
			+ "tl34,tl35,tl57,tl27,tl33;tl34,tl57,tl33,tl35,tl27;tl34,tl33,tl27,tl57,tl35;tl34,tl27,tl35,tl33,tl57;"
			+ "tl33,tl34,tl56,tl26,tl32;tl33,tl56,tl32,tl34,tl26;tl33,tl32,tl26,tl56,tl34;tl33,tl26,tl34,tl32,tl56;"
			+ "tl32,tl33,tl55,tl25,tl31;tl32,tl55,tl31,tl33,tl25;tl32,tl31,tl25,tl55,tl33;tl32,tl25,tl33,tl31,tl55;"
			+ "tl31,tl32,#,tl24,tl30;tl31,tl30,tl24,#,tl32;tl31,tl24,tl32,tl30,#;tl30,tl31,tl54,tl23,tl29;tl30,tl54,tl29,tl31,tl23;"
			+ "tl30,tl29,tl23,tl54,tl31;tl30,tl23,tl31,tl29,tl54;tl29,tl30,tl53,tl22,tl51;tl29,tl53,tl51,tl30,tl22;tl29,tl51,tl22,tl53,tl30;"
			+ "tl29,tl22,tl30,tl51,tl53;tl28,tl35,tl27,#,tl21;tl28,tl27,tl21,tl35,#;tl28,tl21,#,tl27,tl35;tl27,tl28,tl34,tl20,tl26;"
			+ "tl27,tl34,tl26,tl28,tl20;tl27,tl26,tl20,tl34,tl28;tl27,tl20,tl28,tl26,tl34;tl26,tl27,tl33,tl42,tl25;tl26,tl33,tl25,tl27,tl42;"
			+ "tl26,tl25,tl42,tl33,tl27;tl26,tl42,tl27,tl25,tl33;tl25,tl26,tl32,tl41,tl24;tl25,tl32,tl24,tl26,tl41;tl25,tl24,tl41,tl32,tl26;"
			+ "tl25,tl41,tl26,tl24,tl32;tl24,tl25,tl31,tl40,tl23;tl24,tl31,tl23,tl25,tl40;tl24,tl23,tl40,tl31,tl25;tl24,tl40,tl25,tl23,tl31;"
			+ "tl23,tl24,tl30,tl39,tl22;tl23,tl30,tl22,tl24,tl39;tl23,tl22,tl39,tl30,tl24;tl23,tl39,tl24,tl22,tl30;tl22,tl23,tl29,tl14,#;tl22,tl29,#,tl23,tl14;"
			+ "tl22,tl14,tl23,#,tl29;tl21,tl28,tl20,#,tl6;tl21,tl20,tl6,tl28,#;tl21,tl6,#,tl20,tl28;tl20,tl21,tl27,#,tl19;tl20,tl27,tl19,tl21,#;tl20,tl19,#,tl27,tl21;"
			+ "tl19,tl20,tl44,tl12,tl18;tl19,tl44,tl18,tl20,tl12;tl19,tl18,tl12,tl44,tl20;tl19,tl12,tl20,tl18,tl44;tl18,tl19,tl43,tl11,tl17;tl18,tl43,tl17,tl19,tl11;"
			+ "tl18,tl17,tl11,tl43,tl19;tl18,tl11,tl19,tl17,tl43;tl17,tl18,tl40,tl10,tl16;tl17,tl40,tl16,tl18,tl10;tl17,tl16,tl10,tl40,tl18;tl17,tl10,tl18,tl16,tl40;tl16,tl17,tl39,tl9,tl15;"
			+ "tl16,tl39,tl15,tl17,tl9;tl16,tl15,tl9,tl39,tl17;tl16,tl9,tl17,tl15,tl39;tl15,tl16,#,tl8,tl14;tl15,tl14,tl8,#,tl16;tl15,tl8,tl16,tl14,#;tl14,tl15,tl22,tl7,#;tl14,tl22,#,tl15,tl7;"
			+ "tl14,tl7,tl15,#,tl22;tl12,tl19,tl11,#,tl38;tl12,tl11,tl38,tl19,#;tl12,tl38,#,tl11,tl19;tl11,tl12,tl18,tl37,tl10;tl11,tl18,tl10,tl12,tl37;tl11,tl10,tl37,tl18,tl12;tl11,tl37,tl12,tl10,tl18;"
			+ "tl10,tl11,tl17,tl36,tl9;tl10,tl17,tl9,tl11,tl36;tl10,tl9,tl36,tl17,tl11;tl10,tl36,tl11,tl9,tl17;tl9,tl10,tl16,tl2,tl8;tl9,tl16,tl8,tl10,tl2;tl9,tl8,tl2,tl16,tl10;tl9,tl2,tl10,tl8,tl16;"
			+ "tl8,tl9,tl15,#,tl7;tl8,tl15,tl7,tl9,#;tl8,tl7,#,tl15,tl9;tl7,tl8,tl14,tl1,tl13;tl7,tl14,tl13,tl8,tl1;tl7,tl13,tl1,tl14,tl8;tl7,tl1,tl8,tl13,tl14;tl1,tl2,tl7,#,tl45;tl1,tl7,tl45,tl2,#;"
			+ "tl1,tl45,#,tl7,tl2;tl2,tl47,tl3,tl1,tl9;tl2,tl3,tl9,tl47,tl1;tl2,tl9,tl1,tl3,tl47;tl2,tl1,tl47,tl9,tl3;tl3,tl4,tl36,#,tl2;tl3,tl36,tl2,tl4,#;tl3,tl2,#,tl36,tl4;tl4,tl48,tl5,tl3,tl37;tl4,tl5,tl37,tl48,tl3;tl4,tl37,tl3,tl5,tl48;"
			+ "tl4,tl3,tl48,tl37,tl5;tl5,tl49,tl6,tl4,tl38;tl5,tl6,tl38,tl49,tl4;tl5,tl38,tl4,tl6,tl49;tl5,tl4,tl49,tl38,tl6;tl6,tl50,tl46,tl5,tl21;tl6,tl46,tl21,tl50,tl5;tl6,tl21,tl5,tl46,tl50;tl6,tl5,tl50,tl21,tl46;";
	private static String row_id = 
			"47,48,49,50;"
			+ "45,1,2,3,4,5,6,46;"
			+ "36,37,38;"
			+ "13,7,8,9,10,11,12;"
			+ "14,15,16,17,18,19,20,21;"
			+ "43,44;"
			+ "39,40,41,42;"
			+ "22,23,24,25,26,27,28;"
			+ "51,29,30,31,32,33,34,35,52;"
			+ "53,54,55,56,57,58;";
	private static String col_id = "45,13,51;"
			+ "1,7,14,22,29,53;"
			+ "8,15;"
			+ "47,2,9,16,39,23,30,54;"
			+ "3,36,10,17,40,24,31;"
			+ "48,4,37,11,18,43,41,25,32,55;"
			+ "49,5,38,12,19,44,42,26,33,56;"
			+ "20,27,34,57;"
			+ "50,6,21,28,35,58;"
			+ "46,52;";
	
	static String JudgeTxt = "./data/flow0901.txt";
	static String Running_AI = "java -classpath ./bin  Mechanic";
	//static String Running_AI = "java -classpath .\\bin  BaseStatus";
	
	//static String Running_AI = "java -classpath ./bin/ its.Main";
	
	///KOCO Color
	private static Color KOCO_Green = new Color(0,200,0);
	private static Color KOCO_LightBlue = new Color(20,190,220);
	private static Color KOCO_Yellow = new Color(213,202,0);
	private static Color KOCO_Orange = new Color(255,127,39);
	
	///KOCO flowdata:
	private static FlowData flowdata = null;
	private static gui self = null;
	
	private static BufferedReader br = null;
	private static BufferedWriter bw = null;
	private static Runtime run = null;
	private static Process proc = null;
	
	public static int dx[]={0, 1,  0, -1};
	public static int dy[]={1, 0, -1,  0};
	public static HashMap<Integer, Integer> px = new HashMap<Integer, Integer>();
	public static HashMap<Integer, Integer> py = new HashMap<Integer, Integer>();
	public static int MAXN = 59;
	public static boolean graph[][] = new boolean[MAXN][MAXN];
	
	private JPanel worldPanel = null;
	private JPanel infoPanel = null; 
	
	
	
	
		
	private static int toid(String s){
		return Integer.parseInt( s.substring(2) );
	}
	public static void generate_world(){
		String str[] = row_id.split(";");
		for ( int i=0; i<str.length; i++ ){
			String elem[] = str[i].split(",");
			for ( int j=0; j<elem.length; j++ ){
				int id =  Integer.parseInt( elem[j] );
				px.put( id, i);			
			}
		}		
		str = col_id.split(";");
		for ( int i=0; i<str.length; i++ ){
			String elem[] = str[i].split(",");
			for ( int j=0; j<elem.length; j++ ){
				int id =  Integer.parseInt( elem[j] );
				py.put( id, i);			
			}
		}
		
		for (int i=0; i<MAXN; i++)
			for (int j=0; j<MAXN; j++){
				graph[i][j] = false;
			}
		String[] lights = traffic_topo_str.trim().split(";");
		for(String light:lights){
			String [] strs = light.trim().split(",");

			int u = toid( strs[0] );
			for (int i=0; i<5; i++){
				if ( 0==i ) continue;
				if ( strs[i].equals("#") ) continue;
				int v = toid(strs[i]);
				graph[u][v] = true;
				graph[v][u] = true;
			}
		}
		
	}
	
	gui()
	{
		process_init();
		init();
		attachListeners();		
        setVisible(true);
        ///KOCO flowdata:
        flowdata = new FlowData();
        flowdata.initJudgeFromTxt( JudgeTxt);
        //flowdata.initJudgeFromFreedomData(6);
        
	}
	private static void process_init(){
		run = Runtime.getRuntime();		
		try {
			if ( proc!=null ) proc.destroy();
			//proc = run.exec("java -classpath .\\bin  Demo_v1");
			//proc = run.exec("java -classpath .\\bin  Demo_v2");
			//proc = run.exec("java -classpath .\\bin  BaseStatus");
			//proc = run.exec("java -classpath .\\bin  Mechanic");
			proc = run.exec(Running_AI);
			

    		BufferedInputStream in = new BufferedInputStream(proc.getInputStream() );
    		br = new BufferedReader(new InputStreamReader(in) );
    		bw = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()) );  
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	private void init(){
		setTitle( "Mechanic 2.0");
		setBounds(0,0,1100,900);
		super.setBackground(Color.WHITE);
		
		worldPanel = new WorldPanel();

		infoPanel = new JPanel();
		JButton jb1 = new JButton("1step");
		jb1.setBounds(0, 0, 100, 30);
		jb1.addActionListener( new NextAction() );
		infoPanel.add(jb1);
		
		JButton jb2 = new JButton("20step");
		jb2.setBounds(0, 0, 100, 30);
		jb2.addActionListener( new NextAction() );
		infoPanel.add(jb2);
		
		JButton jb3 = new JButton("80step");
		jb3.setBounds(0, 0, 100, 30);
		jb3.addActionListener( new NextAction() );
		infoPanel.add(jb3);
		
		JButton jb4 = new JButton("120step");
		jb4.setBounds(0, 0, 100, 30);
		jb4.addActionListener( new NextAction() );
		infoPanel.add(jb4);
		
		JButton jb5 = new JButton("1680step");
		jb5.setBounds(0, 0, 100, 30);
		jb5.addActionListener( new NextAction() );
		infoPanel.add(jb5);
		
		this.setLayout(new BorderLayout());
		this.add(worldPanel, BorderLayout.CENTER);
		this.add(infoPanel, BorderLayout.SOUTH);
		
	}
    private void attachListeners() {
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.addWindowListener(new WindowAdapter(){
    		public void windowClosing(WindowEvent e){
    			if ( proc!=null ) proc.destroy();
    			System.exit(0);
    		}
    	});    	
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
	    	double cx = (size.getHeight()-60)/8.8;
	    	double cy = (size.getWidth() -40)/9.0;
	    	g.setColor( Color.BLACK );
			for (int i=0; i<MAXN; i++){
				for (int j=0; j<MAXN; j++){
					if ( graph[i][j] ){
			    		int uy = (int) ( cx * ( px.get(i) ) + 20 );
			    		int ux = (int) ( cy * ( py.get(i) ) + 20 );
			    		
			    		int vy = (int) ( cx * ( px.get(j) ) + 20 );
			    		int vx = (int) ( cy * ( py.get(j) ) + 20 );
			    		
			    		int dw = 2;
			    		if(ux==vx)
			    		{
			    			if(uy>vy)  {ux+=dw;vx+=dw;}
			    			else {ux-=dw;vx-=dw;}
			    		}
			    		else
			    		{
			    			if(ux>vx) {uy-=dw;vy-=dw;}
			    			else {uy+=dw;vy+=dw;}
			    		}
			    		
			    		
			    		g.setStroke(new BasicStroke(1));
			    		g.setColor(Color.gray);
			    		g.drawLine(ux, uy, vx, vy);
					}
				}
				
			}
	    	for ( int i=1; i<=58; i++ ){
	    		g.setColor( new Color(0, 255,232) );///Green&Blue
	    		int y = (int) ( cx * ( px.get(i) ) + 20 );
	    		int x = (int) ( cy * ( py.get(i) ) + 20 );
	    		int R = 11;
	        	g.fillOval(x-R, y-R, 2*R, 2*R );
	        	
	    		g.setColor( Color.BLACK );
	    		g.setFont(new Font("Dotum",  Font.BOLD, 15));
	        	g.drawString(i<10?"0"+i:""+i, x-10, y+5);
	    	}
	    	
	    	if(flowdata!=null && flowdata.initOK &&  flowdata.lastTimID>=0)
	    	{
	    		int lastTimID = flowdata.lastTimID;
	    		for(int i=1;i<flowdata.roadNum;i++)
	    		{
	    			///if(flowdata.hasRoadID[i]==false) continue;
	    			int fromID = flowdata.roadLinktl[i][0];
	    			int atID   = flowdata.roadLinktl[i][1];
	    			int yfrom = (int) ( cx * ( px.get(fromID) ) + 20 );
		    		int xfrom = (int) ( cy * ( py.get(fromID) ) + 20 );
		    		int yat = (int) ( cx * ( px.get(atID) ) + 20 );
		    		int xat = (int) ( cy * ( py.get(atID) ) + 20 );
		    		
		    		int posx = (xfrom+xat)/2;
		    		int posy = (yfrom+yat)/2;
		    		int du =  20;
		    		int dd =  11;
		    		int dl =  40;
		    		int dr =  8;
		    		String prefix = "";
		    		if(xfrom==xat)
		    		{
		    			if(yfrom>yat)  {posx+=dr;prefix="A";}
		    			else {posx-=dl;prefix="v";}
		    			posy -= 8;
		    		}
		    		else
		    		{
		    			if(xfrom>xat) {posy-=du;prefix="<";}
		    			else {posy+=dd;prefix=">";}
		    			posx -= 10;
		    		}
		    		
		    		 
		    		
		    		int val = flowdata.roadFlow[i][lastTimID];
		    		if(val<=20) g.setColor( KOCO_Green );
		    		else if(val<=40) g.setColor( KOCO_LightBlue );
		    		else if(val<=80) g.setColor( KOCO_Yellow );
		    		else if(val<=160) g.setColor( KOCO_Orange );
		    		else g.setColor( Color.red );
		    		//int StayVal = flowdata.CalcuRoadStay(i, flowdata.lastTimID);//used for debug!
		    		
		    		g.setFont(new Font("Dotum",  Font.BOLD, 11));
		    		//g.setFont(new Font("Dotum",  Font.PLAIN,11));
		    		if(flowdata.hasRoadID[i]==true) g.drawString(new String((val<10?"   "+ val:"  "+val)), posx-6, posy+6);
		    		else 
		    		{
		    			g.setColor( Color.blue );
		    			g.drawString(new String("exit:"+val), posx-6, posy+6);
		    		}
		        	
		        	///Penalty:
		        	int roadPenalty = flowdata.road_penalty[i];
		    		if(roadPenalty<=1*(lastTimID+1)) g.setColor( KOCO_Green );
		    		else if(roadPenalty<=10*(lastTimID+1)) g.setColor( KOCO_LightBlue );
		    		else if(roadPenalty<=80*(lastTimID+1)) g.setColor( KOCO_Yellow );
		    		else if(roadPenalty<=400*(lastTimID+1)) g.setColor( KOCO_Orange );
		    		else g.setColor( Color.red );
		    		
		    		
		    		g.setFont(new Font("Dotum",  Font.BOLD, 11));
		    		//g.setFont(new Font("Dotum",  Font.PLAIN,11));
		    		if(flowdata.hasRoadID[i]==true) g.drawString(new String(""+roadPenalty), posx-4, posy+16);
		    		else
		    		{
		    			int sumFlow = flowdata.roadFlowSum[i][Math.min(1679,flowdata.lastTimID)];
		    			g.setColor( Color.blue );
		    			g.drawString(new String(""+sumFlow), posx-6, posy+16);
		    		}
		        	
		        	///SUM ;
		        	/*
		        	int valSum = flowdata.roadFlowSum[i][lastTimID];
		        	int valLastSum = 0;
		        	int segLen = 20;
		        	if(lastTimID>=segLen) valLastSum = flowdata.roadFlowSum[i][lastTimID-segLen];
		        	int SegSum = valSum - valLastSum;
		        	
		        	if(SegSum<=10) g.setColor( Color.green );
		    		else if(SegSum<=20) g.setColor( Color.blue );
		    		else if(SegSum<=100) g.setColor( Color.pink );
		    		else if(SegSum<=200) g.setColor( Color.orange );
		    		else g.setColor( Color.red );
		    		String pf = "0000";
		    		pf += SegSum;
		        	g.drawString(pf.substring(pf.length()-4), posx, posy+9);
		        	*/
		        	/*
		    		if(valSum<=200) g.setColor( Color.green );
		    		else if(valSum<=750) g.setColor( Color.blue );
		    		else if(valSum<=2000) g.setColor( Color.pink );
		    		else if(valSum<=7500) g.setColor( Color.orange );
		    		else g.setColor( Color.red );
		    		pf = "00000";
		    		pf += valSum;
		        	g.drawString(pf.substring(pf.length()-5), posx-3, posy+18);
		        	*/
		        	
		        	/* trueTrueRate Test*
		        	g.setColor( Color.black );
		        	g.setFont(new Font("Dotum",  Font.BOLD, 15));
		        	g.drawString(""+(int)(flowdata.trueTurnRate[i][0]*10)+(int)(flowdata.trueTurnRate[i][1]*10)+(int)(flowdata.trueTurnRate[i][2]*10)
		        			, posx-3, posy+18);
		        	*/
		        	
		        	/// old light labs : it's true and used for debug!
		        	/*
		        	for(int j=0;j<3;j++)
		        	{
		        		if(flowdata.tmp_trafficlight[i][j]==-1) g.setColor( Color.black );
		        		else if(flowdata.tmp_trafficlight[i][j]==0) g.setColor( Color.red );
		        		else  g.setColor( Color.green );
		        		g.fillOval(posx+j*8,posy+12, 6,6);
		        	}
		        	*/
		        	///Red|Green Light
		        	int d_x = 0,d_y = 0;
		        	int s_x = 0,s_y = 0;
		        	// LightPos = ( s_x + d_x*k + ddx, s_y + d_y*k +ddy) k =0,1,2
		        	int moveSegLen = 18;
		        	int lightSeglen = 8; 
		        	if(xfrom==xat)
		    		{
		    			if(yfrom>yat) 
		    			{
		    				///from is down,
		    				s_x = xat - lightSeglen;  s_y = yat + moveSegLen;
		    				d_x = lightSeglen;        d_y = 0;
		    			}
		    			else 
		    			{
		    				//from is up
		    				s_x = xat + lightSeglen;  s_y = yat - moveSegLen;
		    				d_x = -lightSeglen;       d_y = 0;
		    			}
		    			
		    		}
		    		else
		    		{
		    			if(xfrom>xat)
		    			{
		    				//from is right
		    				s_x = xat + moveSegLen;  s_y = yat + lightSeglen;
		    				d_x =  0;        		 d_y = -lightSeglen;
		    			}
		    			else 
		    			{
		    				//from is left
		    				s_x = xat - moveSegLen;  s_y = yat - lightSeglen;
		    				d_x =  0;        		 d_y = lightSeglen;
		    			}
		    		}
		        	int poslight[] = {0,0,0};
		        	poslight[0] = flowdata.tmp_trafficlight[i][0];
		        	poslight[1] = flowdata.tmp_trafficlight[i][2];
		        	poslight[2] = flowdata.tmp_trafficlight[i][1];
		        	for(int j=0;j<3;j++)
		        	{
		        		if(poslight[j]==-1) g.setColor( Color.black );
		        		else if(poslight[j]==0) g.setColor( Color.red );
		        		else  g.setColor( Color.green );
		        		g.fillOval(s_x+d_x*j-3,s_y+d_y*j-3, 6,6);
		        	}
		        
	    		}
	    		///Draw Penalty:
	    		int Penaltyy = (int) ( cx * ( px.get(46) ) + 20 );
	    		int Penaltyx = (int) ( cy * ( py.get(46) ) + 20 );
	    		int dPx = 70;
	    		g.setColor( KOCO_Orange );
	    		g.setFont(new Font("Dotum",  Font.BOLD, 11));
	    		String Num = "000000000"+ flowdata.SumPenalty;
	        	g.drawString(new String("SP:"+Num.substring(Num.length()-9)),Penaltyx-dPx,Penaltyy+170);
	    		for(int i=0;i<14;i++)
	    		{
	    			Num = "00000000"+ flowdata.penalty[i];
		        	g.drawString(new String("P"+(i<10?"0":"")+i+":"+Num.substring(Num.length()-8)),Penaltyx-dPx,Penaltyy+170 + (i+1)*12);
	    		}
	    		
	    		//Draw through_rate:
	    		g.setColor( Color.blue );
	    		g.drawString(new String("through_L: "+flowdata.through_rate[0]),Penaltyx-dPx,Penaltyy+170 + (16)*12);
	    		g.drawString(new String("through_R: "+flowdata.through_rate[1]),Penaltyx-dPx,Penaltyy+170 + (17)*12);
	    		g.drawString(new String("through_S:" +flowdata.through_rate[2]),Penaltyx-dPx,Penaltyy+170 + (18)*12);
	    		
	    		///tmp_tim
	    		g.setColor( KOCO_Green );
	    		g.drawString(new String("tim_cnt: "+NextAction.count_tick),Penaltyx-dPx,Penaltyy+170 + (21)*12 );
	    	}
	    	
	    	//System.out.println(size);
	    }
	}
	
	
    private static final class NextAction extends AbstractAction {
        /**
		 * 按下next button，执行一步。
		 */
    	public static int count_tick = 0;
		private static final long serialVersionUID = -8603262894746675457L;
        public void actionPerformed(ActionEvent e) {
        	//process_init();
        	System.out.println("Button down");
            System.out.flush();
            int runtim=1;
            System.out.println(e.getSource().toString());
            String fromsource = e.getSource().toString();
            int textpos = fromsource.indexOf("text=");
            int steppos = fromsource.indexOf("step",textpos);
            if(textpos!=-1 && steppos!=-1)
            {
            	runtim = Integer.parseInt( fromsource.substring(textpos+5,steppos) );
            	System.out.println(runtim);
            }
            try{
	            while(runtim>0)
	    		 {
	    			runtim--;

	    			flowdata.updata(count_tick);
	    			String _trafficFlow = flowdata.getTmpRoadFlow();
	    			
	    			bw.write( _trafficFlow + "\n" );
		    		bw.flush();	    
		    		//bw.close();	    	
		    		
		    		String s;
		            s = br.readLine();
		            ///System.out.println(count_tick + " : input : " + _trafficFlow);
		            ///System.out.println(count_tick + " : output: " + s);
		            ///System.out.flush();
		            
		            String ret = flowdata.updataTrafficLights(s);
		            System.out.println(count_tick + " trafficlight result : "+ret);
		            
		            int tmpPenalty = flowdata.updataPenalty(count_tick);
		            System.out.println(count_tick + " tPenalty : "+tmpPenalty + " ; All_Penalty : "+flowdata.SumPenalty);
		            
		            count_tick++;
		            
				} 
	            if(count_tick>=1700)
	            {
	            	count_tick = 0;
	            	flowdata.initJudgeFromTxt(JudgeTxt);
	            }
            }catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            self.repaint();

        }
    }
    	
	public static void main(String av[]) throws IOException{
		generate_world();
		self = new gui();
		//gui supergui = new gui();
	}
}

