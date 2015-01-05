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
	private static String traffic_topo_str = Constants.traffic_topo_str.trim();
	private static String row_id = Constants.row_id.trim();
	private static String col_id = Constants.col_id.trim();
	
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
	public double penalty[];///score of algorithm 120's T => one Penalty
	public double road_penalty[];
	public double SumPenalty;
		
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
		penalty = new double[1680/120+1];
		road_penalty = new double[roadNum];
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
				if(false)
				{
					///+ chuizhi fangxiang dou zhixing
					if(  tmp_trafficlight[id][2]==1 &&
					     (     (leftid>=1  && tmp_trafficlight[leftid][2] == 1 ) 
					       ||  (rightid>=1 && tmp_trafficlight[rightid][2] == 1  )
						 )  )
					{
						ret+=""+id+RoadString(id)+" VerticalConflictWithStraight-" +RoadString(leftid)+"|"+RoadString(rightid)+" ;";
					}
					///+ chuizhi youce buneng zuozhuan
					if( tmp_trafficlight[id][2]==1 && rightid>=1 && tmp_trafficlight[rightid][0] == 1)
					{
						ret+=RoadString(id)+" ConflictRightTurnLeft-"+RoadString(rightid)+" ;";
					}
				}
				
				
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
	
	public double updataPenalty(int TimID)
	{
		if(TimID>=1681||lastTimID==1680) {System.out.println("out of time range!");return 0;}
		double tmpSum = 0;
		for(int id=0;id<roadNum;id++)
		{
			if(hasRoadID[id]==false) continue;
			double tFlowStay = CalcuRoadStay(id,TimID);
			
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


