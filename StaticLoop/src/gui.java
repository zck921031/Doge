import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.geom.Line2D;


public class gui extends JFrame  {
	private static String traffic_topo_str = Constants.traffic_topo_str.trim();
	private static String row_id = Constants.row_id.trim();
	private static String col_id = Constants.col_id.trim();
	
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
		        	int roadPenalty = (int)flowdata.road_penalty[i];
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
	    		String Num = "000000000"+ (int)flowdata.SumPenalty;
	        	g.drawString(new String("SP:"+Num.substring(Num.length()-9)),Penaltyx-dPx,Penaltyy+170);
	    		for(int i=0;i<14;i++)
	    		{
	    			Num = "00000000"+ (int)flowdata.penalty[i];
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

