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

public class MechanicII_Dynamic {
	static FlowData flowdata = null;
	static MechanicII_Model lightmodel = null;/// haven't init
	static boolean debuglab = false;
	static boolean learninglab = false;
	static void DebugPrint(String context){if(debuglab) System.out.println(context);}
	static void Debugger(String txtfile)throws NumberFormatException, IOException
	///����debug�ģ�����txt�������ݣ���flowdataģ�⣬��ģ���������ܣ�
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
	
	//Ϊ����isExTRoad����ѧϰ��Ϊÿ��·�����Լ���isExTRoad�ı�ǩ�����������е���T�������·����Ҫ��ֹ����Щ·��Ϊǰ��·ͨ��������Ҳ���ܷ���	
	static boolean isExTRoadLab_static[][];  //(RoadID,PeriodID)ÿ������ÿ��·��isExTRoadLab�ǲ�һ����
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 || flowdata.hasRoadID[roadID]==false ) return false;
		return isExTRoadLab_static[roadID][TimID/120];
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
				if(flowdata.GotoID[i][2]!=-1 && flowdata.hasRoadID[flowdata.GotoID[i][2]]==false)//һ�����в���
				{
					if(flowdata.tmp_trafficlight[i][2]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= Yu)
					{
						flowdata.tmp_trafficlight[i][2]=1;
					}
				}
				if(ScoreRoad[i]==46)//�������в���
				{
					if(flowdata.tmp_trafficlight[i][2]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 16 && 
							flowdata.roadFlow[flowdata.GotoID[i][2]][flowdata.lastTimID] <=32)
					{
						flowdata.tmp_trafficlight[i][2]=1;
					}
				}
				
				if(flowdata.GotoID[i][0]>0 && flowdata.hasRoadID[flowdata.GotoID[i][0]]==false)//����һ������
				{
					if(flowdata.tmp_trafficlight[i][0]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 2 &&flowdata.roadFlow[i][flowdata.lastTimID] <= 30)
					{
						flowdata.tmp_trafficlight[i][0]=1;
					}
				}
				
				if(flowdata.GotoID[i][0]>0 && ScoreRoad[flowdata.GotoID[i][0]]==48)//���ж���������
				{
					if(flowdata.tmp_trafficlight[i][0]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 2
							//&& flowdata.roadFlow[i][flowdata.lastTimID] <= 30
							&& flowdata.roadFlow[flowdata.GotoID[i][0]][flowdata.lastTimID] <=8)
					{
						flowdata.tmp_trafficlight[i][0]=1;
					}
				}
				
				/* No a good ideal : 
				if(flowdata.GotoID[i][1]>0 && ScoreRoad[flowdata.GotoID[i][1]]<25 
						&& ScoreRoad[i]==48 )//����ǰ���ж¿�
				{
					int last = flowdata.lastGreenlight[i][1];
					if(flowdata.lastTimID - last < 5) flowdata.tmp_trafficlight[i][1]=0;
					//System.out.println("use >4T : "+flowdata.RoadString(i));
				}
				*/
				/*
				if(flowdata.GotoID[i][1]>0 && ScoreRoad[flowdata.GotoID[i][1]]<25 
						&& ScoreRoad[i]==48 )//��ֱ��
				{
					int last = flowdata.lastGreenlight[i][1];
					if(flowdata.lastTimID - last < 5) flowdata.tmp_trafficlight[i][1]=0;
					//System.out.println("use >4T : "+flowdata.RoadString(i));
				}
				*/
			}
		}
	}
	
	///�߼���̬����
		
	static int lightModel_static[][] = null;///14��120T���ڷֿ�ѵ����ÿ��������һ����Ե�
	static int lightRoadA_static[]   = null;//һ��·�ں��̵���ʼroadA��ʵ����roadA��˭������Ҫ����Ҫ����roadA���ܱ�
	static void Windhunter_firstTimInit_static_beforeTraining()
	{
		lightModel_static = new int[flowdata.tlNum][14];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++)
		{
			for(int j=0;j<14;j++)
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
		lightModel_static = new int[flowdata.tlNum][14];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++) lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		
		//String[] PeriodTable =  Constants.NewRuleFollowTrafficRule.trim().split("@");
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule.trim().split("@");
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_lev2break.trim().split("@");
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_lev2break_fasterTrain.trim().split("@");
		//String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_lev2break_fasterTrain_AddisExTRoad.trim().split("@");
		String[] PeriodTable =  Constants.NewRuleBreakTrafficRule_lev2break_fasterTrain_AddisExTRoad_AddLeftBreaking.trim().split("@");
		//String[] PeriodTable =Constants.NewRuleBreakTrafficRule_AllBreakingRule_useFlow0908.trim().split("@");
		
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
				lightModel_static[tlID][PeriodID] = modelID;
			}
		}
		
		//init isExTRoad Lab:
		isExTRoadLab_static = new boolean[flowdata.roadNum][14];
		for(int i=0;i<flowdata.roadNum;i++) for(int j=0;j<14;j++) if(flowdata.hasRoadID[i]) isExTRoadLab_static[i][j] = (ScoreRoad[i]>25?false:true);
		
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_Rule_init;
		String isExTRoadLab_Rule = Constants.isExTRoadLab_Rule_AddLeftBreaking;
		//String isExTRoadLab_Rule = Constants.isExTRoadLab_Rule_AddLeftBreaking_useflow0908;
		
		if(isExTRoadLab_Rule.equals("")) return;
		String[] TRoadPeriodTable = isExTRoadLab_Rule.trim().split("@");//"hourID:roadIDx,trueOrfalse;roadIDy,trueOrfalse;...;@..."
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
				isExTRoadLab_static[roadID][PeriodID] = (trueorfalse==1);
			}
		}
	}
	
	///�߼���̬·��ת������,��periodID�������£�120T�����ڣ�����timID��С����
	static void updateTrafficLight_static(int timID) //periodID = timID/120
	{
		int periodID = timID/120;
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
		
		///right light independent:
		flowdata.tmp_trafficlight[roadID][1] = 1;
		if(isExTRoad(flowdata.GotoID[roadID][1],timID) && timID%5!=0) flowdata.tmp_trafficlight[roadID][1] = 0;
		
		///left light :
		flowdata.tmp_trafficlight[roadID][0] = lightmodel.getLightLab(modelID, timID%PeriodT, ABCD*2 + 0, false);
		if(isExTRoad(flowdata.GotoID[roadID][0],timID) && lightmodel.trafficLightTablefirstGreen[modelID][ABCD*2 + 0]!=timID%PeriodT){
			flowdata.tmp_trafficlight[roadID][0] = 0;///ֻ����һ����
		}
		
		///straight light:
		flowdata.tmp_trafficlight[roadID][2] = lightmodel.getLightLab(modelID, timID%PeriodT, ABCD*2 + 1, false);
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
	
	static int[] canAdvance_static_faster(int periodID)
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID);
		int[][] trafficlightBestModelIDandBestAdvanceValue = new int[flowdata.tlNum][2];//((X,0):bestModel,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.tlNum];
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{	
			if(flowdata.lightLinkRoad[tlID][0]<=0) continue;
			int oldModelID = lightModel_static[tlID][periodID];
			int tmpBeastModelID = 0 , tmpBestAdvanceValue = 0;
			trafficlightBestModelIDandBestAdvanceValue[tlID][1] = 0;
			for(int modelid = 0;modelid < lightmodel.ModelNum;modelid++)
			{
				if(modelid==oldModelID) continue;
				lightModel_static[tlID][periodID] = modelid;
				int tmpPenalty = FastRunning120T_static(periodID);
				if(LastPenalty-tmpPenalty>tmpBestAdvanceValue)
				{
					tmpBestAdvanceValue = LastPenalty-tmpPenalty;
					tmpBeastModelID = modelid;
				}
			}
			lightModel_static[tlID][periodID] = oldModelID;
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
				int modelid = lightModel_static[tlID][periodID];
				int tmpPenalty = FastRunning120T_static(periodID);//��֮ǰ
				lightModel_static[tlID][periodID] = trafficlightBestModelIDandBestAdvanceValue[tlID][0];
				int newPenalty = FastRunning120T_static(periodID);//�ĺ�
				if(tmpPenalty<=newPenalty)//���������޸�
				{
					lightModel_static[tlID][periodID] = modelid ;//��ԭ
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
	
	static int[] canAdvance_static_faster_and_learnisExTRoadLab(int periodID)
	{
		//DebugPrint(""+isExTRoadLab_static[129][0]);
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("it's T-road-lab training last_Penalty: " + LastPenalty +"  periodID: "+periodID);
		
		int[][] roadLabBestModelIDandBestAdvanceValue = new int[flowdata.roadNum][2];//((X,0):0 or 1,(X,1):bestAdvance)
		int SortArray[] = new int[flowdata.roadNum];
		for(int roadID=1;roadID<flowdata.roadNum;roadID++)
		{
			int oldModelID = (isExTRoadLab_static[roadID][periodID]?1:0);
			roadLabBestModelIDandBestAdvanceValue[roadID][1] = 0;
			isExTRoadLab_static[roadID][periodID] = ( (oldModelID^1)==0?false:true );
			int tmpPenalty = FastRunning120T_static(periodID);
			if(tmpPenalty<LastPenalty)
			{
				roadLabBestModelIDandBestAdvanceValue[roadID][1] = LastPenalty - tmpPenalty;
				roadLabBestModelIDandBestAdvanceValue[roadID][0] = (isExTRoadLab_static[roadID][periodID]?1:0);
				SortArray[roadID] = roadLabBestModelIDandBestAdvanceValue[roadID][1]*400 + roadID;
			}
			isExTRoadLab_static[roadID][periodID] = ( oldModelID==0?false:true );
		}
		//����T·��ǩ
		Arrays.sort(SortArray);
		ArrayList <Integer> chooseChange = new ArrayList<Integer>();
		for(int i = flowdata.roadNum - 1 ; i>=0;i--)
		{
			int roadID = (int)(SortArray[i]%400);
			int advance = (int)(SortArray[i]/400);
			
			if(advance>0)
			{
				boolean modelid = isExTRoadLab_static[roadID][periodID];
				int tmpPenalty = FastRunning120T_static(periodID);//��֮ǰ
				isExTRoadLab_static[roadID][periodID] = (roadLabBestModelIDandBestAdvanceValue[roadID][0]==1?true:false);
				int newPenalty = FastRunning120T_static(periodID);//�ĺ�
				if(tmpPenalty<=newPenalty)//���������޸�
				{
					isExTRoadLab_static[roadID][periodID] = modelid ;//��ԭ
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
	
	static int[] canAdvance_static(int periodID)
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID);
		int BestAtID = 0,BeastModelID = 0,BestAdvanceValue = 0;///value�����߷�����atID����ʱ����·�ڣ�fromID��Ƕ���or�ϱ�
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
	///ѧϰ��̬���ԣ�ÿ120Tһ������,����Str��"periodID:tlID,modelID0;...;$"
	static String[] windhunterLearningII_static(String[] filetxtset,int periodID,boolean usefaster)//����Ƿ�ѡ��faster��������ѵ��
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
		if(usefaster==false)//һ�θ���һ��traffic light
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
		else//һ�θ��¶��traffic lights
		{
			while( (AdvanceInfo = canAdvance_static_faster(periodID)) != null 
					|| (AdvanceInfo_Lab = canAdvance_static_faster_and_learnisExTRoadLab(periodID) ) != null )
			{
				//ע��һ�㣺 canAdvance_static_faster ���� canAdvance_static_faster_and_learnisExTRoadLab ֮ǰ���е�
				//���ԣ�canAdvance_static_faster_and_learnisExTRoadLab����canAdvance_static_faster�Ż���Ļ����ϵ�
				//ע����һ�㣬AdvanceInfo��=nullʱ��AdvanceInfo_Lab��������
				if(AdvanceInfo!=null)
				{
					int changeNum = AdvanceInfo.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
					for(int i=0;i<changeNum;i++){
						DebugPrint(""+RunningTim+" : BestTL:"+AdvanceInfo[i*2+0]+",BestModel:"+ AdvanceInfo[i*2+1]
								+",startRoadA:"+flowdata.RoadString(lightRoadA_static[AdvanceInfo[i*2+0]]) +";");
						DebugPrint("--->this Model is : "+lightmodel.getModelStr( AdvanceInfo[i*2+1]));
					}
					DebugPrint("Advance : "+AdvanceInfo[changeNum*2]);
					fullAdvance += AdvanceInfo[changeNum*2];
				}
				//��Ϊ��ע����һ�㣬AdvanceInfo��=nullʱ��AdvanceInfo_Lab�������У���������Ҫǿ������һ��,��Ȼ��ϻ����
				if(AdvanceInfo!=null) AdvanceInfo_Lab = canAdvance_static_faster_and_learnisExTRoadLab(periodID) ;
				if(AdvanceInfo_Lab!=null)
				{
					int changeNum = AdvanceInfo_Lab.length/2;//([0,changeNum*2-1],changeInfo,[changeNum*2]=>sum advance)
					for(int i=0;i<changeNum;i++){
						DebugPrint(""+RunningTim+" : BestRoadID:"+AdvanceInfo_Lab[i*2+0]+",BestModel:"+ AdvanceInfo_Lab[i*2+1]+";");
					}
					DebugPrint("Advance : "+AdvanceInfo_Lab[changeNum*2]);
					fullAdvance += AdvanceInfo_Lab[changeNum*2];
				}
				//ע�����ﲻ���޸ģ���Ϊ��canAdvance_static_faster���Ѿ����޸�����
				if(RunningTim>=100) 
				{
					DebugPrint("Out Of Max Tim:" + RunningTim);
					break;
				}
				RunningTim++;
			}
		}
		
		DebugPrint("Learning End! and Advanced : " + fullAdvance);
		String[] retStrs = new String[2];
		///OutPut Traffic Light Rule Table:
		StringBuilder ret1 = new  StringBuilder();
		ret1.append(""+periodID+":");
		for(int tlID = 1;tlID < flowdata.tlNum;tlID++)
		{
			if(flowdata.lightLinkRoad[tlID][0]>0)
			{
				ret1.append(""+tlID+","+lightModel_static[tlID][periodID]+";");
			}
		}
		ret1.append("@");
		DebugPrint(ret1.toString());
		retStrs[0] = ret1.toString();
		
		//Output road is ExTRoad Lab :
		StringBuilder ret2 = new  StringBuilder();
		ret2.append(""+periodID+":");
		for(int roadID = 1;roadID < flowdata.roadNum;roadID++)
		{
			ret2.append(""+roadID+","+(isExTRoadLab_static[roadID][periodID]?"1":"0")+";");
		}
		ret2.append("@");
		DebugPrint(ret2.toString());
		retStrs[1] = ret2.toString();
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
			//ÿ�����һ��ȫ������Ϊѵ��ʵ��̫��
			DebugPrint("NewRule:");
			DebugPrint(lastStr[0]);
			DebugPrint(lastStr[1]);
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
		
		//if(true) {DoSomethingInit();return;}//����lightmodel��Ҫ�Ķ���
		//if(true) {if(args.length==2) runTrainingModel(args[0],args[1],"false");else runTrainingModel(args[0],args[1],args[2]);return;}//���ⲿ�������
		
		///if Learning?
		//learninglab = true;
		if(learninglab)
		{
			String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			String[] txts = alltxt.trim().split(";");
			debuglab = true;///for print 
			String lastStr1 = "";
			String lastStr2 = "";
			for(int i=0;i<14;i++)
			{
				//String[] ret = windhunterLearningII_static(txts,i,false);
				String[] ret = windhunterLearningII_static(txts,i,true);
				lastStr1 += ret[0];
				lastStr2 += ret[1];
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
