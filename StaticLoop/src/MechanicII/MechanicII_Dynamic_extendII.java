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

public class MechanicII_Dynamic_extendII {
	static int partNum = 2;//120T��Ϊ����
	//static FlowData flowdata = null;
	static FlowdataII flowdata = null;
	static MechanicII_Model lightmodel = null;/// haven't init
	static boolean debuglab = false;
	static boolean learninglab = false;
	static void DebugPrint(String context){if(debuglab) System.out.println(context);}
	static void Debugger(String txtfile)throws NumberFormatException, IOException
	///����debug�ģ�����txt�������ݣ���flowdataģ�⣬��ģ���������ܣ�
	{
		int count = 0;
		//flowdata = new FlowData();
		flowdata = new FlowdataII();
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
	static String MechanicII_AI(int timID)//���ݷ��ص�ǰtimID���غ��̵Ʋ���
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
	
	///T·�ڲ��Ҵ��
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
	
	static int BreakingLevNum = 4;//���в�������(0,1,2,..,BreakingLevNum-1)
	static int isBreakingRoadLab_static[][]; //(RoadID,period)ÿ������ÿ��·�������ַ��в���
	static int isLeftBreakingRoadLab_static[][]; //(RoadID,period)ÿ������ÿ��·�������ַ��в���,���з���
	static boolean isBreakingRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		int cas = isBreakingRoadLab_static[roadID][TimID/(120/partNum)];
		if(cas == 0) return false;//���ɷ���
		if(cas == 1)//��֤һ������,ֻҪ��·�γ�������10���ɷ���
		{
			if(flowdata.roadFlow[roadID][TimID] >= 10) return true;
			return false;
		}
		if(cas == 2)//��֤��������,ֻҪ��·�γ�������16��ǰ��·������32����
		{
			int nxtRoadID = flowdata.GotoID[roadID][2];
			if(nxtRoadID>0 && flowdata.hasRoadID[nxtRoadID] && flowdata.roadFlow[roadID][TimID] >= 16 && flowdata.roadFlow[nxtRoadID][TimID] <= 32) 
				return true;
			return false;
		}
		if(cas == 3)//��֤�������ԣ�������ο��Է���
		{
			return true;
		}
		return false;
	}
	static boolean isLeftBreakingRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		int cas = isLeftBreakingRoadLab_static[roadID][TimID/(120/partNum)];
		if(cas == 0) return false;//���ɷ���
		if(cas == 1)//��֤һ������,ֻҪ��·�γ�������10���ɷ���
		{
			if(flowdata.GotoID[roadID][0]>0 && flowdata.hasRoadID[flowdata.GotoID[roadID][0]]==false)//����һ������
			{
				if( flowdata.roadFlow[roadID][flowdata.lastTimID] >= 2 &&flowdata.roadFlow[roadID][flowdata.lastTimID] <= 50)
				{
					return true;
				}
			}
			return false;
		}
		if(cas == 2)//��֤��������,ֻҪ��·�γ�������16��ǰ��·������32����
		{
			int nxtRoadID = flowdata.GotoID[roadID][0];
			if(nxtRoadID>0 && ScoreRoad[nxtRoadID]==48)//���ж���������
			{
				if(flowdata.roadFlow[roadID][flowdata.lastTimID] >= 4&& flowdata.roadFlow[nxtRoadID][flowdata.lastTimID] <=12)
				{
					return true;
				}
			}
			return false;
		}
		if(cas == 3)//��֤�������ԣ�������ο��Է���
		{
			return true;
		}
		return false;
	}
	
	static boolean isExTRoadLab_static[][];  //(RoadID,PeriodID)ÿ������ÿ��·��isExTRoadLab�ǲ�һ����
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		return isExTRoadLab_static[roadID][TimID/(120/partNum)];
		//if(ScoreRoad[roadID]<=25) return true;else return false;
	}
	
	static void flowDischargeWithBreakingTrafficRule(int timID)//ǿͨй�������ܽ�ͨ����
	{
		//if(timID%120 >= 112) return;//���ڲ���
		int Yu = 10;
		for(int i=0;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])
			{
				if(isBreakingRoad(i,flowdata.lastTimID))//ֱ�߷��в���
				{
					flowdata.tmp_trafficlight[i][2]=1;
				}
				if(isLeftBreakingRoad(i,flowdata.lastTimID))//���з��в���
				{
					flowdata.tmp_trafficlight[i][0]=1;
				}
			}
		}
	}
	
	static int lightModel_static[][] = null;///14��120T���ڷֿ�ѵ����ÿ��������һ����Ե�
	static int lightRoadA_static[]   = null;//һ��·�ں��̵���ʼroadA��ʵ����roadA��˭������Ҫ����Ҫ����roadA���ܱ�
	//new: Right Period���������תһ���������ʶ��ٴεģ���Ϊ��ת���������������isExTRoad�Ѿ����ٺ��ʣ���ΪT·������ת�ɴ�10��8������
	static int rightlightModel_static[][] = null;///14��120T���ڷֿ�ѵ����ÿ��������һ����ת����,value = {1,2,3,4,5}
	static int leftlightModel_static[][] = null;///14��120T���ڷֿ�ѵ����ÿ��������һ����ת����,value = {1,2,3,4,5}
	static void Windhunter_firstTimInit_static_FromRuleStr()
	{
		//init lightModel_static!
		lightModel_static = new int[flowdata.tlNum][14*partNum];
		lightRoadA_static = new int[flowdata.tlNum];
		rightlightModel_static = new int[flowdata.roadNum][14*partNum];
		leftlightModel_static  = new int[flowdata.roadNum][14*partNum];
		for(int i=1;i<flowdata.tlNum;i++) lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		
		//***********************
		//***********************
		//***********************
		//String[] PeriodTable = Constants.NewRuleTrafficRule_part2_input7.trim().split("@");
		String[] PeriodTable = Constants.NewRuleTrafficRule_part2_input7_r2.trim().split("@");
		//String[] PeriodTable = Constants.NewRuleTrafficRule_part2_inputAllmean2_r2.trim().split("@");
				
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
		

		//String isExTRoadLab_Rule = Constants.isExTRoadLab_part2_input7;
		String isExTRoadLab_Rule = Constants.isExTRoadLab_part2_input7_r2;
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_part2_inputAllmean2_r2;
		
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
		isLeftBreakingRoadLab_static = new int[flowdata.roadNum][partNum*14];
		for(int i=0;i<flowdata.roadNum;i++) for(int j=0;j<partNum*14;j++)if(flowdata.hasRoadID[i])
		{
			 isBreakingRoadLab_static[i][j] = 0;
			 if(ScoreRoad[i]==48) isBreakingRoadLab_static[i][j] = 1;
			 if(ScoreRoad[i]==46) isBreakingRoadLab_static[i][j] = 2;
		}
		for(int i=0;i<flowdata.roadNum;i++) for(int j=0;j<partNum*14;j++)if(flowdata.hasRoadID[i])
		{
			 isLeftBreakingRoadLab_static[i][j] = 0;
			 int nxtRoad = flowdata.GotoID[i][0];
			 if(nxtRoad<=0) continue;
			 if(ScoreRoad[nxtRoad]==50) isLeftBreakingRoadLab_static[i][j] = 1;
			 if(ScoreRoad[nxtRoad]==48) isLeftBreakingRoadLab_static[i][j] = 2;
		}
		/////////////////////////////
		//String isBreakingRoadLab_Rule = Constants.isBreakingRoadLab_part2_input7;
		String isBreakingRoadLab_Rule = Constants.isBreakingRoadLab_part2_input7_r2;
		//String isBreakingRoadLab_Rule = Constants.isBreakingRoadLab_part2_inputAllmean2_r2;
		
		if(isBreakingRoadLab_Rule.equals("")==false)
		{
			String[] BreakRuleTable = isBreakingRoadLab_Rule.trim().split("@");//"hourID:roadIDx,breakLev;roadIDy,breakLev;...;@..."
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
					isBreakingRoadLab_static[roadID][PeriodID] = lev;
				}
			}
		}
		
		////////////Left Break
		String isLeftBreakingRoadLab_Rule = Constants.isLeftBreakingRoadLab_part2_input7_r2;
		//String isBreakingRoadLab_Rule = Constants.isBreakingRoadLab_part2_inputAllmean2_r2;
		
		if(isLeftBreakingRoadLab_Rule.equals("")==false)
		{
			String[] BreakRuleTable = isLeftBreakingRoadLab_Rule.trim().split("@");//"hourID:roadIDx,breakLev;roadIDy,breakLev;...;@..."
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
					isLeftBreakingRoadLab_static[roadID][PeriodID] = lev;
				}
			}
		}
		
		//////////////////////////////////
		//--------rightLight Model Table:
		String rightLightTable = Constants.rightLightTable_r2;
		
		//��һ�γ�ʼ����ת,����isExTRoad�����
		for(int perT = 0;perT < 14*partNum;perT++) for(int rid=0;rid<flowdata.roadNum;rid++) if(flowdata.hasRoadID[rid]){
			 if(flowdata.GotoID[rid][1]>0 && isExTRoadLab_static[flowdata.GotoID[rid][1]][perT]) rightlightModel_static[rid][perT] = 1;else rightlightModel_static[rid][perT] = 5;
		}
		//�ǵ�һ��
		if(rightLightTable.equals("")==false)
		{
			String[] RightlightTable = rightLightTable.trim().split("@");//"hourID:roadIDx,breakLev;roadIDy,breakLev;...;@..."
			int lablen3 = RightlightTable.length;
			for(String PeriodStr : RightlightTable)
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
					rightlightModel_static[roadID][PeriodID] = lev;
				}
			}
		}
		
		///////////////////////////////////
		//--------leftLight Model Table:
		String leftLightTable = Constants.leftLightTable_r2;
		
		//��һ�γ�ʼ����ת,����isExTRoad�����
		for(int perT = 0;perT < 14*partNum;perT++) for(int rid=0;rid<flowdata.roadNum;rid++) if(flowdata.hasRoadID[rid]){
			 if(flowdata.GotoID[rid][0]>0 && isExTRoadLab_static[flowdata.GotoID[rid][0]][perT]) leftlightModel_static[rid][perT] = 1;else leftlightModel_static[rid][perT] = 5;
		}
		//�ǵ�һ��
		if(leftLightTable.equals("")==false)
		{
			String[] LeftlightTable = leftLightTable.trim().split("@");//"hourID:roadIDx,breakLev;roadIDy,breakLev;...;@..."
			int lablen3 = LeftlightTable.length;
			for(String PeriodStr : LeftlightTable)
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
					leftlightModel_static[roadID][PeriodID] = lev;
				}
			}
		}
		
	}
	
	///�߼���̬·��ת������,��periodID�������£�120T�����ڣ�����timID��С����
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
		int passTimID = timID%(120/partNum);//��Ϊ������һ���ӿ�δ���ܱ����� 
		
		///right light independent:
		flowdata.tmp_trafficlight[roadID][1] = 1;
		//if(isExTRoad(flowdata.GotoID[roadID][1],timID) && timID%5!=0) flowdata.tmp_trafficlight[roadID][1] = 0;
		if(passTimID%5 >= rightlightModel_static[roadID][timID/(120/partNum)]) flowdata.tmp_trafficlight[roadID][1] = 0;
		
		///left light :
		flowdata.tmp_trafficlight[roadID][0] = lightmodel.getLightLab(modelID, passTimID%PeriodT, ABCD*2 + 0, false);
		//if(isExTRoad(flowdata.GotoID[roadID][0],timID) && lightmodel.trafficLightTablefirstGreen[modelID][ABCD*2 + 0]!=passTimID%PeriodT)
		//	flowdata.tmp_trafficlight[roadID][0] = 0;///ֻ����һ����
		
		if(flowdata.tmp_trafficlight[roadID][0]>0 && 
				lightmodel.trafficLightTableGreenCnt[modelID][passTimID%PeriodT][ABCD*2]>leftlightModel_static[roadID][timID/(120/partNum)])
			flowdata.tmp_trafficlight[roadID][0] = 0;///ֻ��ǰX��
		
		
		///straight light:
		flowdata.tmp_trafficlight[roadID][2] = lightmodel.getLightLab(modelID, passTimID%PeriodT, ABCD*2 + 1, false);
	}
	
	///ѧϰ��̬���ԣ�ÿ120Tһ������,����Str��"periodID:tlID,modelID0;...;@"
	static int FastRunning120T_static(int periodID)
	///����ѭ��120�β����㵱ǰ��·�Ʋ����ܵ÷֡�ע����Ҫ�Զ���ļ�ѵ��
	{
		double SumPenalty = 0;
		for(int dataid = 0;dataid < flowdata.BigDataNum ;dataid++)
		{
			//ÿ��������Ҫ��ʼ��һ��lastTimID
			flowdata.lastTimID = periodID*120-1;
			flowdata.SumPenalty = 0;///��ʼ��,����ʼ����ʼҲ��~~���Ǻ�
			for(int i=0;i<120;i++)
			{
				//ע��ÿ��ѵ�����ݶ�Ҫ����һ�Σ�
				flowdata.updataMultiData(dataid , periodID*120 + i);
				///ע�ⲻ��Ҫ����Windhunter_firstTimInit()����ΪcanAdvance���Ѿ�׼������lightT��lightStartID
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
	
	//һ������ǰ��partNum�β�һ��
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
				int tmpPenalty = FastRunning120T_static(periodID);//��֮ǰ
				lightModel_static[tlID][periodID*partNum + partID] = trafficlightBestModelIDandBestAdvanceValue[tlID][0];
				int newPenalty = FastRunning120T_static(periodID);//�ĺ�
				if(tmpPenalty<=newPenalty)//���������޸�
				{
					lightModel_static[tlID][periodID*partNum + partID] = modelid ;//��ԭ
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
	
	
	//һ������ǰ��partNum�β�һ��
	static int[] canAdvance_static_faster_and_learnisBreakRoad(int periodID,int partID,boolean isLeft)//partID = 0 or 1 or .....
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("it's Breaking Rule last Penalty achieve: " + LastPenalty +"  periodID: "+periodID+" part: "+partID);
		int[][] RoadBreakBestModelIDandBestAdvanceValue = new int[flowdata.roadNum][2];//((X,0):bestModel,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.roadNum];
		for(int roadID=1;roadID<flowdata.roadNum;roadID++)
		{	
			if(flowdata.hasRoadID[roadID]==false) continue;
			int oldModelID = isLeft?isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID]:isBreakingRoadLab_static[roadID][periodID*partNum + partID];
			int tmpBeastModelID = 0 , tmpBestAdvanceValue = 0;
			RoadBreakBestModelIDandBestAdvanceValue[roadID][1] = 0;
			for(int modelid = 0;modelid < BreakingLevNum;modelid++)
			{
				if(modelid==oldModelID) continue;
				if(isLeft) isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid;
				else isBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>tmpBestAdvanceValue)
				{
					tmpBestAdvanceValue = LastPenalty-tmpPenalty;
					tmpBeastModelID = modelid;
				}
			}
			if(isLeft) isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID] = oldModelID;
			else isBreakingRoadLab_static[roadID][periodID*partNum + partID] = oldModelID;
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
				int modelid = isLeft?isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID]:isBreakingRoadLab_static[roadID][periodID*partNum + partID];
				int tmpPenalty = FastRunning120T_static(periodID);//��֮ǰ
				if(isLeft) isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID] = RoadBreakBestModelIDandBestAdvanceValue[roadID][0];
				else isBreakingRoadLab_static[roadID][periodID*partNum + partID] = RoadBreakBestModelIDandBestAdvanceValue[roadID][0];
				int newPenalty = FastRunning120T_static(periodID);//�ĺ�
				if(tmpPenalty<=newPenalty)//���������޸�
				{
					if(isLeft) isLeftBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid;
					else isBreakingRoadLab_static[roadID][periodID*partNum + partID] = modelid;//��ԭ
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
	
	//һ������ǰ��partNum�β�һ��
	static int[] canAdvance_static_faster_and_learn_LeftorRight_lightTable(int periodID,int partID,boolean isleft)//partID = 0 or 1 or .....
	{
		int LastPenalty = FastRunning120T_static(periodID);
		if(isleft==false) DebugPrint("it's RightLightTable last Penalty achieve: " + LastPenalty +"  periodID: "+periodID+" part: "+partID);
		else  DebugPrint("it's LeftLightTable last Penalty achieve: " + LastPenalty +"  periodID: "+periodID+" part: "+partID);
		int[][] RoadRightBestModelIDandBestAdvanceValue = new int[flowdata.roadNum][2];//((X,0):bestModel,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.roadNum];
		for(int roadID=1;roadID<flowdata.roadNum;roadID++)
		{	
			if(flowdata.hasRoadID[roadID]==false) continue;
			int oldModelID = isleft?leftlightModel_static[roadID][periodID*partNum + partID]:rightlightModel_static[roadID][periodID*partNum + partID];
			int tmpBeastModelID = 0 , tmpBestAdvanceValue = 0;
			RoadRightBestModelIDandBestAdvanceValue[roadID][1] = 0;
			for(int modelid = 1;modelid <= 5;modelid++)
			{
				if(modelid==oldModelID) continue;
				if(isleft) leftlightModel_static[roadID][periodID*partNum + partID] = modelid;
				else rightlightModel_static[roadID][periodID*partNum + partID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>tmpBestAdvanceValue)
				{
					tmpBestAdvanceValue = LastPenalty-tmpPenalty;
					tmpBeastModelID = modelid;
				}
			}
			if(isleft) leftlightModel_static[roadID][periodID*partNum + partID] = oldModelID;
			else rightlightModel_static[roadID][periodID*partNum + partID] = oldModelID;
			RoadRightBestModelIDandBestAdvanceValue[roadID][0] = tmpBeastModelID;
			RoadRightBestModelIDandBestAdvanceValue[roadID][1] = tmpBestAdvanceValue;
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
				int modelid = isleft?leftlightModel_static[roadID][periodID*partNum + partID]:rightlightModel_static[roadID][periodID*partNum + partID];
				int tmpPenalty = FastRunning120T_static(periodID);//��֮ǰ
				if(isleft) leftlightModel_static[roadID][periodID*partNum + partID] = RoadRightBestModelIDandBestAdvanceValue[roadID][0];
				else rightlightModel_static[roadID][periodID*partNum + partID] = RoadRightBestModelIDandBestAdvanceValue[roadID][0];
				int newPenalty = FastRunning120T_static(periodID);//�ĺ�
				if(tmpPenalty<=newPenalty)//���������޸�
				{
					//��ԭ
					if(isleft) leftlightModel_static[roadID][periodID*partNum + partID] = modelid;
					else rightlightModel_static[roadID][periodID*partNum + partID] = modelid;
				}
				else
				{
					chooseChange.add(roadID);
					chooseChange.add(RoadRightBestModelIDandBestAdvanceValue[roadID][0]);
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

	static int trytoAdvance(int periodID,int RunningTim)
	{
		int ret = 0;
		int[] AdvanceInfo = null;
		for(int partID=0;partID<partNum;partID++)
		{
			
			if((AdvanceInfo = canAdvance_static_faster_and_learn_LeftorRight_lightTable(periodID,partID,false)) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint("RigitLightRule: "+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+", "+flowdata.RoadString(AdvanceInfo[i*2+0])+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			
			if((AdvanceInfo = canAdvance_static_faster_and_learn_LeftorRight_lightTable(periodID,partID,true)) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint("LeftLightRule: "+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+", "+flowdata.RoadString(AdvanceInfo[i*2+0])+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			
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
			
			if((AdvanceInfo = canAdvance_static_faster_and_learnisBreakRoad(periodID,partID,false)) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint("BreakRule: "+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+", "+flowdata.RoadString(AdvanceInfo[i*2+0])+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			
			if((AdvanceInfo = canAdvance_static_faster_and_learnisBreakRoad(periodID,partID,true)) != null )
			{
				int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
				for(int i=0;i<changeNum;i++){
					DebugPrint("LeftBreakRule: "+RunningTim+" : BestRoadID:"+AdvanceInfo[i*2+0]+", "+flowdata.RoadString(AdvanceInfo[i*2+0])+",BestModel:"+ AdvanceInfo[i*2+1]+";");
				}
				DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
				ret += AdvanceInfo[changeNum*2];
			}
			
			
			/*
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
		}
		
		return ret;
	}
	///ѧϰ��̬���ԣ�ÿ120Tһ������,����Str��"periodID:tlID,modelID0;...;$"
	static String[] windhunterLearningII_static(String[] filetxtset,int periodID,boolean usefaster)//����Ƿ�ѡ��faster��������ѵ��
	{
		///unfinished ! should create a new model first!
		//flowdata = new FlowData();
		flowdata = new FlowdataII();
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
		//һ�θ��¶��traffic lights
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
		String[] retStrs = new String[5];
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
		
		//Output road is LeftBreakRoad Lab :
		StringBuilder ret2 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret2.append(""+(periodID*partNum+partID)+":");
			for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
			{
				ret2.append(""+roadID+","+(isLeftBreakingRoadLab_static[roadID][periodID*partNum+partID])+";");
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
		
		//Output road is rightTable Lab :
		StringBuilder ret4 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret4.append(""+(periodID*partNum+partID)+":");
			for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
			{
				ret4.append(""+roadID+","+(rightlightModel_static[roadID][periodID*partNum+partID])+";");
			}
			ret4.append("@");
		}
		DebugPrint(ret4.toString());
		retStrs[3] = ret4.toString();
		
		//Output road is leftTable Lab :
		StringBuilder ret5 = new  StringBuilder();
		for(int partID=0;partID<partNum;partID++){
			ret5.append(""+(periodID*partNum+partID)+":");
			for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
			{
				ret5.append(""+roadID+","+(leftlightModel_static[roadID][periodID*partNum+partID])+";");
			}
			ret5.append("@");
		}
		DebugPrint(ret5.toString());
		retStrs[4] = ret5.toString();
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
			FileOutputStream outfile2 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_LeftBreakLab_"+PeriodIDStr+".txt");
			FileOutputStream outfile3 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_BreakLab_"+PeriodIDStr+".txt");
			FileOutputStream outfile4 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_RightTable_"+PeriodIDStr+".txt");
			FileOutputStream outfile5 = new FileOutputStream("./"+OutPutFile+"_"+fasterLab+"_resultOf_LeftTable_"+PeriodIDStr+".txt");
			
			//String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0906.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			//String alltxt = "./data/flow0908_guess.txt;";
			//String alltxt = "./data/Allmeanflow_2.txt;";
			String alltxt = "./data/round2_0901.txt;./data/round2_0902.txt;./data/round2_0903.txt;./data/round2_0904.txt;./data/round2_0905.txt;./data/round2_0906.txt;./data/round2_0907.txt;";

			String[] txts = alltxt.trim().split(";");
			debuglab = true;///for print 
			//flowdata = new FlowData();
			flowdata = new FlowdataII();
			int periodID = flowdata.toInt(PeriodIDStr);
			String[] lastStr = null;
			
			if(fasterLab.equals("usefaster")==false) lastStr = windhunterLearningII_static(txts,periodID,false);
			else  lastStr = windhunterLearningII_static(txts,periodID,true);
			//lastStr += windhunterLearningII_static(txts,periodID,true);
			//ÿ�����һ��ȫ������Ϊѵ��ʵ��̫��
			outfile1.write((lastStr[0]).getBytes());
			outfile2.write((lastStr[1]).getBytes());
			outfile3.write((lastStr[2]).getBytes());
			outfile4.write((lastStr[3]).getBytes());
			outfile5.write((lastStr[4]).getBytes());
			return;
		}
		catch (Exception e) {   
            e.printStackTrace();   
        } 
	}
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		//#���Allmeanflow����Ҫȷ��һ��������ϵĺû�
		//if(true) {DoSomethingInit();return;}//����lightmodel��Ҫ�Ķ���
		//if(true) {if(args.length==2) runTrainingModel(args[0],args[1],"false");else runTrainingModel(args[0],args[1],args[2]);return;}//���ⲿ�������
		
		///if Learning?
		//learninglab = true;
		if(learninglab)
		{
			debuglab = true;
			//String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0906.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/Allmeanflow.txt;";
			String alltxt = "./data/round2_0901.txt;./data/round2_0902.txt;./data/round2_0903.txt;./data/round2_0904.txt;./data/round2_0905.txt;./data/round2_0906.txt;./data/round2_0907.txt;";

			String[] txts = alltxt.trim().split(";");
			String lastStr1 = "";
			String lastStr2 = "";
			String lastStr3 = "";
			String lastStr4 = "";
			for(int i=7;i<14;i++)
			{
				//String[] ret = windhunterLearningII_static(txts,i,false);
				String[] ret = windhunterLearningII_static(txts,i,true);
				lastStr1 += ret[0];
				lastStr2 += ret[1];
				lastStr3 += ret[2];
				lastStr4 += ret[3];
				//ÿ�����һ��ȫ������Ϊѵ��ʵ��̫��
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
			DebugPrint(lastStr4);
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
		//flowdata = new FlowData();
		flowdata = new FlowdataII();
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

