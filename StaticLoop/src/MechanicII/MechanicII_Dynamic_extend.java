package MechanicII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.io.File;   
import java.io.FileOutputStream;  

public class MechanicII_Dynamic_extend {
	static int partNum = 2;//120T分为几段
	static FlowData flowdata = null;
	static MechanicII_Model lightmodel = null;/// haven't init
	static boolean debuglab = false;
	static boolean learninglab = false;
	static void DebugPrint(String context){if(debuglab) System.out.println(context);}
	static void Debugger(String txtfile)throws NumberFormatException, IOException
	///用来debug的，即从txt读入数据，用flowdata模拟，再模拟器离线跑！
	{
		int count = 0;
		flowdata = new FlowData();
		flowdata.initJudgeFromTxt(txtfile);
		//flowdata.initJudgeFromFreedomData(8);
		DebugPrint(""+flowdata.initOK);
		
		lightmodel = new MechanicII_Model();
		lightmodel.initTableFrom();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		DebugPrint("whether run 1 step by step? Y/N :");
		String onebyone = "N";
		onebyone = br.readLine();
		boolean onestepLab = "Y".equals(onebyone);
		while(count<1680)
		{
			flowdata.updata(count);
			String AI_trafficLightTable = MechanicII_AI(count);
			flowdata.updataTrafficLightToHistory();
            String ret = flowdata.updataTrafficLights(AI_trafficLightTable);
            DebugPrint(""+count + ": trafficlight result : "+ret);
            double tmpPenalty = flowdata.updataPenalty(count);
            DebugPrint(""+count + " tPenalty : "+(int)tmpPenalty + " ; All_Penalty : "+(int)flowdata.SumPenalty);			
            if(onestepLab){DebugPrint("press any key to continue:");String heheStr = br.readLine();}
			count++;
		}
		DebugPrint("End! Penalty is : "+(int)flowdata.SumPenalty+"   through_rate :"+flowdata.through_rate[0]+","+flowdata.through_rate[1]+","+flowdata.through_rate[2]);
	}
	static String MechanicII_AI(int timID)//根据返回当前timID返回红绿灯策略
	{
		return WindhunterSB_Dynamic(timID);
	}
	static String WindhunterSB_Dynamic(int timID)
	{
		if(timID==0){
			initScoreRoad();
			//Windhunter_firstTimInit_static_beforeTraining();
			Windhunter_firstTimInit_static_FromRuleStr();
		}
		updateTrafficLight_static(timID);
		return flowdata.getTmpTrafficLight();
	}
	
	///T路口查找打分
	static int ScoreRoad[];///different road have different Score, high Score is good! like road on T path
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
	
	static int BreakingLevNum = 4;//放行策略总数(0,1,2,..,BreakingLevNum-1)
	static int isBreakingRoadLab_static[][]; //(RoadID,period)每个周期每条路的用哪种放行策略
	static boolean isBreakingRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		int cas = isBreakingRoadLab_static[roadID][TimID/(120/partNum)];
		if(cas == 0) return false;//不可犯规
		if(cas == 1)//验证一级策略,只要该路段车辆多于10即可放行
		{
			if(flowdata.roadFlow[roadID][TimID] >= 10) return true;
			return false;
		}
		if(cas == 2)//验证二级策略,只要该路段车辆多于16且前方路段少于32辆车
		{
			int nxtRoadID = flowdata.GotoID[roadID][2];
			if(nxtRoadID>0 && flowdata.hasRoadID[nxtRoadID] && flowdata.roadFlow[roadID][TimID] >= 16 && flowdata.roadFlow[nxtRoadID][TimID] <= 32) 
				return true;
			return false;
		}
		if(cas == 3)//验证三级策略，无论如何可以犯规
		{
			return true;
		}
		return false;
	}
	
	static boolean isExTRoadLab_static[][];  //(RoadID,PeriodID)每个周期每条路的isExTRoadLab是不一样的
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		return isExTRoadLab_static[roadID][TimID/(120/partNum)];
		//if(ScoreRoad[roadID]<=25) return true;else return false;
	}
	
	static void flowDischargeWithBreakingTrafficRule(int timID)//强通泄流，不管交通规则
	{
		//if(timID%120 >= 112) return;//后期不用
		int Yu = 10;
		for(int i=0;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])
			{
				if(isBreakingRoad(i,flowdata.lastTimID))//直线放行策略
				{
					flowdata.tmp_trafficlight[i][2]=1;
				}
				
				if(flowdata.GotoID[i][0]>0 && flowdata.hasRoadID[flowdata.GotoID[i][0]]==false)//左行一级放行
				{
					if(flowdata.tmp_trafficlight[i][0]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 2 &&flowdata.roadFlow[i][flowdata.lastTimID] <= 30)
					{
						flowdata.tmp_trafficlight[i][0]=1;
					}
				}
				if(flowdata.GotoID[i][0]>0 && ScoreRoad[flowdata.GotoID[i][0]]==48)//左行二级级放行
				{
					if(flowdata.tmp_trafficlight[i][0]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 2
							//&& flowdata.roadFlow[i][flowdata.lastTimID] <= 30
							&& flowdata.roadFlow[flowdata.GotoID[i][0]][flowdata.lastTimID] <=8)
					{
						flowdata.tmp_trafficlight[i][0]=1;
					}
				}
			}
		}
	}
	
	static int lightModel_static[][] = null;///14个120T周期分开训练，每个周期用一组策略灯
	static int lightRoadA_static[]   = null;//一个路口红绿灯起始roadA；实际上roadA是谁并不重要，重要的是roadA不能变
	static void Windhunter_firstTimInit_static_beforeTraining()
	{
		lightModel_static = new int[flowdata.tlNum][14*partNum];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++)
		{
			for(int j=0;j<(14*partNum);j++)
			{
				if(flowdata.lightLinkRoad[i][0]<=0) 
				{
					lightModel_static[i][j] = -1;
					continue;
				}
				lightModel_static[i][j] = 0;
			}
			lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		}
		
	}
	static void Windhunter_firstTimInit_static_FromRuleStr()
	{
		//init lightModel_static!
		lightModel_static = new int[flowdata.tlNum][14*partNum];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++) lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_lev2break_fasterTrain_AddisExTRoad_AddLeftBreaking.trim().split("@");
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_TrainFrom0908_partof2.trim().split("@");
		String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_TrainFromAllflow_partof2.trim().split("@");
		//String[] PeriodTable = Constants.NewRuleBreakTrafficRule_TrainFromAllflow_partof6.trim().split("@");
		//String[] PeriodTable = Constants.NewRuleBreakTrafficRule_TrainFromAllflow_partof6_0908.trim().split("@");
				
		int lablen = PeriodTable.length;
		//DebugPrint(""+lablen);
		for(String PeriodStr : PeriodTable)
		{
			String[] PStr = PeriodStr.trim().split(":");
			int PeriodID = flowdata.toInt(PStr[0]);
			String[] tlStr = PStr[1].trim().split(";");
			for(String ss : tlStr)
			{
				//DebugPrint(ss);
				String[] s = ss.trim().split(",");
				int tlID = flowdata.toInt(s[0]);
				int modelID = flowdata.toInt(s[1]);
				if(lablen<14*partNum)//old model, one period => one model
				{
					int pass = 14*partNum/lablen;
					for(int i=0;i<pass;i++) lightModel_static[tlID][PeriodID*pass+i]   = modelID;
				}
				else//new model, one period => two models
				{
					lightModel_static[tlID][PeriodID]   = modelID;
				}
				
			}
		}
		
		//init isExTRoad Lab: not change isExTRoadLab_static
		isExTRoadLab_static = new boolean[flowdata.roadNum][partNum*14];
		for(int i=0;i<flowdata.roadNum;i++) for(int j=0;j<partNum*14;j++) if(flowdata.hasRoadID[i]) isExTRoadLab_static[i][j] = (ScoreRoad[i]>25?false:true);
		
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_Rule_AddLeftBreaking;
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_TrainFrom_0908_partof2;
		String isExTRoadLab_Rule = Constants.isExTRoadLab_TrainFromAllflow_partof2;
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_TrainFromAllflow_partof6;
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_TrainFromAllflow_partof6_0908;
		if(isExTRoadLab_Rule.equals("")==false) 
		{
			String[] TRoadPeriodTable = isExTRoadLab_Rule.trim().split("@");//"hourID:roadIDx,trueOrfalse;roadIDy,trueOrfalse;...;@..."
			int lablen2 = TRoadPeriodTable.length;
			for(String PeriodStr : TRoadPeriodTable)
			{
				String[] PStr = PeriodStr.trim().split(":");
				int PeriodID = flowdata.toInt(PStr[0]);
				String[] roadStr = PStr[1].trim().split(";");
				for(String ss : roadStr)
				{
					//DebugPrint(ss);
					String[] s = ss.trim().split(",");
					int roadID = flowdata.toInt(s[0]);
					int trueorfalse = flowdata.toInt(s[1]);
					if(lablen2<14*partNum)
					{
						int pass = 14*partNum/lablen2;
						for(int i=0;i<pass;i++) isExTRoadLab_static[roadID][PeriodID*pass+i]   = (trueorfalse==1);
					}
					else
					{
						isExTRoadLab_static[roadID][PeriodID] = (trueorfalse==1);
					}
				}
			}
		}
		
		//init isBreakingRoadLab_static
		isBreakingRoadLab_static = new int[flowdata.roadNum][partNum*14];
		for(int i=0;i<flowdata.roadNum;i++) for(int j=0;j<partNum*14;j++)if(flowdata.hasRoadID[i])
		{
			 isBreakingRoadLab_static[i][j] = 0;
			 if(ScoreRoad[i]==48) isBreakingRoadLab_static[i][j] = 1;
			 if(ScoreRoad[i]==46) isBreakingRoadLab_static[i][j] = 2;
		}
		/////////////////////////////
		String isBreakingRoadLab_Rule = Constants.isBreakingRoadLab_Rule;
		
		if(isBreakingRoadLab_Rule.equals("")==false)
		{
			String[] BreakRuleTable = isBreakingRoadLab_Rule.trim().split("@");//"hourID:roadIDx,breakLev;roadIDy,breakLev;...;@..."
			int lablen3 = BreakRuleTable.length;
			for(String PeriodStr : BreakRuleTable)
			{
				String[] PStr = PeriodStr.trim().split(":");
				int PeriodID = flowdata.toInt(PStr[0]);
				String[] roadStr = PStr[1].trim().split(";");
				for(String ss : roadStr)
				{
					//DebugPrint(ss);
					String[] s = ss.trim().split(",");
					int roadID = flowdata.toInt(s[0]);
					int lev = flowdata.toInt(s[1]);
					if(lablen3<14*partNum)
					{
						int pass = 14*partNum/lablen3;
						for(int i=0;i<pass;i++) isBreakingRoadLab_static[roadID][PeriodID*pass+i]   = lev;
					}
					else
					{
						isBreakingRoadLab_static[roadID][PeriodID] = lev;
					}
				}
			}
		}
	}
	
	///高级静态路灯转换策略,第periodID大周期下（120T的周期），第timID个小周期
	static void updateTrafficLight_static(int timID) //periodID = timID/120
	{
		int periodID = timID/(120/partNum);
		//System.out.println("tim : "+timID+" :: "+periodID);
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{
			if(lightRoadA_static[tlID]>0)
			{
				int roadA = lightRoadA_static[tlID];
				int roadB = flowdata.getAntiRoad(flowdata.GotoID[roadA][2]);
				int roadC = flowdata.getAntiRoad(flowdata.GotoID[roadA][0]);
				int roadD = flowdata.getAntiRoad(flowdata.GotoID[roadA][1]);
				if(roadA>0) changeRoadLight_static(roadA,lightModel_static[tlID][periodID],timID,0);
				if(roadB>0) changeRoadLight_static(roadB,lightModel_static[tlID][periodID],timID,1);
				if(roadC>0) changeRoadLight_static(roadC,lightModel_static[tlID][periodID],timID,2);
				if(roadD>0) changeRoadLight_static(roadD,lightModel_static[tlID][periodID],timID,3);
			}
		}
		///set -1 to null lights!
		for(int i=0;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])
			{
				for(int j=0;j<3;j++) if(flowdata.GotoID[i][j]<=0) flowdata.tmp_trafficlight[i][j] = -1;
			}
		}
		///cheat for high score
		flowDischargeWithBreakingTrafficRule(timID);
	}
	
	static void changeRoadLight_static(int roadID,int modelID,int timID,int ABCD)//ABCD+'A' => roadX, timID
	{
		int PeriodT = lightmodel.trafficLightTablePeriod[modelID];
		int passTimID = timID%(120/partNum);//因为，现在一个子块未必能被整除 
		
		///right light independent:
		flowdata.tmp_trafficlight[roadID][1] = 1;
		if(isExTRoad(flowdata.GotoID[roadID][1],timID) && timID%5!=0) flowdata.tmp_trafficlight[roadID][1] = 0;
		
		///left light :
		flowdata.tmp_trafficlight[roadID][0] = lightmodel.getLightLab(modelID, passTimID%PeriodT, ABCD*2 + 0, false);
		if(isExTRoad(flowdata.GotoID[roadID][0],timID) && 
				lightmodel.trafficLightTablefirstGreen[modelID][ABCD*2 + 0]!=passTimID%PeriodT){
			flowdata.tmp_trafficlight[roadID][0] = 0;///只亮第一个绿
		}
		
		///straight light:
		flowdata.tmp_trafficlight[roadID][2] = lightmodel.getLightLab(modelID, passTimID%PeriodT, ABCD*2 + 1, false);
	}
	
	///学习静态策略，每120T一个策略,策略Str："periodID:tlID,modelID0;...;@"
	static int FastRunning120T_static(int periodID)
	///快速循环120次并计算当前红路灯策略总得分。注意需要对多个文件训练
	{
		double SumPenalty = 0;
		for(int dataid = 0;dataid < flowdata.BigDataNum ;dataid++)
		{
			//每组数据需要初始化一次lastTimID
			flowdata.lastTimID = periodID*120-1;
			flowdata.SumPenalty = 0;///初始化,不初始化起始也行~~，呵呵
			for(int i=0;i<120;i++)
			{
				//注意每组训练数据都要读入一次；
				flowdata.updataMultiData(dataid , periodID*120 + i);
				///注意不需要运行Windhunter_firstTimInit()，因为canAdvance里已经准备好了lightT，lightStartID
				updateTrafficLight_static(periodID*120 + i);
				flowdata.updataTrafficLightToHistory();
				double tmpPenalty = flowdata.updataPenalty(periodID*120 + i);
				SumPenalty += tmpPenalty;
				///DebugPrint(""+i + " tPenalty : "+tmpPenalty + " ; All_Penalty : "+flowdata.SumPenalty);		
			}
		}
		//DebugPrint(":"+SumPenalty);		
		return (int)SumPenalty;
	}
	
	//一个周期前后partNum段不一样
	static int[] canAdvance_static_faster(int periodID,int partID)//partID = 0 or 1 or .....
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID+" part: "+partID);
		int[][] trafficlightBestModelIDandBestAdvanceValue = new int[flowdata.tlNum][2];//((X,0):bestModel,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.tlNum];
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{	
			if(flowdata.lightLinkRoad[tlID][0]<=0) continue;
			int oldModelID = lightModel_static[tlID][periodID*partNum + partID];
			int tmpBeastModelID = 0 , tmpBestAdvanceValue = 0;
			trafficlightBestModelIDandBestAdvanceValue[tlID][1] = 0;
			for(int modelid = 0;modelid < lightmodel.ModelNum;modelid++)
			{
				if(modelid==oldModelID) continue;
				lightModel_static[tlID][periodID*partNum + partID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>tmpBestAdvanceValue)
				{
					tmpBestAdvanceValue = LastPenalty-tmpPenalty;
					tmpBeastModelID = modelid;
				}
			}
			lightModel_static[tlID][periodID*partNum + partID] = oldModelID;
			trafficlightBestModelIDandBestAdvanceValue[tlID][0] = tmpBeastModelID;
			trafficlightBestModelIDandBestAdvanceValue[tlID][1] = tmpBestAdvanceValue;
			if(tmpBestAdvanceValue>0) SortArray[tlID] = tmpBestAdvanceValue*100 + tlID;
		}
		
		Arrays.sort(SortArray);
		ArrayList <Integer> chooseChange = new ArrayList<Integer>();
		for(int i = flowdata.tlNum - 1 ; i>=0;i--)
		{
			int tlID = (int)(SortArray[i]%100);
			int advance = (int)(SortArray[i]/100);
			
			if(advance>0)
			{
				int modelid = lightModel_static[tlID][periodID*partNum + partID];
				int tmpPenalty = FastRunning120T_static(periodID);//改之前
				lightModel_static[tlID][periodID*partNum + partID] = trafficlightBestModelIDandBestAdvanceValue[tlID][0];
				int newPenalty = FastRunning120T_static(periodID);//改后
				if(tmpPenalty<=newPenalty)//不提升不修改
				{
					lightModel_static[tlID][periodID*partNum + partID] = modelid ;//还原
				}
				else
				{
					chooseChange.add(tlID);
					chooseChange.add(trafficlightBestModelIDandBestAdvanceValue[tlID][0]);
				}
			}
		}
		int sumAdvance = LastPenalty - FastRunning120T_static(periodID);
		chooseChange.add(sumAdvance);
		
		if(sumAdvance>0)
		{
			int[] ret = new int[chooseChange.size()];
			for(int i=0;i<ret.length;i++)
			{
				ret[i] = chooseChange.get(i).intValue();
			}
			return ret;
		}
		return null;
	}
	
	static int[] canAdvance_static_faster_and_learnisExTRoadLab(int periodID,int partID)
	{
		//DebugPrint(""+isExTRoadLab_static[129][0]);
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("it's T-road-lab training last_Penalty: " + LastPenalty +"  periodID: "+periodID+" partID: "+partID);
		
		int[][] roadLabBestModelIDandBestAdvanceValue = new int[flowdata.roadNum][2];//((X,0):0 or 1,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.roadNum];
		for(int roadID=1;roadID<flowdata.roadNum;roadID++)
		{
			int oldModelID = (isExTRoadLab_static[roadID][periodID*partNum + partID]?1:0);
			roadLabBestModelIDandBestAdvanceValue[roadID][1] = 0;
			isExTRoadLab_static[roadID][periodID*partNum + partID] = ( (oldModelID^1)==0?false:true );
			int tmpPenalty = FastRunning120T_static(periodID);
			if(tmpPenalty<LastPenalty)
			{
				roadLabBestModelIDandBestAdvanceValue[roadID][1] = LastPenalty - tmpPenalty;
				roadLabBestModelIDandBestAdvanceValue[roadID][0] = (isExTRoadLab_static[roadID][periodID*partNum + partID]?1:0);
				SortArray[roadID] = roadLabBestModelIDandBestAdvanceValue[roadID][1]*400 + roadID;
			}
			isExTRoadLab_static[roadID][periodID*partNum + partID] = ( oldModelID==0?false:true );
		}
		//更新T路标签
		Arrays.sort(SortArray);
		ArrayList <Integer> chooseChange = new ArrayList<Integer>();
		for(int i = flowdata.roadNum - 1 ; i>=0;i--)
		{
			int roadID = (int)(SortArray[i]%400);
			int advance = (int)(SortArray[i]/400);
			
			if(advance>0)
			{
				boolean modelid = isExTRoadLab_static[roadID][periodID*partNum + partID];
				int tmpPenalty = FastRunning120T_static(periodID);//改之前
				isExTRoadLab_static[roadID][periodID*partNum + partID] = (roadLabBestModelIDandBestAdvanceValue[roadID][0]==1?true:false);
				int newPenalty = FastRunning120T_static(periodID);//改后
				if(tmpPenalty<=newPenalty)//不提升不修改
				{
					isExTRoadLab_static[roadID][periodID*partNum + partID] = modelid ;//还原
				}
				else
				{
					chooseChange.add(roadID);
					chooseChange.add(roadLabBestModelIDandBestAdvanceValue[roadID][0]);
				}
			}
		}		
		int sumAdvance = LastPenalty - FastRunning120T_static(periodID);
		chooseChange.add(sumAdvance);
		if(sumAdvance>0)
		{
			int[] ret = new int[chooseChange.size()];
			for(int i=0;i<ret.length;i++)
			{
				ret[i] = chooseChange.get(i).intValue();
			}
			return ret;
		}
		return null;
	}
	
	//一个周期前后partNum段不一样
	static int[] canAdvance_static_faster_and_learnisBreakRoad(int periodID,int partID)//partID = 0 or 1 or .....
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("it's Breaking Rule last Penalty achieve: " + LastPenalty +"  periodID: "+periodID+" part: "+partID);
		int[][] RoadBreakBestModelIDandBestAdvanceValue = new int[flowdata.roadNum][2];//((X,0):bestModel,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.roadNum];
		for(int roadID=1;roadID<flowdata.roadNum;roadID++)
		{	
			if(flowdata.hasRoadID[roadID]==false) continue;
			int oldModelID = isBreakingRoadLab_static[roadID][periodID*partNum + partID];
			int tmpBeastModelID = 0 , tmpBestAdvanceValue = 0;
			RoadBreakBestModelIDandBestAdvanceValue[roadID][1] = 0;
			for(int modelid = 0;modelid < BreakingLevNum;modelid++)
			{
				if(modelid==oldModelID) continue;
				isBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>tmpBestAdvanceValue)
				{
					tmpBestAdvanceValue = LastPenalty-tmpPenalty;
					tmpBeastModelID = modelid;
				}
			}
			isBreakingRoadLab_static[roadID][periodID*partNum + partID] = oldModelID;
			RoadBreakBestModelIDandBestAdvanceValue[roadID][0] = tmpBeastModelID;
			RoadBreakBestModelIDandBestAdvanceValue[roadID][1] = tmpBestAdvanceValue;
			if(tmpBestAdvanceValue>0) SortArray[roadID] = tmpBestAdvanceValue*400 + roadID;
		}
		
		Arrays.sort(SortArray);
		ArrayList <Integer> chooseChange = new ArrayList<Integer>();
		for(int i = flowdata.roadNum - 1 ; i>=0;i--)
		{
			int roadID  = (int)(SortArray[i]%400);
			int advance = (int)(SortArray[i]/400);
			
			if(advance>0)
			{
				int modelid = isBreakingRoadLab_static[roadID][periodID*partNum + partID];
				int tmpPenalty = FastRunning120T_static(periodID);//改之前
				isBreakingRoadLab_static[roadID][periodID*partNum + partID] = RoadBreakBestModelIDandBestAdvanceValue[roadID][0];
				int newPenalty = FastRunning120T_static(periodID);//改后
				if(tmpPenalty<=newPenalty)//不提升不修改
				{
					isBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid ;//还原
				}
				else
				{
					chooseChange.add(roadID);
					chooseChange.add(RoadBreakBestModelIDandBestAdvanceValue[roadID][0]);
				}
			}
		}
		int sumAdvance = LastPenalty - FastRunning120T_static(periodID);
		chooseChange.add(sumAdvance);
		
		if(sumAdvance>0)
		{
			int[] ret = new int[chooseChange.size()];
			for(int i=0;i<ret.length;i++)
			{
				ret[i] = chooseChange.get(i).intValue();
			}
			return ret;
		}
		return null;
	}
	
	static int[] canAdvance_static(int periodID)
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID);
		int BestAtID = 0,BeastModelID = 0,BestAdvanceValue = 0;///value最大提高分数，atID最优时所在路口，fromID标记东西or南北
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{	
			if(flowdata.lightLinkRoad[tlID][0]<=0) continue;
			int oldModelID = lightModel_static[tlID][periodID];
			for(int modelid = 0;modelid < lightmodel.ModelNum;modelid++)
			{
				if(modelid==oldModelID) continue;
				lightModel_static[tlID][periodID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>BestAdvanceValue)
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BeastModelID = modelid;
				}
			}
			lightModel_static[tlID][periodID] = oldModelID;
		}
		if(BestAdvanceValue>0)
		{
			return new int[]{BestAtID,BeastModelID,BestAdvanceValue};
		}
		return null;
	}
	static int trytoAdvance(int periodID,int RunningTim)
	{
		int ret = 0;
		int[] AdvanceInfo = null;
		for(int partID=0;partID<partNum;partID++)
		{
			/*
			if((AdvanceInfo=canAdvance_static_faster(periodID,partID))!=null)
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint(""+RunningTim+" : BestTL:"+AdvanceInfo[i*2+0]+",BestModel:"+ AdvanceInfo[i*2+1]
							+",startRoadA:"+flowdata.RoadString(lightRoadA_static[AdvanceInfo[i*2+0]]) +";");
					DebugPrint("--->this Model is : "+lightmodel.getModelStr( AdvanceInfo[i*2+1]));
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			if((AdvanceInfo = canAdvance_static_faster_and_learnisExTRoadLab(periodID,partID) ) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint(""+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			*/
			if((AdvanceInfo = canAdvance_static_faster_and_learnisBreakRoad(periodID,partID)) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint("BreakRule: "+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+", "+flowdata.RoadString(AdvanceInfo[i*2+0])+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
		}
		
		return ret;
	}
	///学习静态策略，每120T一个策略,策略Str："periodID:tlID,modelID0;...;$"
	static String[] windhunterLearningII_static(String[] filetxtset,int periodID,boolean usefaster)//添加是否选用faster方法更新训练
	{
		///unfinished ! should create a new model first!
		flowdata = new FlowData();
		flowdata.initJudgeFromMultiTxts(filetxtset);
		lightmodel = new MechanicII_Model();
		lightmodel.initTableFrom();
		initScoreRoad();
		//Windhunter_firstTimInit_static_beforeTraining();
		Windhunter_firstTimInit_static_FromRuleStr();
		
		int RunningTim = 0;
		DebugPrint("Leaarning Started!");
		//Windhunter_firstTimInit_Smarter();
		int fullAdvance = 0;
		
		int[] AdvanceInfo = null;//new int[]{1,1,1};
		int[] AdvanceInfo_Lab = null;
		if(usefaster==false)//一次更新一个traffic light
		{
			while( (AdvanceInfo = canAdvance_static(periodID)) != null )
			{
				DebugPrint(""+RunningTim+" : BestTL:"+AdvanceInfo[0]+",BestModel:"+ AdvanceInfo[1]
						+",startRoadA:"+flowdata.RoadString(lightRoadA_static[AdvanceInfo[0]]) +",advance:"+AdvanceInfo[2]+";");
				DebugPrint("the Model is : "+lightmodel.getModelStr( AdvanceInfo[1]));
				fullAdvance += AdvanceInfo[2];
				lightModel_static[AdvanceInfo[0]][periodID] = AdvanceInfo[1];
				if(RunningTim>=100) 
				{
					DebugPrint("Out Of Max Tim:" + RunningTim);
					break;
				}
				RunningTim++;
			}
		}
		else//一次更新多个traffic lights
		{
			int tmpadvance = 0;
			while( (tmpadvance=trytoAdvance(periodID,RunningTim))!=0 )
			{
				fullAdvance+=tmpadvance;
				if(RunningTim>=100) 
				{
					DebugPrint("Out Of Max Tim:" + RunningTim);
					break;
				}
				RunningTim++;
			}
		}
		
		DebugPrint("Learning End! and Advanced : " + fullAdvance);
		String[] retStrs = new String[3];
		///OutPut Traffic Light Rule Table:
		StringBuilder ret1 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret1.append(""+(periodID*partNum+partID)+":");
			for(int tlID = 1;tlID < flowdata.tlNum;tlID++)
			{
				if(flowdata.lightLinkRoad[tlID][0]>0)
				{
					ret1.append(""+tlID+","+lightModel_static[tlID][periodID*partNum+partID]+";");
				}
			}
			ret1.append("@");
		}
		DebugPrint(ret1.toString());
		retStrs[0] = ret1.toString();
		
		//Output road is ExTRoad Lab :
		StringBuilder ret2 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret2.append(""+(periodID*partNum+partID)+":");
			for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
			{
				ret2.append(""+roadID+","+(isExTRoadLab_static[roadID][periodID*partNum+partID]?"1":"0")+";");
			}
			ret2.append("@");
		}
		DebugPrint(ret2.toString());
		retStrs[1] = ret2.toString();
		
		//Output road is BreakRoad Lab :
		StringBuilder ret3 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret3.append(""+(periodID*partNum+partID)+":");
			for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
			{
				ret3.append(""+roadID+","+(isBreakingRoadLab_static[roadID][periodID*partNum+partID])+";");
			}
			ret3.append("@");
		}
		DebugPrint(ret3.toString());
		retStrs[2] = ret3.toString();
		return retStrs;
	}
	 
	
	static void DoSomethingInit()
	{
		lightmodel = new MechanicII_Model();
		lightmodel.BuildModels();
	}
	static void runTrainingModel(String PeriodIDStr,String OutPutFile,String fasterLab)
	{
		try{
			FileOutputStream outfile1 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_tlModel_"+PeriodIDStr+".txt");
			FileOutputStream outfile2 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_RoadLab_"+PeriodIDStr+".txt");
			String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			//String alltxt = "./data/flow0908_guess.txt;";
			String[] txts = alltxt.trim().split(";");
			debuglab = true;///for print 
			flowdata = new FlowData();
			int periodID = flowdata.toInt(PeriodIDStr);
			String[] lastStr = null;
			
			if(fasterLab.equals("usefaster")==false) lastStr = windhunterLearningII_static(txts,periodID,false);
			else  lastStr = windhunterLearningII_static(txts,periodID,true);
			//lastStr += windhunterLearningII_static(txts,periodID,true);
			//每次输出一次全集，因为训练实在太慢
			outfile1.write((lastStr[0]).getBytes());
			outfile2.write((lastStr[1]).getBytes());
			return;
		}
		catch (Exception e) {   
            e.printStackTrace();   
        } 
	}
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		
		//if(true) {DoSomethingInit();return;}//生成lightmodel需要的东西
		//if(true) {if(args.length==2) runTrainingModel(args[0],args[1],"false");else runTrainingModel(args[0],args[1],args[2]);return;}//在外部多次运行
		
		///if Learning?
		//learninglab = true;
		if(learninglab)
		{
			debuglab = true;
			String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			String[] txts = alltxt.trim().split(";");
			//debuglab = true;///for print 
			String lastStr1 = "";
			String lastStr2 = "";
			String lastStr3 = "";
			for(int i=0;i<14;i++)
			{
				//String[] ret = windhunterLearningII_static(txts,i,false);
				String[] ret = windhunterLearningII_static(txts,i,true);
				lastStr1 += ret[0];
				lastStr2 += ret[1];
				lastStr3 += ret[2];
				//每次输出一次全集，因为训练实在太慢
				/*
				StringBuilder buildStr = new StringBuilder();
				for(int hour=0;hour<14;hour++) 
				{
					buildStr.append(""+hour+":");
					for(int tlID = 1;tlID < flowdata.tlNum;tlID++)
					{
						if(flowdata.lightLinkRoad[tlID][0]>0)
						{
							buildStr.append(""+tlID+","+lightModel_static[tlID][hour]+";");
						}
					}
					buildStr.append("@");
				}
				DebugPrint("tmpmem:\n"+buildStr.toString());
				*/
			}
			DebugPrint("NewRule:");
			DebugPrint(lastStr1);
			DebugPrint(lastStr2);
			DebugPrint(lastStr3);
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
		
		lightmodel = new MechanicII_Model();
		lightmodel.initTableFrom();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String flows_str = br.readLine();	
		while(!"end".equalsIgnoreCase(flows_str)){
			///push data to flowdata from readString
			flowdata.updataFromStringByMoniqi(flows_str,count);
			///Do AI function
			String AI_trafficLightTable = MechanicII_AI(count);
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

