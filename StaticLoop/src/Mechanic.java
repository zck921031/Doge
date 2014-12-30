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
import java.util.Vector;

public class Mechanic {
	static FlowData flowdata = null;
	static boolean debuglab = false;
	static boolean learninglab = false;
	static void DebugPrint(String context)
	{
		if(debuglab) System.out.println(context);
	}
	static void Debugger(String txtfile)throws NumberFormatException, IOException
	///用来debug的，即从txt读入数据，用flowdata模拟，再模拟器离线跑！
	{
		
		int count = 0;
		flowdata = new FlowData();
		flowdata.initJudgeFromTxt(txtfile);
		//flowdata.initJudgeFromFreedomData(8);
		DebugPrint(""+flowdata.initOK);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		DebugPrint("whether run 1 step by step? Y/N :");
		String onebyone = "N";
		onebyone = br.readLine();
		boolean onestepLab = "Y".equals(onebyone);
		while(count<1680)
		{
			flowdata.updata(count);
			
			String AI_trafficLightTable = Mechanic_AI(count);
			flowdata.updataTrafficLightToHistory();
            
            String ret = flowdata.updataTrafficLights(AI_trafficLightTable);
            DebugPrint(""+count + ": trafficlight result : "+ret);
            
            int tmpPenalty = flowdata.updataPenalty(count);
            DebugPrint(""+count + " tPenalty : "+tmpPenalty + " ; All_Penalty : "+flowdata.SumPenalty);			
			
			///wait for nxt step
            if(onestepLab){DebugPrint("press any key to continue:");String heheStr = br.readLine();}
			count++;
		}
		DebugPrint("End! Penalty is : "+flowdata.SumPenalty+
				"   through_rate :"+flowdata.through_rate[0]+","+flowdata.through_rate[1]+","+flowdata.through_rate[2]);
	}
	
	static String Mechanic_AI(int timID)
	//根据返回当前timID返回红绿灯策略
	{
		return ZCK_XXX(timID);
		//return WindhunterSB_EasyAI(timID);
		//return ZCK_EasyAI(timID);
	}
	
	static void initTrafficLightFreely()
	{
		for(int ltID = 1;ltID<flowdata.tlNum;ltID++)
		{
			//int roadA = flowdata.lightLinkRoad[ltID][0];
			int roadA = lightStartID[ltID];
			if(roadA<=0) continue;
			int roadS = flowdata.GotoID[roadA][2];
			int roadL = flowdata.GotoID[roadA][0];
			int roadR = flowdata.GotoID[roadA][1];
			
			// get anti
			roadS = roadS<=0? -1 : flowdata.antiRoadID[ roadS ];
			roadL = roadL<=0? -1 : flowdata.antiRoadID[ roadL ];
			roadR = roadR<=0? -1 : flowdata.antiRoadID[ roadR ];
			
			for(int i=0;i<3;i++)
			{
				if(roadA>0) {
					flowdata.tmp_trafficlight[roadA][i] = 1;
					if(flowdata.GotoID[roadA][i]<=0) flowdata.tmp_trafficlight[roadA][i] = -1;
				}
				if(roadS>0){
					flowdata.tmp_trafficlight[roadS][i] = 1;
					if(flowdata.GotoID[roadS][i]<=0) flowdata.tmp_trafficlight[roadS][i] = -1;
				}
				if(roadL>0){
					flowdata.tmp_trafficlight[roadL][i] = i==1?1:0;
					if(flowdata.GotoID[roadL][i]<=0) flowdata.tmp_trafficlight[roadL][i] = -1;
				}
				if(roadR>0){
					flowdata.tmp_trafficlight[roadR][i] = i==1?1:0;
					if(flowdata.GotoID[roadR][i]<=0) flowdata.tmp_trafficlight[roadR][i] = -1;
				}
			}

		}
	}
	
	//add by windhunter : as some road is not T road but can be as bad as T road!
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 ) return false;
		int Yu = 60;
		//if(ScoreRoad[roadID]<=25) return true;
		if(ScoreRoad[roadID]<=25 || flowdata.roadFlow[roadID][TimID]>= Yu) return true;
		return false;
	}
	
	static void changeTrafficLight(int timID)
	{
		///if(flowdata.lastTimID<=2) DebugPrint("?? -- ??"+flowdata.lastTimID);
		for(int i=1;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])
			{
				///int fromID = flowdata.roadLinktl[i][0];
				int atID   = flowdata.roadLinktl[i][1];
				boolean special3vs2 = (lightT[atID]==5); 
				boolean specialTcorossing = (lightT[atID]==6); 
				boolean specialTcorossing_2 = (lightT[atID]==7||lightT[atID]==8);/// exit类路口采用特殊策略
				
				///right light: it's independent !
				if(flowdata.tmp_trafficlight[i][1]!=-1)
				{
					/// 右行策略，如果是悲剧路口尽量不让右行去那里 --
					//if(ScoreRoad[flowdata.GotoID[i][1]]>25) 
					if( isExTRoad(flowdata.GotoID[i][1],flowdata.lastTimID) == false )
					//if(flowdata.roadFlow[ flowdata.GotoID[i][1] ][timID] <= 32 )
					{
						flowdata.tmp_trafficlight[i][1] = 1;
					}
					else
					{
						flowdata.tmp_trafficlight[i][1] = 0;
						int lastGreen = flowdata.lastTimID - 1;///bug ： this T should not be contained！！！
						while(lastGreen>=0 && flowdata.history_trafficlight[i][1][lastGreen]==0) lastGreen--;
						if(flowdata.lastTimID - lastGreen >= 4) flowdata.tmp_trafficlight[i][1] = 1;
						///if(flowdata.roadFlow[ flowdata.GotoID[i][1] ][flowdata.lastTimID] < 6)  flowdata.tmp_trafficlight[i][1] = 1;
					}
				}
				
				if(specialTcorossing)
				{
					int TroadNum = 0;//judge whether it's a T crossing :
					for(int ii=0;ii<4;ii++) if(flowdata.lightLinkRoad[atID][ii]>0) TroadNum++;
					if(TroadNum!=3) DebugPrint("err : "+atID+"Not a crossing");
					///这个路口有些特殊，因为是T字路口才能是6，所以这个路口只有一个入度路比较特别，三个入度边比较逗
					///一个路口是左右都有，一个有左没右，一个有右没左，用这个特性区别各路
					int mod = 4;//4T a cycle
					int tt  = (flowdata.lastTimID - (flowdata.lastTimID/120)*120)%mod;
					// straight light & left light design:
					if(flowdata.GotoID[i][0]>0&&flowdata.GotoID[i][1]>0) //left & right
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = -1;///no this light here
						//  left light 
						if(tt==0) flowdata.tmp_trafficlight[i][0] = 0;
						else      flowdata.tmp_trafficlight[i][0] = 1;
					}
					else if(flowdata.GotoID[i][0]>0) // only left
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = 1;///always green
						//  left light 
						flowdata.tmp_trafficlight[i][0] = 1;///always green
						///if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && tt!=0) 
						if( isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID) && tt!=0) 
							flowdata.tmp_trafficlight[i][0] = 0;
						
					}
					else // only right
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = (tt==0?1:0); // only first Period Green 
						//  left light 
						flowdata.tmp_trafficlight[i][0] = -1; // no left light
					}
					
					continue;//code follow down is not needed
				}
				
				if(specialTcorossing_2)
				{
					int NULLroadArr = -1;//查找哪个路是出去的，0,1,2分别是L，R，S；3是anti反向道路
					int NULLNum = 0;
					for(int ii=0;ii<3;ii++) 
						if(flowdata.GotoID[i][ii]>0 && flowdata.hasRoadID[ flowdata.GotoID[i][ii] ] == false) 
						{NULLroadArr=ii;NULLNum++;}
					if( flowdata.hasRoadID[flowdata.antiRoadID[i]]==false) {NULLroadArr = 3;NULLNum++;}
					if(NULLroadArr==-1||NULLNum==2) DebugPrint("err : "+atID+" Not an exit or have more than 1 exit");					
					///这个路口有些特殊，因为anti/goto1/goto2/goto3里必有一个是hasRoadID[]=false的路径
					int mod = (lightT[atID]==7?5:4);//4T a cycle
					int tt  = (flowdata.lastTimID - (flowdata.lastTimID/120)*120)%mod;
					if(NULLroadArr==2) //straight to exit 
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = (tt==mod-1?0:1);///no this light here
						//  left light 
						flowdata.tmp_trafficlight[i][0] = (tt==mod-1?0:1);
						//if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && tt!=0) 
						if( isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID) && tt!=0) 
							flowdata.tmp_trafficlight[i][0] = 0;
					}
					else if(NULLroadArr==0) //left to exit 
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = (tt==mod-1?1:0);
						//  left light 
						flowdata.tmp_trafficlight[i][0] = (tt==0?0:1);
						//if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && tt!=0) flowdata.tmp_trafficlight[i][0] = 0;
					}
					else  if(NULLroadArr==1) // right to exit
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = (tt==mod-1?1:0); // only first Period Green 
						//  left light 
						flowdata.tmp_trafficlight[i][0] = (tt==mod-1?1:0);; // left light
						//if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && tt!=0) flowdata.tmp_trafficlight[i][0] = 0;
					}
					else /// anti to exit
					{
						// straight light
						flowdata.tmp_trafficlight[i][2] = (tt==0?1:0); // only first Period Green 
						//  left light 
						flowdata.tmp_trafficlight[i][0] = (tt==mod-1?0:1);; // left light
						//if(flowdata.GotoID[i][0]>0 && ScoreRoad[flowdata.GotoID[i][0]]<=25 && tt!=0) 
						if( flowdata.GotoID[i][0]>0  && isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID) && tt!=0) 
							flowdata.tmp_trafficlight[i][0] = 0;
					}
					for(int ii=0;ii<3;ii++) if(flowdata.GotoID[i][ii]<=0) flowdata.tmp_trafficlight[i][ii] = -1;
					continue;//code follow down is not needed
				}
				
				///straight light:
				if(flowdata.tmp_trafficlight[i][2]!=-1)
				{
					if(lightT[atID]!=1)//special road!
					{
						int mod = lightT[atID]+1;
						if(special3vs2) mod = 5;///special 3:2
						int tt  = (flowdata.lastTimID - (flowdata.lastTimID/120)*120)%mod;
						if((special3vs2==false&&tt<mod-1)||(special3vs2&&tt<3)) 
							flowdata.tmp_trafficlight[i][2] = flowdata.history_trafficlight[i][2][0];
						else 
							flowdata.tmp_trafficlight[i][2] = flowdata.history_trafficlight[i][2][0]^1;
						
						///this maybe not be useful!
						/*
						if( isExTRoad( flowdata.GotoID[i][2] , flowdata.lastTimID) )
						/// do something to cut flow in the road!
						{
							if(flowdata.tmp_trafficlight[i][2] == 1 && 
									( (tt!=0 && flowdata.history_trafficlight[i][2][0]==1) || 
											(special3vs2 && tt!=3&& flowdata.history_trafficlight[i][2][0]==0) ))
							{
								flowdata.tmp_trafficlight[i][2] = 0;
							}
						}
						*/
					}
					else 
					{
						///add some special rule, for T road!
						//flowdata.tmp_trafficlight[i][2] = flowdata.tmp_trafficlight[i][2]^1;
						flowdata.tmp_trafficlight[i][2] = flowdata.history_trafficlight[i][2][0];
						if(flowdata.lastTimID%2==1) flowdata.tmp_trafficlight[i][2]^=1;
						if(isExTRoad( flowdata.GotoID[i][2] , flowdata.lastTimID) && flowdata.lastTimID%4 >=2) 
						{
							///if(flowdata.roadFlow[ flowdata.GotoID[i][0] ][flowdata.lastTimID] >= 6) 
								flowdata.tmp_trafficlight[i][2]=0;
						}
					}
				}
				///left light:
				if(flowdata.tmp_trafficlight[i][0]!=-1)
				{
					if(lightT[atID]!=1)//special road!
					{
						int mod = lightT[atID]+1;
						if(special3vs2) mod = 5;///special 3:2
						int tt  = (flowdata.lastTimID - (flowdata.lastTimID/120)*120)%mod;
						if((special3vs2==false&&tt<mod-1)||(special3vs2&&tt<3)) 
							flowdata.tmp_trafficlight[i][0] = flowdata.history_trafficlight[i][0][0];
						else 
							flowdata.tmp_trafficlight[i][0] = flowdata.history_trafficlight[i][0][0]^1;
						//T 类路，X：1的X部分，现在不是第一个绿
						//if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && flowdata.history_trafficlight[i][0][0]==1 && tt!=0)
						if(  isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID)  && flowdata.history_trafficlight[i][0][0]==1 && tt!=0) 
						{
							flowdata.tmp_trafficlight[i][0] = 0;
						}
						//else if(special3vs2 && ScoreRoad[flowdata.GotoID[i][0]]<=25 && flowdata.history_trafficlight[i][0][0]==0 && tt!=3)
						else if(special3vs2 &&  isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID)  && flowdata.history_trafficlight[i][0][0]==0 && tt!=3) 
						{
							flowdata.tmp_trafficlight[i][0] = 0;
						}
					}
					else
					{
						flowdata.tmp_trafficlight[i][0] = flowdata.history_trafficlight[i][0][0];
						if(flowdata.lastTimID%2==1) flowdata.tmp_trafficlight[i][0]^=1;
						//if(ScoreRoad[flowdata.GotoID[i][0]]<=25 && flowdata.lastTimID%4 >=2 )
						if(isExTRoad( flowdata.GotoID[i][0] , flowdata.lastTimID) && flowdata.lastTimID%4 >=2) 
						{
							///if(flowdata.roadFlow[ flowdata.GotoID[i][0] ][flowdata.lastTimID] >= 6) 
								flowdata.tmp_trafficlight[i][0]=0;
						}
					}						
				}
			}
		}
	}
	
	///data for Wind_AI
	static int ScoreRoad[];///different road have different Score, high Score is good! like road on T path
	static String special_path = "4,37,11,18,43,41,25,32,55;5,38,12,19,44,42,26,33,56;2,9,16,39,23,30,54;";
	static String my_XT ="30,2;";
	static int lightT[];///起始：另一侧亮灯周期比X：1
	static int lightStartID[];//一个路口红绿灯起始绿的road
	/* 讲一下lightT与lightStartID确定一个路口的原理：lightStartID[atID]决定第一次红绿灯的绿灯在南北还是东西，不妨假设在南北方向，
	 * lightT决定的是这个路口的红绿灯开关比例，因为在南北方向先绿，所以之后lightT个周期南北都是绿，而第lightT+1那个周期是红
	 * 东西方向恰好相反。所以南北：东西 = lightT：1。这是对直行的
	 * 左转，我们不希望左转到T字路上，但是又必须转一些，所以我们只在（lightT+1）周期的第一个周期南北左转，东西左转完全和东西直行一样。
	 * 这个方案不算完全但是减小了搜索空间，进一步搜索以后再说。。。。。
	 */
	static void initScoreRoad()
	{
		int roadNum = flowdata.roadNum;
		ScoreRoad = new int[roadNum];
		for(int i=1;i<roadNum;i++)
		{
			int go = i,cnt=0;
			while(flowdata.hasRoadID[go] && flowdata.GotoID[go][2]>0) {go = flowdata.GotoID[go][2];cnt++;}
			if(flowdata.hasRoadID[go]==false){///exits
				ScoreRoad[i] = 50-cnt*2;
			}
			else{/// T road - -
				ScoreRoad[i] = cnt*2;
			}
		}
	}
	static void Windhunter_firstTimInit()
	{
		initScoreRoad();
		
		String[] Light_TX = my_XT.trim().split(";");
		int[][] LightID_TX = new int[Light_TX.length][2];
		for(int i=0;i<Light_TX.length;i++) 
		{
			String[] ss = Light_TX[i].split(",");
			LightID_TX[i][0] = flowdata.toInt(ss[0]);
			LightID_TX[i][1] = flowdata.toInt(ss[1]);
		}
		
		int tlNum = flowdata.tlNum;
		lightT = new int[tlNum];
		lightStartID = new int[tlNum];
		String[] strs = special_path.trim().split(";");
		for(String str : strs)
		{
			String[] s = str.trim().split(",");
			int len = s.length;
			for(int i=0;i<len-1;i++)
			{
				int atID    = flowdata.toInt(s[i]);
				int fromID  = flowdata.toInt(s[i+1]);
				lightT[atID] = 4;
				lightStartID[atID] = flowdata.roadID[fromID][atID];
				for(int ii=0;ii<Light_TX.length;ii++) if(LightID_TX[ii][0]==atID) lightT[atID] = LightID_TX[ii][1];
			}
		}
		for(int i=1;i<tlNum;i++)
		{
			if(lightT[i]!=0) continue;
			lightT[i] = 1;
			lightStartID[i] = flowdata.lightLinkRoad[i][0];
		}
	}
	///学习出来的交通等亮灯规则：SmartStatic_LightRule
	static String SmartStatic_LightRule = "1,23,1;2,30,3;3,127,3;4,130,4;5,134,3;6,158,1;7,27,1;8,31,1;9,52,3;"
			+ "10,56,3;11,60,4;12,64,4;14,50,1;15,29,4;16,137,4;17,140,4;18,151,4;19,154,3;20,93,3;21,97,1;22,77,1;"
			+ "23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;28,95,1;29,165,1;30,166,2;31,83,1;32,167,4;33,168,4;"
			+ "34,124,1;35,170,1;36,37,3;37,41,4;38,44,4;39,80,3;40,84,4;41,88,4;42,92,3;43,147,4;44,150,3;";
	static String SmartStatic_LightRule2_Added_3vs2 ="1,23,5;2,30,3;3,127,3;4,130,4;5,134,3;6,158,1;7,27,5;"
			+ "8,31,1;9,52,2;10,56,5;11,60,4;12,64,4;14,50,5;15,29,4;16,137,4;17,140,4;18,151,5;19,154,3;"
			+ "20,93,3;21,97,1;22,77,1;23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;28,73,5;29,165,1;"
			+ "30,166,2;31,83,1;32,167,4;33,168,4;34,169,2;35,170,5;36,37,4;37,41,4;38,44,4;39,80,5;"
			+ "40,84,5;41,88,5;42,92,3;43,147,4;44,150,3;";
	static String SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance ="1,23,5;2,30,3;3,127,3;4,130,4;5,134,3;"
			+ "6,158,1;7,27,5;8,31,1;9,52,2;10,56,5;11,60,4;12,135,3;14,50,5;15,29,4;16,137,4;17,140,4;18,151,5;"
			+ "19,154,3;20,93,3;21,97,1;22,77,1;23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;28,73,5;29,165,1;"
			+ "30,166,2;31,83,1;32,167,4;33,168,4;34,169,2;35,170,5;36,37,5;37,41,4;38,44,3;39,80,5;40,84,5;"
			+ "41,88,5;42,92,3;43,147,4;44,150,3;";

	static String SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6 = "1,23,6;2,30,3;3,127,3;"
			+ "4,130,4;5,134,4;6,162,1;7,27,4;8,31,6;9,52,5;10,56,5;11,60,4;12,135,6;14,50,6;15,29,4;16,137,4;"
			+ "17,140,4;18,151,5;19,154,3;20,93,3;21,97,6;22,77,6;23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;"
			+ "28,73,5;29,105,1;30,166,2;31,83,1;32,167,4;33,168,4;34,169,2;35,170,5;36,37,5;37,41,4;38,44,6;"
			+ "39,80,5;40,84,5;41,88,5;42,92,3;43,147,4;44,150,3;";
	static String SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6_Added_ExitCross7 = "1,157,7;"
			+ "2,30,7;3,5,1;4,130,7;5,134,7;6,158,1;7,27,7;8,31,6;9,52,5;10,56,5;11,60,4;12,135,6;14,50,6;15,29,2;"
			+ "16,137,4;17,140,4;18,151,5;19,154,3;20,93,3;21,97,6;22,77,6;23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;"
			+ "28,73,5;29,165,1;30,166,2;31,83,1;32,167,7;33,168,7;34,169,2;35,170,5;36,37,4;37,41,4;38,44,6;39,80,5;40,84,5;"
			+ "41,88,5;42,92,3;43,147,4;44,150,3;";
	static String SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6_Added_ExitCross7_8 = "1,157,7;2,30,7;"
			+ "3,127,4;4,130,7;5,134,7;6,158,1;7,27,7;8,31,6;9,52,5;10,56,5;11,60,4;12,135,6;14,50,6;15,29,2;16,137,4;"
			+ "17,140,4;18,151,5;19,154,3;20,93,3;21,97,6;22,77,6;23,104,4;24,108,4;25,111,4;26,115,4;27,119,4;28,73,5;"
			+ "29,165,1;30,166,2;31,112,3;32,167,7;33,168,7;34,169,2;35,170,5;36,37,4;37,41,4;38,44,6;39,80,5;40,84,5;"
			+ "41,88,5;42,92,3;43,147,4;44,150,3;";
	//static String SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6_Added_ExitCross7_8_changeStraightRule = "";
	static String SmartStatic_LightRule3_from1vs1 = "1,23,5;2,159,3;3,11,1;4,160,3;5,161,2;6,162,1;7,27,5;"
			+ "8,31,1;9,52,2;10,56,1;11,60,3;12,135,3;14,50,1;15,29,3;16,137,3;17,140,3;18,151,1;19,154,3;"
			+ "20,72,1;21,20,1;22,77,1;23,104,4;24,108,4;25,111,4;26,115,3;27,119,3;28,123,5;29,105,5;"
			+ "30,166,5;31,83,1;32,167,4;33,168,4;34,169,2;35,99,5;36,37,3;37,41,3;38,44,3;39,80,5;"
			+ "40,84,2;41,88,2;42,92,3;43,147,2;44,150,3;";
	static void Windhunter_firstTimInit_Smarter()
	{
		initScoreRoad();
		
		lightT = new int[flowdata.tlNum];
		lightStartID = new int[flowdata.tlNum];
		
		//String[] strs = SmartStatic_LightRule.trim().split(";");
		//String[] strs = SmartStatic_LightRule2_Added_3vs2.trim().split(";");
		//String[] strs = SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance.trim().split(";");
		///String[] strs = SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6.trim().split(";");
		///String[] strs = SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6_Added_ExitCross7.trim().split(";");
		String[] strs = SmartStatic_LightRule2_Added_3vs2_Added_coinAdvance_Added_Tcrossing6_Added_ExitCross7_8.trim().split(";");
		//String[] strs = SmartStatic_LightRule3_from1vs1.trim().split(";");
		
		
		for(String tstr : strs)
		{
			String[] ss = tstr.trim().split(",");
			int tlID    = flowdata.toInt(ss[0]);
			int StartID = flowdata.toInt(ss[1]);
			int ltT  = flowdata.toInt(ss[2]);
			lightT[tlID] = ltT;
			lightStartID[tlID] = StartID;
		}
	}
	
	static void windhunter_Simple_firstTimeInit()
	{
		initScoreRoad();
		lightT = new int[flowdata.tlNum];
		lightStartID = new int[flowdata.tlNum];
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{
			if(flowdata.lightLinkRoad[tlID][0]<=0) continue;
			lightT[tlID] = 1;
			lightStartID[tlID] = flowdata.lightLinkRoad[tlID][0];
		}
	}
	
	static String WindhunterSB_EasyAI(int timID)
	{
		///按流量比确定直行红绿灯时间
		///第一次需要初始化一些静态数据
		if(timID==0)
		{
			//Windhunter_firstTimInit();
			Windhunter_firstTimInit_Smarter();
		}
		if(timID%120==0)
		{
			initTrafficLightFreely();
		}
		else
		{
			changeTrafficLight(timID);
		}
		///simpleFix(timID);
		return flowdata.getTmpTrafficLight();
	}
	static String ZCK_XXX(int timID)
	{
		WindhunterSB_EasyAI(timID);
		simpleFix(timID);
		return flowdata.getTmpTrafficLight();
	}
	
	/*
	 * Add by zck 
	 * 在AI返回前调用。                                                                                                                                                                                                                                                                                                                           
	 */
	static Vector<Integer> leave_city_road_id = new Vector<Integer> ();
	static void init_simpleFix()
	{
		leave_city_road_id.clear();
		for (int i=0; i<Constants.leavecity.length; i++)
		{
			String str[] = Constants.leavecity[i].trim().split("-");
			int u = flowdata.toid( str[0] ); 
			int v = flowdata.toid( str[1] );
			leave_city_road_id.add( flowdata.roadID[u][v] );
		}
	}
	static void simpleFix(int timID)
	{
		if ( timID == 0 ) init_simpleFix();
		
		for (int i=leave_city_road_id.size()-1; i>=0; i--)
		{		
			flowdata.tmp_trafficlight[leave_city_road_id.get(i)][2] = 1;			
		}
	}
	
	static String ZCK_EasyAI(int timID)
	{
		if(timID==0)
		{
			Windhunter_firstTimInit_Smarter();
		}
		if(timID%120==0) initTrafficLightFreely();
		else
		{
			for(int i=1;i<flowdata.roadNum;i++) if(flowdata.hasRoadID[i])
			{
				for(int j=0;j<3;j++) if(j!=1&&flowdata.tmp_trafficlight[i][j]!=-1) flowdata.tmp_trafficlight[i][j] = flowdata.tmp_trafficlight[i][j]^1;
			}
		}
		return flowdata.getTmpTrafficLight();
	}
	static int AdvanceTimes[];///防止一个红绿灯被提升过多次
	static int UP_AdvanceTime = 4;
	/// lightT[] , lightStartID[] are what we should use
	
	static int FastRunning120T()
	///快速循环120次并计算当前红路灯策略总得分
	{
		flowdata.lastTimID = -1;
		flowdata.SumPenalty = 0;///初始化,不初始化起始也行~~，呵呵
		
		//神奇的是下面的不初始化居然计算会出错，不明白为啥？ ==》 问题已经找到，向右转绿灯策略有个越界
		//for(int i=0;i<flowdata.roadNum;i++)for(int j=0;j<3;j++)for(int k=0;k<120;k++) flowdata.history_trafficlight[i][j][k] = 0;
		//------------------------------------------------------
		int SumPenalty = 0;
		for(int i=0;i<1680;i++)
		{
			flowdata.updata(i);
			///注意不需要运行Windhunter_firstTimInit()，因为canAdvance里已经准备好了lightT，lightStartID
			if(i==0) initTrafficLightFreely();
			else changeTrafficLight(i);
			flowdata.updataTrafficLightToHistory();
			//if(i<=5)DebugPrint(flowdata.getTmpTrafficLight());
			//if(i<=5) DebugPrint(""+flowdata.history_trafficlight[flowdata.roadID[4][37]][1][i]);
				
			int tmpPenalty = flowdata.updataPenalty(i);
			SumPenalty += tmpPenalty;
			///DebugPrint(""+i + " tPenalty : "+tmpPenalty + " ; All_Penalty : "+flowdata.SumPenalty);		
		}
		///DebugPrint(":"+SumPenalty);		
		return SumPenalty;
	}
	static int[] canAdvance()
	//任务查找一个路口修改后全局得分最大，类似模拟退火，但是这个路口不能已经给修改超过UP_AdvanceTime次，如果已经达到局部最优，return null；
	{
		int LastPenalty = FastRunning120T();
		 ///FastRunning120T();FastRunning120T();FastRunning120T();
		//DebugPrint("LastPenalty:"+LastPenalty+" : "+FastRunning120T()+" : "+flowdata.SumPenalty);
		int BestAtID = 0,BestRoadID = 0,BeastT = 1,BestAdvanceValue = 0;///value最大提高分数，atID最优时所在路口，fromID标记东西or南北
		int BestAtID2 = 0,BestRoadID2 = 0,BeastT2 = 1;
		int BestNum = 0;
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{
			int roadA = lightStartID[tlID];
			if(roadA<=0 || AdvanceTimes[tlID] >= UP_AdvanceTime)
			///该路口没有红绿灯 或者 该路口北优化过UP_AdvanceRime次
			{
				if(roadA>0) DebugPrint("UP_AdvanceTime:"+tlID);
				continue;
			}
			int roadL = flowdata.GotoID[roadA][0];
			int roadR = flowdata.GotoID[roadA][1];
			// get anti
			roadL = roadL<=0? -1 : flowdata.antiRoadID[ roadL ];
			roadR = roadR<=0? -1 : flowdata.antiRoadID[ roadR ];
			int roadX;///垂直方向
			if(roadL>0) roadX = roadL;else roadX = roadR;
			if(roadX<=0) ///理论上不会发生
			{
				DebugPrint("no roadX! "+tlID + ","+roadA);
				continue;
			}
			
			///记忆原始值用于还原
			int memStartID = lightStartID[tlID];
			int memT       = lightT[tlID];
			
			for(int nxtT = 1;nxtT<=5;nxtT++)///T可以取1~4,5是个特例，它代表3:2
			{
				lightT[tlID] = nxtT;
				//还是roadA先亮
				lightStartID[tlID] = roadA;
				int tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadA;
					BeastT = nxtT;
					BestNum = 1;
				}
				//roadX先亮
				lightStartID[tlID] = roadX;
				tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadX;
					BeastT = nxtT;
					BestNum = 1;
				}
			}
			int TroadNum = 0;
			for(int ii=0;ii<4;ii++) if(flowdata.lightLinkRoad[tlID][ii]>0) TroadNum++;
			if(TroadNum==3)//T crossing
			{
				lightT[tlID] = 6;
				//还是roadA先亮
				lightStartID[tlID] = roadA;
				int tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadA;
					BeastT = 6;
					BestNum = 1;
				}
			}
			int NULLroadArr = -1;//查找哪个路是出去的，0,1,2分别是L，R，S；3是anti反向道路
			int NULLNum = 0;
			for(int ii=0;ii<3;ii++) 
				if(flowdata.GotoID[roadA][ii]>0 && flowdata.hasRoadID[ flowdata.GotoID[roadA][ii] ] == false) 
				{NULLroadArr=ii;NULLNum++;}
			if( flowdata.hasRoadID[flowdata.antiRoadID[roadA]]==false) {NULLroadArr = 3;NULLNum++;}
			if(NULLroadArr!=-1 && NULLNum==1)//T crossing
			{
				lightT[tlID] = 7;
				//还是roadA先亮
				lightStartID[tlID] = roadA;
				int tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadA;
					BeastT = 7;
					BestNum = 1;
				}
				
				lightT[tlID] = 8;
				//还是roadA先亮
				lightStartID[tlID] = roadA;
				tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadA;
					BeastT = 8;
					BestNum = 1;
				}
			}
			
			lightStartID[tlID] = memStartID;
			lightT[tlID] = memT;
		}
		///添加提升规则，联合提升，即向量的两个路口同时change，注意同时change符合两个特点：1、他们的start——road是同一条；2、他们的lightT一样大；
		/*测试发现其提升效率不高：还会降低运行速度
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{
			int roadA = lightStartID[tlID];
			if(roadA<=0 || AdvanceTimes[tlID] >= UP_AdvanceTime)
			///该路口没有红绿灯 或者 该路口北优化过UP_AdvanceRime次
			{
				if(roadA>0) DebugPrint("UP_AdvanceTime:"+tlID);
				continue;
			}
			int tlID2 = (flowdata.roadLinktl[roadA][0]==tlID?flowdata.roadLinktl[roadA][1]:flowdata.roadLinktl[roadA][0]);
			if(lightStartID[tlID2]<=0 && tlID2<tlID) continue;
			int roadA2 = flowdata.antiRoadID[roadA];
			if(roadA2 <= 0) continue;
			///记忆原始值用于还原
			int memStartID = lightStartID[tlID];
			int memT       = lightT[tlID];
			int memStartID2= lightStartID[tlID2];
			int memT2      = lightT[tlID2];
			
			for(int nxtT = 1;nxtT<=5;nxtT++)///T可以取1~4,5是个特例，它代表3:2
			{
				lightT[tlID] = nxtT;
				//还是roadA先亮
				lightStartID[tlID] = roadA;
				
				lightStartID[tlID2] = roadA2;
				lightT[tlID2] = nxtT;
				
				int tmpPenalty = FastRunning120T();
				if(LastPenalty-tmpPenalty>BestAdvanceValue)///有提升
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BestRoadID = roadA;
					BeastT = nxtT;
					BestNum = 2;
					BestAtID2 = tlID2;
					BestRoadID2 = roadA2;
					BeastT2 = nxtT;
				}
				
			}
			lightStartID[tlID] = memStartID;
			lightT[tlID] = memT;
			lightStartID[tlID2] = memStartID2;
			lightT[tlID2] = memT2;
		}
		*/
		
		if(BestAdvanceValue==0) return null;
		return new int[] {BestAtID,BestRoadID,BeastT,BestAdvanceValue,BestNum,BestAtID2,BestRoadID2};
	}
	
	static void windhunterLearning(String filetxt)
	{
		flowdata = new FlowData();
		flowdata.initJudgeFromTxt(filetxt);
		int[] AdvanceInfo ;
		AdvanceTimes = new int[flowdata.tlNum];
		int RunningTim = 0;
		DebugPrint("Leaarning Started!");
		//两种起始方案：
		//1:从所有都是1:1开始学习
		///windhunter_Simple_firstTimeInit();
		
		//2:从Windhunter_firstTimInit开始学习（进一步优化）
		///Windhunter_firstTimInit();
		Windhunter_firstTimInit_Smarter();
		
		int fullAdvance = 0;
		while( (AdvanceInfo = canAdvance()) != null )
		{
			DebugPrint(""+RunningTim+" : BestTL:"+AdvanceInfo[0]+",BestRoad:"+flowdata.RoadString( AdvanceInfo[1] )+
					",BestT:"+AdvanceInfo[2]+",advance:"+AdvanceInfo[3]+";"+(AdvanceInfo[4]==1?"":""+  
							"- BestTL2 :"+AdvanceInfo[5]+",BestRoad:"+flowdata.RoadString( AdvanceInfo[6] )));
			fullAdvance += AdvanceInfo[3];
			lightStartID[AdvanceInfo[0] ] = AdvanceInfo[1];
			lightT[AdvanceInfo[0] ] = AdvanceInfo[2];
			if(AdvanceInfo[4]==2)
			{
				lightStartID[AdvanceInfo[5] ] = AdvanceInfo[6];
				lightT[AdvanceInfo[5] ] = AdvanceInfo[2];
			}
			RunningTim++;
			if(RunningTim>=50) 
			{
				DebugPrint("Out Of Max Tim:" + RunningTim);
				break;
			}
		}
		DebugPrint("Learning End! and Advanced : " + fullAdvance);
		///OutPut Traffic Light Rule Table:
		StringBuilder ret = new  StringBuilder();
		for(int tlID = 1;tlID < flowdata.tlNum;tlID++)
		{
			if(flowdata.lightLinkRoad[tlID][0]>0)
			{
				ret.append(""+tlID+","+lightStartID[tlID]+","+lightT[tlID]+";");
			}
		}
		DebugPrint(ret.toString());
	}
	public static void main(String[] args) throws NumberFormatException, IOException{
		
		///if Learning?
		//learninglab = true;
		if(learninglab)
		{
			debuglab = true;///for print 
			windhunterLearning("./data/flow0901.txt");
			return;
		}
		
		///if debug?
		//debuglab = true;
		if(debuglab)
		{
			Debugger("./data/flow0901.txt");
			return;
		}
		int count = 0;
		flowdata = new FlowData();
		flowdata.initTrafficLogic();
				
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String flows_str = br.readLine();	
		while(!"end".equalsIgnoreCase(flows_str)){
			///push data to flowdata from readString
			flowdata.updataFromStringByMoniqi(flows_str,count);
			///Do AI function
			String AI_trafficLightTable = Mechanic_AI(count);
			flowdata.updataTrafficLightToHistory();
			System.out.println(AI_trafficLightTable);
			System.out.flush();
			flows_str = br.readLine();		
			count += 1;
			if(count==1680) 
			{
				count=0;
				flowdata.initTrafficLogic();
			}
		}			
	}
}



