package MechanicII;
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
import java.io.File;   
import java.io.FileOutputStream;  

public class MechanicII_Dynamic {
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
	static boolean isExTRoad(int roadID,int TimID)///extended T road!
	{
		if(roadID <= 0 ) return false;
		int Yu = 60;
		//if ( learninglab ){
			if(ScoreRoad[roadID]<=25) return true;
		//}else{
			//if(ScoreRoad[roadID]<=25 || flowdata.roadFlow[roadID][TimID]>= Yu) return true;
		//}
		return false;
	}
	
	static void flowDischargeWithBreakingTrafficRule()//强通泄流，不管交通规则
	{
		int Yu = 10;
		for(int i=0;i<flowdata.roadNum;i++)
		{
			if(flowdata.hasRoadID[i])
			{
				if(flowdata.GotoID[i][2]!=-1 && flowdata.hasRoadID[flowdata.GotoID[i][2]]==false)
				{
					if(flowdata.tmp_trafficlight[i][2]==0 && flowdata.roadFlow[i][flowdata.lastTimID] >= Yu)
					{
						flowdata.tmp_trafficlight[i][2]=1;
					}
				}
			}
		}
	}
	
	///高级静态策略
	static String NewRuleFollowTrafficRule = "0:1,134;2,38;3,145;4,6;5,11;6,14;7,134;8,141;9,163;10,162;11,64;12,135;14,75;15,68;16,192;17,185;18,101;19,199;20,1;21,134;22,71;23,71;24,85;25,204;26,207;27,200;28,135;29,122;30,100;31,101;32,71;33,18;34,181;35,199;36,150;37,64;38,134;39,205;40,161;41,177;42,143;43,69;44,137;@1:1,44;2,29;3,101;4,87;5,87;6,0;7,134;8,134;9,112;10,0;11,58;12,44;14,71;15,11;16,98;17,207;18,0;19,0;20,100;21,134;22,71;23,69;24,203;25,18;26,206;27,204;28,47;29,31;30,175;31,140;32,204;33,204;34,197;35,22;36,160;37,24;38,136;39,73;40,202;41,17;42,133;43,73;44,8;@2:1,30;2,29;3,57;4,178;5,178;6,188;7,134;8,134;9,112;10,1;11,88;12,139;14,71;15,68;16,98;17,205;18,101;19,174;20,121;21,134;22,71;23,68;24,16;25,17;26,68;27,204;28,0;29,31;30,175;31,151;32,204;33,204;34,197;35,23;36,203;37,203;38,135;39,73;40,206;41,2;42,136;43,68;44,137;@3:1,148;2,29;3,140;4,97;5,97;6,6;7,134;8,134;9,112;10,2;11,88;12,140;14,71;15,2;16,98;17,206;18,0;19,72;20,70;21,135;22,71;23,68;24,17;25,18;26,206;27,204;28,136;29,31;30,100;31,110;32,204;33,204;34,197;35,197;36,207;37,196;38,134;39,68;40,192;41,52;42,137;43,67;44,138;@4:1,141;2,29;3,47;4,87;5,87;6,86;7,134;8,134;9,112;10,177;11,185;12,0;14,71;15,68;16,98;17,206;18,0;19,68;20,134;21,134;22,71;23,71;24,36;25,18;26,68;27,71;28,43;29,100;30,65;31,42;32,204;33,71;34,78;35,23;36,174;37,163;38,121;39,70;40,107;41,99;42,137;43,72;44,139;@5:1,134;2,29;3,146;4,87;5,87;6,86;7,134;8,134;9,112;10,13;11,58;12,135;14,71;15,76;16,98;17,207;18,207;19,0;20,99;21,134;22,71;23,70;24,16;25,16;26,205;27,204;28,47;29,100;30,65;31,136;32,204;33,204;34,197;35,22;36,207;37,19;38,136;39,205;40,101;41,101;42,133;43,69;44,133;@6:1,134;2,29;3,41;4,54;5,54;6,60;7,134;8,134;9,112;10,0;11,88;12,138;14,71;15,2;16,98;17,206;18,0;19,68;20,134;21,134;22,71;23,70;24,68;25,16;26,68;27,71;28,32;29,100;30,71;31,10;32,204;33,71;34,78;35,60;36,2;37,64;38,135;39,204;40,69;41,98;42,137;43,72;44,138;@7:1,141;2,29;3,146;4,87;5,87;6,0;7,141;8,134;9,112;10,123;11,4;12,126;14,71;15,16;16,98;17,205;18,76;19,0;20,71;21,134;22,71;23,69;24,68;25,67;26,206;27,204;28,47;29,31;30,175;31,136;32,204;33,204;34,197;35,22;36,51;37,4;38,121;39,98;40,101;41,98;42,137;43,69;44,139;@8:1,121;2,180;3,39;4,54;5,54;6,60;7,134;8,134;9,49;10,104;11,112;12,126;14,71;15,76;16,98;17,205;18,76;19,1;20,71;21,134;22,71;23,73;24,68;25,67;26,207;27,204;28,43;29,31;30,100;31,110;32,204;33,204;34,197;35,23;36,15;37,59;38,134;39,98;40,189;41,101;42,133;43,68;44,41;@9:1,98;2,97;3,158;4,87;5,87;6,86;7,134;8,141;9,163;10,0;11,88;12,10;14,71;15,57;16,154;17,205;18,76;19,0;20,31;21,134;22,71;23,71;24,16;25,18;26,16;27,204;28,43;29,100;30,18;31,6;32,204;33,204;34,197;35,22;36,13;37,112;38,134;39,69;40,61;41,53;42,137;43,68;44,8;@10:1,98;2,178;3,110;4,87;5,87;6,86;7,134;8,134;9,112;10,0;11,4;12,134;14,100;15,6;16,98;17,205;18,76;19,0;20,31;21,134;22,71;23,69;24,32;25,16;26,207;27,204;28,43;29,31;30,175;31,166;32,204;33,204;34,197;35,23;36,4;37,4;38,134;39,70;40,64;41,67;42,137;43,69;44,8;@11:1,148;2,97;3,161;4,178;5,178;6,188;7,134;8,134;9,112;10,0;11,88;12,140;14,71;15,68;16,98;17,207;18,86;19,0;20,18;21,134;22,71;23,71;24,30;25,18;26,16;27,204;28,43;29,100;30,18;31,137;32,204;33,204;34,197;35,22;36,174;37,187;38,134;39,70;40,203;41,15;42,137;43,68;44,8;@12:1,44;2,29;3,32;4,87;5,87;6,0;7,134;8,134;9,112;10,1;11,112;12,137;14,71;15,12;16,98;17,206;18,86;19,0;20,184;21,39;22,71;23,70;24,16;25,16;26,16;27,204;28,43;29,100;30,18;31,41;32,204;33,204;34,197;35,23;36,206;37,98;38,121;39,70;40,207;41,2;42,120;43,69;44,8;@13:1,134;2,29;3,158;4,87;5,87;6,86;7,134;8,134;9,112;10,0;11,72;12,140;14,71;15,14;16,98;17,205;18,76;19,0;20,31;21,134;22,71;23,69;24,16;25,17;26,17;27,204;28,43;29,31;30,175;31,153;32,204;33,204;34,197;35,22;36,174;37,98;38,121;39,70;40,2;41,3;42,133;43,68;44,9;@";
	
	static String NewRuleBreakTrafficRule = "0:1,148;2,178;3,140;4,60;5,66;6,18;7,134;8,141;9,163;10,162;11,64;12,123;14,71;15,68;16,206;17,194;18,101;19,199;20,1;21,134;22,71;23,71;24,140;25,204;26,207;27,200;28,135;29,134;30,134;31,110;32,71;33,3;34,179;35,199;36,144;37,64;38,134;39,205;40,161;41,177;42,139;43,2;44,133;@1:1,163;2,180;3,135;4,197;5,205;6,71;7,91;8,134;9,112;10,98;11,88;12,140;14,71;15,86;16,98;17,207;18,64;19,207;20,134;21,135;22,71;23,204;24,207;25,204;26,206;27,204;28,136;29,141;30,134;31,125;32,103;33,87;34,178;35,178;36,0;37,88;38,134;39,173;40,16;41,17;42,133;43,66;44,137;@2:1,68;2,180;3,135;4,197;5,204;6,204;7,128;8,134;9,49;10,123;11,88;12,134;14,71;15,68;16,98;17,205;18,101;19,174;20,122;21,135;22,71;23,204;24,68;25,70;26,71;27,71;28,137;29,134;30,134;31,140;32,172;33,149;34,142;35,76;36,1;37,88;38,134;39,71;40,202;41,2;42,133;43,68;44,133;@3:1,187;2,92;3,135;4,198;5,204;6,204;7,91;8,134;9,49;10,121;11,88;12,140;14,71;15,65;16,66;17,206;18,1;19,72;20,134;21,136;22,71;23,204;24,16;25,17;26,204;27,204;28,137;29,134;30,134;31,138;32,126;33,111;34,178;35,97;36,1;37,88;38,134;39,204;40,15;41,14;42,133;43,68;44,9;@4:1,141;2,95;3,135;4,197;5,65;6,65;7,91;8,134;9,163;10,171;11,187;12,139;14,71;15,68;16,176;17,206;18,174;19,68;20,134;21,136;22,71;23,71;24,98;25,70;26,68;27,71;28,138;29,134;30,135;31,138;32,1;33,137;34,137;35,77;36,1;37,187;38,141;39,204;40,101;41,98;42,137;43,68;44,138;@5:1,187;2,92;3,135;4,197;5,205;6,71;7,91;8,134;9,112;10,104;11,187;12,140;14,71;15,76;16,98;17,101;18,98;19,174;20,71;21,40;22,71;23,204;24,16;25,16;26,204;27,204;28,137;29,134;30,134;31,137;32,136;33,126;34,138;35,178;36,80;37,187;38,140;39,204;40,101;41,101;42,120;43,69;44,136;@6:1,68;2,29;3,40;4,65;5,65;6,65;7,91;8,134;9,112;10,0;11,88;12,139;14,71;15,16;16,177;17,206;18,0;19,68;20,134;21,135;22,71;23,71;24,98;25,69;26,70;27,71;28,136;29,141;30,134;31,135;32,103;33,126;34,133;35,188;36,1;37,88;38,134;39,204;40,101;41,98;42,137;43,68;44,138;@7:1,148;2,95;3,140;4,65;5,204;6,204;7,91;8,134;9,112;10,125;11,187;12,141;14,71;15,57;16,66;17,205;18,76;19,0;20,161;21,135;22,71;23,71;24,68;25,70;26,70;27,75;28,136;29,134;30,134;31,125;32,0;33,126;34,133;35,178;36,0;37,187;38,141;39,204;40,101;41,98;42,133;43,69;44,136;@8:1,68;2,183;3,123;4,197;5,65;6,65;7,114;8,134;9,112;10,104;11,112;12,136;14,71;15,16;16,177;17,205;18,76;19,1;20,57;21,135;22,71;23,71;24,68;25,69;26,207;27,204;28,136;29,134;30,134;31,135;32,103;33,87;34,178;35,178;36,15;37,88;38,134;39,204;40,66;41,101;42,137;43,72;44,41;@9:1,68;2,180;3,135;4,197;5,205;6,71;7,91;8,134;9,163;10,0;11,88;12,121;14,71;15,2;16,66;17,205;18,76;19,0;20,31;21,135;22,71;23,204;24,68;25,205;26,204;27,204;28,137;29,141;30,134;31,135;32,103;33,111;34,178;35,78;36,13;37,88;38,134;39,204;40,2;41,2;42,120;43,16;44,8;@10:1,163;2,180;3,123;4,197;5,204;6,204;7,91;8,134;9,112;10,7;11,72;12,134;14,71;15,6;16,98;17,205;18,76;19,0;20,31;21,135;22,71;23,71;24,68;25,204;26,204;27,204;28,137;29,134;30,134;31,125;32,103;33,111;34,178;35,97;36,0;37,72;38,134;39,204;40,199;41,174;42,133;43,207;44,8;@11:1,187;2,92;3,135;4,198;5,204;6,204;7,82;8,134;9,112;10,125;11,88;12,10;14,71;15,68;16,98;17,207;18,76;19,0;20,184;21,135;22,71;23,204;24,68;25,205;26,204;27,204;28,136;29,134;30,134;31,135;32,103;33,111;34,178;35,178;36,1;37,88;38,134;39,71;40,2;41,3;42,120;43,69;44,8;@12:1,163;2,183;3,123;4,197;5,205;6,71;7,91;8,134;9,112;10,104;11,187;12,141;14,71;15,68;16,98;17,207;18,76;19,0;20,161;21,135;22,71;23,204;24,207;25,207;26,204;27,204;28,136;29,134;30,134;31,133;32,126;33,133;34,178;35,178;36,13;37,187;38,141;39,71;40,68;41,98;42,120;43,69;44,136;@13:1,141;2,95;3,146;4,78;5,65;6,65;7,91;8,134;9,163;10,0;11,72;12,134;14,71;15,200;16,201;17,205;18,76;19,0;20,31;21,135;22,71;23,204;24,205;25,204;26,204;27,204;28,136;29,141;30,134;31,125;32,1;33,188;34,155;35,178;36,4;37,72;38,134;39,204;40,2;41,2;42,133;43,68;44,0;@";
	
	//static String NewRuleBreakTrafficRule = "0:1,148;2,178;3,140;4,60;5,66;6,18;7,134;8,141;9,163;10,162;11,64;12,123;14,71;15,68;16,206;17,194;18,101;19,199;20,1;21,134;22,71;23,71;24,140;25,204;26,207;27,200;28,135;29,134;30,134;31,110;32,71;33,3;34,179;35,199;36,144;37,64;38,134;39,205;40,161;41,177;42,139;43,2;44,133;@1:1,163;2,180;3,135;4,197;5,205;6,71;7,91;8,134;9,112;10,98;11,88;12,140;14,71;15,86;16,98;17,207;18,64;19,207;20,134;21,135;22,71;23,204;24,207;25,204;26,206;27,204;28,136;29,141;30,134;31,125;32,103;33,87;34,178;35,178;36,0;37,88;38,134;39,173;40,16;41,17;42,133;43,66;44,137;@2:1,68;2,180;3,135;4,197;5,204;6,204;7,128;8,134;9,49;10,123;11,88;12,134;14,71;15,68;16,98;17,205;18,101;19,174;20,122;21,135;22,71;23,204;24,68;25,70;26,71;27,71;28,137;29,134;30,134;31,140;32,172;33,149;34,142;35,76;36,1;37,88;38,134;39,71;40,202;41,2;42,133;43,68;44,133;@3:1,187;2,92;3,135;4,198;5,204;6,204;7,91;8,134;9,49;10,121;11,88;12,140;14,71;15,65;16,66;17,206;18,1;19,72;20,134;21,136;22,71;23,204;24,16;25,17;26,204;27,204;28,137;29,134;30,134;31,138;32,126;33,111;34,178;35,97;36,1;37,88;38,134;39,204;40,15;41,14;42,133;43,68;44,9;@4:1,141;2,95;3,135;4,197;5,65;6,65;7,91;8,134;9,163;10,171;11,187;12,139;14,71;15,68;16,176;17,206;18,174;19,68;20,134;21,136;22,71;23,71;24,98;25,70;26,68;27,71;28,138;29,134;30,135;31,138;32,1;33,137;34,137;35,77;36,1;37,187;38,141;39,204;40,101;41,98;42,137;43,68;44,138;@5:1,187;2,92;3,135;4,197;5,205;6,71;7,91;8,134;9,112;10,104;11,187;12,140;14,71;15,76;16,98;17,101;18,98;19,174;20,71;21,40;22,71;23,204;24,16;25,16;26,204;27,204;28,137;29,134;30,134;31,137;32,136;33,126;34,138;35,178;36,80;37,187;38,140;39,204;40,101;41,101;42,120;43,69;44,136;@6:1,68;2,29;3,40;4,65;5,65;6,65;7,91;8,134;9,112;10,0;11,88;12,139;14,71;15,16;16,177;17,206;18,0;19,68;20,134;21,135;22,71;23,71;24,98;25,69;26,70;27,71;28,136;29,141;30,134;31,135;32,103;33,126;34,133;35,188;36,1;37,88;38,134;39,204;40,101;41,98;42,137;43,68;44,138;@7:1,141;2,29;3,146;4,87;5,87;6,0;7,141;8,134;9,112;10,123;11,4;12,126;14,71;15,16;16,98;17,205;18,76;19,0;20,71;21,134;22,71;23,69;24,68;25,67;26,206;27,204;28,47;29,31;30,175;31,136;32,204;33,204;34,197;35,22;36,51;37,4;38,121;39,98;40,101;41,98;42,137;43,69;44,139;@8:1,68;2,183;3,123;4,197;5,65;6,65;7,114;8,134;9,112;10,104;11,112;12,136;14,71;15,16;16,177;17,205;18,76;19,1;20,57;21,135;22,71;23,71;24,68;25,69;26,207;27,204;28,136;29,134;30,134;31,135;32,103;33,87;34,178;35,178;36,15;37,88;38,134;39,204;40,66;41,101;42,137;43,72;44,41;@9:1,68;2,180;3,135;4,197;5,205;6,71;7,91;8,134;9,163;10,0;11,88;12,121;14,71;15,2;16,66;17,205;18,76;19,0;20,31;21,135;22,71;23,204;24,68;25,205;26,204;27,204;28,137;29,141;30,134;31,135;32,103;33,111;34,178;35,78;36,13;37,88;38,134;39,204;40,2;41,2;42,120;43,16;44,8;@10:1,163;2,180;3,123;4,197;5,204;6,204;7,91;8,134;9,112;10,7;11,72;12,134;14,71;15,6;16,98;17,205;18,76;19,0;20,31;21,135;22,71;23,71;24,68;25,204;26,204;27,204;28,137;29,134;30,134;31,125;32,103;33,111;34,178;35,97;36,0;37,72;38,134;39,204;40,199;41,174;42,133;43,207;44,8;@11:1,187;2,92;3,135;4,198;5,204;6,204;7,82;8,134;9,112;10,125;11,88;12,10;14,71;15,68;16,98;17,207;18,76;19,0;20,184;21,135;22,71;23,204;24,68;25,205;26,204;27,204;28,136;29,134;30,134;31,135;32,103;33,111;34,178;35,178;36,1;37,88;38,134;39,71;40,2;41,3;42,120;43,69;44,8;@12:1,163;2,183;3,123;4,197;5,205;6,71;7,91;8,134;9,112;10,104;11,187;12,141;14,71;15,68;16,98;17,207;18,76;19,0;20,161;21,135;22,71;23,204;24,207;25,207;26,204;27,204;28,136;29,134;30,134;31,133;32,126;33,133;34,178;35,178;36,13;37,187;38,141;39,71;40,68;41,98;42,120;43,69;44,136;@13:1,141;2,95;3,146;4,78;5,65;6,65;7,91;8,134;9,163;10,0;11,72;12,134;14,71;15,200;16,201;17,205;18,76;19,0;20,31;21,135;22,71;23,204;24,205;25,204;26,204;27,204;28,136;29,141;30,134;31,125;32,1;33,188;34,155;35,178;36,4;37,72;38,134;39,204;40,2;41,2;42,133;43,68;44,0;@";
	
	
	static int lightModel_static[][] = null;///14个120T周期分开训练，每个周期用一组策略灯
	static int lightRoadA_static[]   = null;//一个路口红绿灯起始roadA；实际上roadA是谁并不重要，重要的是roadA不能变
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
		lightModel_static = new int[flowdata.tlNum][14];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++) lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		
		//String[] PeriodTable =  NewRuleFollowTrafficRule.trim().split("@");
		String[] PeriodTable =  NewRuleBreakTrafficRule.trim().split("@");
		
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
	}
	
	///高级静态路灯转换策略,第periodID大周期下（120T的周期），第timID个小周期
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
		flowDischargeWithBreakingTrafficRule();
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
			flowdata.tmp_trafficlight[roadID][0] = 0;///只亮第一个绿
		}
		
		///straight light:
		flowdata.tmp_trafficlight[roadID][2] = lightmodel.getLightLab(modelID, timID%PeriodT, ABCD*2 + 1, false);
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
	
	static int[] canAdvance_static(int periodID)
	{
		int LastPenalty = FastRunning120T_static(periodID);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID);
		int BestAtID = 0,BeastModelID = 0,BestAdvanceValue = 0;///value最大提高分数，atID最优时所在路口，fromID标记东西or南北
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{	
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
	///学习静态策略，每120T一个策略,策略Str："periodID:tlID,modelID0;...;$"
	static String windhunterLearningII_static(String[] filetxtset,int periodID)
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
		DebugPrint("Learning End! and Advanced : " + fullAdvance);
		
		///OutPut Traffic Light Rule Table:
		StringBuilder ret = new  StringBuilder();
		ret.append(""+periodID+":");
		for(int tlID = 1;tlID < flowdata.tlNum;tlID++)
		{
			if(flowdata.lightLinkRoad[tlID][0]>0)
			{
				ret.append(""+tlID+","+lightModel_static[tlID][periodID]+";");
			}
		}
		ret.append("@");
		DebugPrint(ret.toString());
		
		return ret.toString();
	}
	
	
	static void DoSomethingInit()
	{
		lightmodel = new MechanicII_Model();
		lightmodel.BuildModels();
	}
	static void runTrainingModel(String PeriodIDStr,String OutPutFile)
	{
		try{
			FileOutputStream outfile = new FileOutputStream("./"+OutPutFile+"_resultOf_"+PeriodIDStr+".txt");
			String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			String[] txts = alltxt.trim().split(";");
			debuglab = true;///for print 
			flowdata = new FlowData();
			int periodID = flowdata.toInt(PeriodIDStr);
			String lastStr = "";
			lastStr += windhunterLearningII_static(txts,periodID);
			//每次输出一次全集，因为训练实在太慢
			DebugPrint("NewRule:");
			DebugPrint(lastStr);
			outfile.write((lastStr).getBytes());
			return;
		}
		catch (Exception e) {   

            e.printStackTrace();   

        } 
		
	}
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		
		//if(true) {DoSomethingInit();return;}//生成lightmodel需要的东西
		//if(true) {runTrainingModel(args[0],args[1]);return;}//在外部多次运行
		
		///if Learning?
		//learninglab = true;
		if(learninglab)
		{
			String alltxt = "./data/flow0901.txt;./data/flow0903.txt;./data/flow0905.txt;./data/flow0907.txt;";
			//String alltxt = "./data/flow0901.txt;";
			String[] txts = alltxt.trim().split(";");
			debuglab = true;///for print 
			String lastStr = "";
			for(int i=0;i<14;i++)
			{
				lastStr += windhunterLearningII_static(txts,i);
				//每次输出一次全集，因为训练实在太慢
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
			}
			DebugPrint("NewRule:");
			DebugPrint(lastStr);
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
