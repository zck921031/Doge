import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class FlowData
{
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
	
	public static double[] turn_rate = new double[]{0.1,0.1,0.8};///{L,R,S}
	public static int[]  through_rate = new int[]{2,2,16};///It's True
	//public static int[]  through_rate = new int[]{4,4,20};///It's Wrong
	public double trueTurnRate[][];///(roadID,3): + Intersection and T Intersection have different rate;
	public boolean hasTrafficLight[][];///(roadID,3) {0:L,1:R,2:S} hasTrafficLight[roadid][L/S/R]=true => there is an exit
	public int tmp_trafficlight[][];///(roadID,3) {0:L,1:R,2:S}
	public int history_trafficlight[][][];///(roadID,3,CountTime)
	public int GotoID[][];///(roadID,3){0:L,1:R,2:S}
	public int FromID[][];///(roadID,3){0:L,1:R,2:S} GotoID[ FromID[roadID][0|1|2] ][0|1|2] = roadID;
	public int antiRoadID[];///(roadID) roadID = (u,v); antiRoadID[roadID] = (v,u)
	public int penalty[];///score of algorithm 120's T => one Penalty
	public int road_penalty[];
	public int SumPenalty;
		
	public int roadID[][];
	public int roadLinktl[][];///a direct road : from u=roadLinktl[id][0] to v=roadLinktl[id][1] ( if roadID[u][v] = id )
	public int lightLinkRoad[][];/// A Light will connect to at most 4 roads.
	public int roadFlow[][];///tmpFlow
	public int roadFlowSum[][];///FlowSum
	public boolean hasRoadID[];///some road won't be used! and have no data!!
	public int roadNum;
	public int tlNum;///Number of traffic lights
	public int DataFromTable[][];///(roadID,time) get the flow from txt
	public boolean initOK=false;
	public int lastTimID;
	
	FlowData()
	{
		initOK = false;
		lastTimID = -1;
	}
	public void initJudgeFromFreedomData(int MAXUP)
	{
		initTrafficLogic();
		//freedom data
		for(int id = 1;id<roadNum;id++)
		{
			if(hasRoadID[id])
			{
				for(int t=0;t<1680;t++)
				{
					DataFromTable[id][t] = (int)(Math.random()*MAXUP);
				}
			}
		}
	}
	public void initJudgeFromTxt(String FileName)/// e.g ".\\data\\flow0901.txt"
	{
		initTrafficLogic();
		///read data from file_txt:
		{
			File file = new File(FileName);
	        BufferedReader reader = null;
	        try {
	            System.out.println("read file started !!");
	            reader = new BufferedReader(new FileReader(file));
	            String context = null;
	            int line = 0;
	            // 一次读入一行，直到读入null为文件结束
	            while ((context = reader.readLine()) != null) {
	                // 显示行号
	                ///System.out.println("line " + line + ": " + context);
	                line++;
	                
	                String [] strs = context.trim().split(",");
	                ///System.out.println((strs.length-2)+" | "+roadNum+" | id ");
	                int atID   = toid(strs[0]);
	                int fromID = toid(strs[1]);
	                int id = roadID[fromID][atID];
	                
	                ///System.out.println((strs.length-2)+" | "+roadNum+" | id "+id);
	                for(int i=2;i<strs.length;i++)
	                {
	                	DataFromTable[id][i-2] = toInt(strs[i]);            	
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
		System.out.println("read file : finished!");
	}
	public void initTrafficLogic()///define array and build logic of roads
	{
		initOK = false;
		lastTimID = -1;
		
		boolean graph[][] = new boolean[59][59];
		String[] lights = traffic_topo_str.trim().split(";");
		for(String light:lights)
		{
			String [] strset = light.trim().split(",");

			int u = toid( strset[0] );
			for (int i=0; i<5; i++){
				if ( 0==i ) continue;
				if ( strset[i].equals("#") ) continue;
				int v = toid(strset[i]);
				graph[u][v] = true;
				graph[v][u] = true;
			}
		}
			
		tlNum = graph.length;
		roadLinktl = new int[tlNum*4+20][2];
		roadID = new int[tlNum][tlNum];
		for(int i=0;i<tlNum;i++)for(int j=0;j<tlNum;j++) roadID[i][j] = -1;
		roadNum = 0;
		for(int i=1;i<tlNum;i++)
		for(int j=1;j<tlNum;j++)
		{
			if(graph[i][j])
			{
				roadNum ++;
				roadID[i][j] = roadNum;
				roadLinktl[roadNum][0] = i;
				roadLinktl[roadNum][1] = j;
			}
		}
		///System.out.println("..."+roadID[48][4]+" "+roadID[4][48]);
		roadNum++;
		hasRoadID = new boolean[roadNum];
		lightLinkRoad = new int[tlNum][4];
		roadFlow = new int[roadNum][1690];
		roadFlowSum = new int[roadNum][1690];
		DataFromTable = new int[roadNum][1690]; 
		history_trafficlight = new int[roadNum][3][1690];
		tmp_trafficlight = new int[roadNum][3];
		penalty = new int[1680/120+1];
		road_penalty = new int[roadNum];
		SumPenalty = 0;
		
		trueTurnRate = new double[roadNum][3];
		
		hasTrafficLight  = new boolean[roadNum][3]; 
		GotoID           = new int[roadNum][3];
		FromID           = new int[roadNum][3];
		antiRoadID       = new int[roadNum];
		for(int i=0;i<roadNum;i++)
		{
			for(int j=0;j<3;j++) FromID[i][j]=-1;
			antiRoadID[i]=-1;
		}
		///Build traffic logic!!
		for(String light:lights)
		{
			String [] strset = light.trim().split(",");
			int atID       = toid(strset[0]);
			int fromID     = toid(strset[1]);
			int leftID     = toid(strset[2]);
			int rightID    = toid(strset[3]);
			int straightID = toid(strset[4]);
			
			///if GotoID[][] == -1, it mean that there are no roads to go!
			int thisRoad = roadID[fromID][atID];
			
			GotoID[ thisRoad ][0] = roadID[atID][leftID];     //Left
			GotoID[ thisRoad ][1] = roadID[atID][rightID];    //Right
			GotoID[ thisRoad ][2] = roadID[atID][straightID]; //straight
			
			if(roadID[atID][leftID]!=-1) 	 FromID[ roadID[atID][leftID]     ][0] = thisRoad;//Left
			if(roadID[atID][rightID]!=-1) 	 FromID[ roadID[atID][rightID]    ][1] = thisRoad;//Right
			if(roadID[atID][straightID]!=-1) FromID[ roadID[atID][straightID] ][2] = thisRoad;//straight
			
			antiRoadID[  thisRoad ] = roadID[atID][fromID];
			antiRoadID[ roadID[atID][fromID] ] = thisRoad;
			
			hasRoadID[thisRoad] = true;
			
			for(int i=0;i<4;i++)
			{
				if(lightLinkRoad[atID][i]==0||lightLinkRoad[atID][i]==fromID)
				{
					lightLinkRoad[atID][i]=thisRoad;
					///System.out.println(""+atID+" : "+RoadString(thisRoad));
					break;
				}
			}
		}
		///Test:
		/*
		for(String light:lights)
		{
			String [] strset = light.trim().split(",");
			int atID       = toid(strset[0]);
			int fromID     = toid(strset[1]);
			
			///if GotoID[][] == -1, it mean that there are no roads to go!
			int thisRoad = roadID[fromID][atID];
			System.out.println(RoadString(thisRoad)+
					///"\t Go:L:"+RoadString(GotoID[ thisRoad ][0])+";R:"+RoadString(GotoID[ thisRoad ][1])+
					///";S:"+RoadString(GotoID[ thisRoad ][2])+
					"\tFrom : L:"+RoadString(FromID[ thisRoad ][0])+";R:"
					+RoadString(FromID[ thisRoad ][1])+";S:"+RoadString(FromID[ thisRoad ][2])
					+"; \tanti:"+RoadString(antiRoadID[ thisRoad ]));
			
		}
		*/
		
		//turnRate : 
		for(int id=1;id<roadNum;id++)
		{
			if(hasRoadID[id]==false) continue;
			for(int i=0;i<3;i++) trueTurnRate[id][i] = turn_rate[i];
			if (GotoID[id][0]<=0) {
				trueTurnRate[id][1] += trueTurnRate[id][0];
				trueTurnRate[id][0] -= trueTurnRate[id][0];
			}else if (GotoID[id][1]<=0) {
				trueTurnRate[id][0] += trueTurnRate[id][1];
				trueTurnRate[id][1] -= trueTurnRate[id][1];
			}else if (GotoID[id][2]<=0) {
				trueTurnRate[id][0] += trueTurnRate[id][2]*0.5;
				trueTurnRate[id][1] += trueTurnRate[id][2]*0.5;
				trueTurnRate[id][2] -= trueTurnRate[id][2];
			}
		}
		//Test:
/*		
		for(int id=1;id<roadNum;id++)
		{
			if(hasRoadID[id]==false) continue;
			System.out.println(RoadString(id)+"\t Rate:L:"+trueTurnRate[id][0]+";R:"+trueTurnRate[id][1]
					+";S:"+trueTurnRate[id][2]);
		}
*/		
		initOK = true;		
	}
	public int toid(String s){
		if("#".equals (s)) return 0; /// because node 0 is null
		///System.out.println("toid: "+s);
		return Integer.parseInt( s.substring(2) );
	}
	public int toInt(String s){
		return Integer.parseInt( s );
	}
	public String RoadString(int id)
	{
		if(id<=0) return "null Road";
		///if(roadLinktl[id][0]==0||roadLinktl[id][1]==0) System.out.println("badid: "+id);
		return "("+roadLinktl[id][0]+"->"+roadLinktl[id][1]+")";
	}
	
	//get traffic lights from algorithm's out.string
	public String updataTrafficLights(String TLstr)
	//TLstr = "tlatNodeID,tlfromNodeID,L,R,S;.......;"
	{
		///System.out.println("here!");
		String ret = "";
		String FourTJudge = "";
		String[] strs = TLstr.trim().split(";");///trim 去掉乱七八糟的无效字符？？
		for(String st : strs)
		{
			String[] ss = st.trim().split(",");
			int atID   = toid(ss[0]);
			int fromID = toid(ss[1]);
			int Roadid = roadID[fromID][atID];
			for(int i=0;i<3;i++)
			{
				tmp_trafficlight[Roadid][i] = toInt(ss[i+2]);
			}
		}
		///judge traffic light:
		for(int id=1;id<roadNum;id++)
		{
			if(hasRoadID[id])
			{
				///if(id==9)System.out.println("id=9  "+GotoID[id][0]+" "+RoadString(GotoID[id][0])+" - anti "+antiRoadID[ GotoID[id][0] ]);

				int leftid   = GotoID[id][0]>0?antiRoadID[ GotoID[id][0] ]:-1;
				int rightid  = GotoID[id][1]>0?antiRoadID[ GotoID[id][1] ]:-1;
				
				/// no exit but have light! or has exit but do not have light
				for(int i=0;i<3;i++)
				{
					if(GotoID[id][i]<=0 && tmp_trafficlight[id][i]!=-1)
					{
						tmp_trafficlight[id][i] = -1;
						ret+=RoadString(id)+" no exit Goto_"+i+" but have light; ";
					} 
					if(GotoID[id][i]>0 && tmp_trafficlight[id][i]==-1)
					{
						tmp_trafficlight[id][i] = 0;
						ret+=RoadString(id)+" has exit Goto_"+i+" but haven't light; ";
					}
				}
				
				///+ chuizhi fangxiang dou zhixing
//				if(  tmp_trafficlight[id][2]==1 &&
//				     (     (leftid>=1  && tmp_trafficlight[leftid][2] == 1 ) 
//				       ||  (rightid>=1 && tmp_trafficlight[rightid][2] == 1  )
//					 )  )
//				{
//					ret+=""+id+RoadString(id)+" VerticalConflictWithStraight-" +RoadString(leftid)+"|"+RoadString(rightid)+" ;";
//				}
//				///+ chuizhi youce buneng zuozhuan
//				if( tmp_trafficlight[id][2]==1 && rightid>=1 && tmp_trafficlight[rightid][0] == 1)
//				{
//					ret+=RoadString(id)+" ConflictRightTurnLeft-"+RoadString(rightid)+" ;";
//				}
				
				///updata history traffic lights and 
				for(int j=0;j<3;j++) history_trafficlight[id][j][lastTimID] = tmp_trafficlight[id][j];
				
				/// judge whether there are road get Red light for more than 4T time
				for(int j=0;j<3;j++)
				{
					int tt = lastTimID;
					while(tt>=0&&((tt+1)%120!=0)&&history_trafficlight[id][j][tt]==0)
					{
						tt--;
					}
					if(lastTimID - tt > 4)
					{
						FourTJudge += RoadString(id) + "Goto exit " + j +" has >4T; ";
					}
				}
			}
		}
		if("".equals(ret) && "".equals(FourTJudge)) return "Good Boy!! Great Light_Table!!";
		if("".equals(ret)) return FourTJudge;
		if("".equals(FourTJudge)) return ret;
		return ret+"\n\t"+FourTJudge;
	}
	public void updataTrafficLightToHistory()
	{
		///if(lastTimID<=5) System.out.println("hehe---"+lastTimID);
		for(int id=0;id<roadNum;id++)
		{
			if(hasRoadID[id]==false) continue;
			for(int j=0;j<3;j++) history_trafficlight[id][j][lastTimID] = tmp_trafficlight[id][j];
		}
		
	}
	///
	public int CalcuRoadStay(int roadid,int timID)/// the cars can't leave at timID of roadid
	{
		/// calculate the traffic flow from history_trafficlight
		int leftThrough=0,rightThrough=0,straightThrough=0;
		///only green light can pass
		if (history_trafficlight[roadid][0][timID]==1) {
			leftThrough = through_rate[0];
		}
		if (history_trafficlight[roadid][1][timID]==1) {
			rightThrough = through_rate[1];
		}
		if (history_trafficlight[roadid][2][timID]==1) {
			straightThrough = through_rate[2];
		}
		int tmpFlow = roadFlow[roadid][timID];
		int leftStay     = Math.max(0, (int)Math.ceil(tmpFlow * trueTurnRate[roadid][0]) - leftThrough     );
		int rightStay    = Math.max(0, (int)Math.ceil(tmpFlow * trueTurnRate[roadid][1]) - rightThrough    );
		int straightStay = Math.max(0, (int)Math.ceil(tmpFlow * trueTurnRate[roadid][2]) - straightThrough );
		return (leftStay + rightStay + straightStay);
	}
	
	public int updataPenalty(int TimID)
	{
		if(TimID>=1681||lastTimID==1680) {System.out.println("out of time range!");return 0;}
		int tmpSum = 0;
		for(int id=0;id<roadNum;id++)
		{
			if(hasRoadID[id]==false) continue;
			int tFlowStay = CalcuRoadStay(id,TimID);
			
			//更新，加上红绿灯违反交通规则的惩罚 a:直行垂直直行惩罚 b:直行垂直左转惩罚
			double a=0,b=0;		
			//交通违规的惩罚倍数
			double zeta =0.5;			
 
			int leftid = -1, rightid = -1;
			if ( GotoID[id][0] >= 0 ) leftid = antiRoadID[ GotoID[id][0] ];
			if ( GotoID[id][1] >= 0 ) rightid = antiRoadID[ GotoID[id][1] ];

			//垂直方向不能同时直行	
			if (tmp_trafficlight[id][2]==1 &&
					((leftid>=0 && tmp_trafficlight[leftid][2]==1) 
					|| (rightid>=0 && tmp_trafficlight[rightid][2]==1)) )
			{
				a += zeta*roadFlow[id][TimID];				
				if ( leftid>=0 ) {
					a += zeta*roadFlow[leftid][TimID];
				}
				if ( rightid>=0 ) {
					a += zeta*roadFlow[rightid][TimID];
				}
			}
			//直行时垂直方向右侧不能左转
			if ( tmp_trafficlight[id][2]==1 && rightid>=0 && tmp_trafficlight[rightid][0]==1 ) {
				b += zeta*(roadFlow[rightid][TimID] + roadFlow[id][TimID]);
			}

			//违规扣分
			tFlowStay += 0.5*a + b;
			
			road_penalty[id]   +=  tFlowStay;
			penalty[TimID/120] +=  tFlowStay;
			SumPenalty         +=  tFlowStay;
			tmpSum			   +=  tFlowStay;
		}
		return tmpSum;
	}
	
	
	public void updata(int timID)
	///timID will from [0,1680], we define timID == lastIimID+1!
	{
		if(timID>=1681||lastTimID==1680) {System.out.println("out of time range!");return;}
		//
		if(timID%120==0)// clearMemory|but we do not Remove memory| 
		{
			///System.out.println("????~~~~first updata");
			///isSpecial
			for(int i=0;i<roadNum;i++)
			{
				if(hasRoadID[i])
				{
					roadFlow[i][timID] = DataFromTable[i][timID];
					roadFlowSum[i][timID] = roadFlow[i][timID];
				}
				else 
				{
					if(timID>0) roadFlowSum[i][timID] = roadFlowSum[i][timID-1];///exit road has a special rule!
				}
			}
		}
		else
		{
			///System.out.println("????~~~~"+timID);
			for(int i=0;i<roadNum;i++)
			{
				///if(hasRoadID[i])
				///change : calculate how many cars left the map from the exit!
				{
					/// trueData' Cars & last T's Stay Cars
					if(hasRoadID[i]) roadFlow[i][timID]  =  (int)(DataFromTable[i][timID]/2);
					///roadFlow[i][timID] = (int)(Math.floor(DataFromTable[i][timID]*0.5));
					if(hasRoadID[i]) roadFlow[i][timID] +=  CalcuRoadStay(i,lastTimID);
					/// Flow in the road of last T
					int leftid    = FromID[i][0];
					int rightid   = FromID[i][1];
					int straightid= FromID[i][2];
					
					int leftIn = 0,rightIn = 0, straightIn = 0;
					
					if(leftid>0  && history_trafficlight[leftid][0][lastTimID]==1)
					{
						leftIn = Math.min(through_rate[0],(int)Math.ceil(roadFlow[leftid][lastTimID]*trueTurnRate[leftid][0]));
					}
					if(rightid>0  && history_trafficlight[rightid][1][lastTimID]==1)
					{
						rightIn = Math.min(through_rate[1],(int)Math.ceil(roadFlow[rightid][lastTimID]*trueTurnRate[rightid][1]));
					}
					if(straightid>0  && history_trafficlight[straightid][2][lastTimID]==1)
					{
						straightIn = Math.min(through_rate[2],(int)Math.ceil(roadFlow[straightid][lastTimID]*trueTurnRate[straightid][2]));
					}
					roadFlow[i][timID] += leftIn + rightIn + straightIn;
					//updata Sum Of roadflow
					roadFlowSum[i][timID] = roadFlow[i][timID] + roadFlowSum[i][timID-1];
				}
				
			}
		}
		
		lastTimID = timID;
	}
	
	public String getTmpRoadFlow()
	{
		StringBuilder ret = new StringBuilder();
		for(int i=0;i<roadNum;i++)
		{
			if(hasRoadID[i])
			{
				int fromID = roadLinktl[i][0];
				int atID   = roadLinktl[i][1];
				ret.append("tl"+atID+",tl"+fromID+","+roadFlow[i][lastTimID]+";");
			}
		}
		return ret.toString();
	}
	
	public String getTmpTrafficLight()
	{
		StringBuilder ret = new  StringBuilder();
		for(int i=0;i<roadNum;i++)
		{
			if(hasRoadID[i])
			{
				int fromID = roadLinktl[i][0];
				int atID   = roadLinktl[i][1];
				ret.append("tl"+atID+",tl"+fromID);
				for(int j=0;j<3;j++) ret.append(","+tmp_trafficlight[i][j]);
				ret.append(";");
			}
		}
		return ret.toString();
	}
	
	public void updataFromStringByMoniqi(String Input,int timID)
	{
		if(timID>1685) return;
		String[] strs = Input.trim().split(";");
		for(String str : strs)
		{
			String[] ss = str.trim().split(",");
			int fromID = toid(ss[1]);
			int atID   = toid(ss[0]);
			int flow   = toInt(ss[2]);
			roadFlow[ roadID[fromID][atID] ][timID] = flow;
		}
		lastTimID = timID;
	}
	
}


