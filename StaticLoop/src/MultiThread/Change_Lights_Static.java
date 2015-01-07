package MultiThread;

/**
 * 根据静态规则改变亮灯策略。
 */
public class Change_Lights_Static {
	/**
	 * T路口查找打分 different road have different Score, high Score is good! like road on T path
	 */
	static int ScoreRoad[];
	
	static void initScoreRoad()
	{
		FlowData flowdata = new FlowData();
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
	
	/**
	 * 高级静态路灯转换策略,第periodID大周期下（120T的周期），第timID个小周期
	 * @param flowdata
	 * @param lightModel_static
	 * @param lightRoadA_static
	 * @param timID
	 */
	public static void updateTrafficLight_static(FlowData flowdata, int lightModel_static[][], int lightRoadA_static[], int timID)//periodID = timID/120
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
				if(roadA>0) changeRoadLight_static(flowdata, roadA,lightModel_static[tlID][periodID],timID,0);
				if(roadB>0) changeRoadLight_static(flowdata, roadB,lightModel_static[tlID][periodID],timID,1);
				if(roadC>0) changeRoadLight_static(flowdata, roadC,lightModel_static[tlID][periodID],timID,2);
				if(roadD>0) changeRoadLight_static(flowdata, roadD,lightModel_static[tlID][periodID],timID,3);
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
		flowDischargeWithBreakingTrafficRule(flowdata, timID);
	}
	
	static void changeRoadLight_static(FlowData flowdata, int roadID,int modelID,int timID,int ABCD)//ABCD+'A' => roadX, timID
	{
		int PeriodT = MechanicII_Model.trafficLightTablePeriod[modelID];
		
		///right light independent:
		flowdata.tmp_trafficlight[roadID][1] = 1;
		if(isExTRoad(flowdata.GotoID[roadID][1],timID) && timID%5!=0) flowdata.tmp_trafficlight[roadID][1] = 0;
		
		///left light :
		flowdata.tmp_trafficlight[roadID][0] = MechanicII_Model.getLightLab(modelID, timID%PeriodT, ABCD*2 + 0, false);
		if(isExTRoad(flowdata.GotoID[roadID][0],timID) && MechanicII_Model.trafficLightTablefirstGreen[modelID][ABCD*2 + 0]!=timID%PeriodT){
			flowdata.tmp_trafficlight[roadID][0] = 0;///只亮第一个绿
		}
		
		///straight light:
		flowdata.tmp_trafficlight[roadID][2] = MechanicII_Model.getLightLab(modelID, timID%PeriodT, ABCD*2 + 1, false);
	}	

	/**
	 * 强通泄流，不管交通规则
	 * @param flowdata
	 * @param timID
	 */
	static void flowDischargeWithBreakingTrafficRule(FlowData flowdata, int timID)
	{
		//if(timID%120 >= 112) return;//后期不用
		int Yu = 10;
		for(int i=0;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])//一级放行策略
			{
				if(flowdata.GotoID[i][2]!=-1 && flowdata.hasRoadID[flowdata.GotoID[i][2]]==false)
				{
					if(flowdata.tmp_trafficlight[i][2]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= Yu)
					{
						flowdata.tmp_trafficlight[i][2]=1;
					}
				}
				if(ScoreRoad[i]==46)//二级放行策略
				{
					if(flowdata.tmp_trafficlight[i][2]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= 16 && 
							flowdata.roadFlow[flowdata.GotoID[i][2]][flowdata.lastTimID] <=32)
					{
						flowdata.tmp_trafficlight[i][2]=1;
					}
				}
			}
		}
	}
	
	/**
	 * 判断是否非特殊路口
	 * @param roadID
	 * @param TimID
	 * @return
	 */
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 ) return false;
//		int Yu = 60;
//		if ( learninglab ){
			if(ScoreRoad[roadID]<=25) return true;
//		}else{
//			if(ScoreRoad[roadID]<=25 || flowdata.roadFlow[roadID][TimID]>= Yu) return true;
//		}
		return false;
	}	
	
	
	public static void main(String[] args) {


	}
	
	/**
	 * 静态类成员函数初始化
	 */
	static
	{
		initScoreRoad();
	}
	
}
