package search;
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

public class GreedySearch {
	static FlowData flowdata = null;
	
	static void search_1st(FlowData flowdata, int TimID){
		for (int i=1; i<flowdata.tlNum; i++){
			int best = Integer.MAX_VALUE;
			int bestmask = 0;
			for (int mask=0; mask<(1<<8); mask++ )
			{
				int tmpmask = mask;
				for (int j = 0; j < flowdata.lightLinkRoad[i].size(); j++ )
				{
					int r = flowdata.lightLinkRoad[i].get(j);
					flowdata.tmp_trafficlight[r][0] = tmpmask%2;
					flowdata.history_trafficlight[r][0][TimID] = tmpmask%2;
					tmpmask>>=1;
					flowdata.tmp_trafficlight[r][1] = 1;
					flowdata.history_trafficlight[r][1][TimID] = 1;
					flowdata.tmp_trafficlight[r][2] = tmpmask%2;
					flowdata.history_trafficlight[r][2][TimID] = tmpmask%2;
					tmpmask>>=1;
				}
				int sum = 0;
				for (int j = 0; j < flowdata.lightLinkRoad[i].size(); j++ )
				{
					int r = flowdata.lightLinkRoad[i].get(j);
					sum += flowdata.CalcRoadPenalty(r, TimID);
					//System.out.println( flowdata.RoadString(r) );
				}
				if ( best > sum ){
					best = sum;
					bestmask = mask;
				}				
			}

			//System.out.println(bestmask+ " : " + best );
			for (int j = 0; j < flowdata.lightLinkRoad[i].size(); j++ )
			{
				int r = flowdata.lightLinkRoad[i].get(j);
				flowdata.tmp_trafficlight[r][0] = bestmask%2;
				flowdata.history_trafficlight[r][0][TimID] = bestmask%2;
				bestmask>>=1;
				flowdata.tmp_trafficlight[r][1] = 1;
				flowdata.history_trafficlight[r][1][TimID] = 1;
				flowdata.tmp_trafficlight[r][2] = bestmask%2;
				flowdata.history_trafficlight[r][2][TimID] = bestmask%2;
				bestmask>>=1;
			}			
		}
	}
	static String Search_AI(int TimID)
	{
		search_1st(flowdata, TimID);
		return flowdata.getTmpTrafficLight();		
	}
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		
		//if(true) {DoSomethingInit();return;}//生成lightmodel需要的东西
		//if(true) {if(args.length==2) runTrainingModel(args[0],args[1],"false");else runTrainingModel(args[0],args[1],args[2]);return;}//在外部多次运行
		
		int count = 0;
		flowdata = new FlowData();
		flowdata.initTrafficLogic();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String flows_str = br.readLine();	
		while(!"end".equalsIgnoreCase(flows_str)){
			///push data to flowdata from readString
			flowdata.updataFromStringByMoniqi(flows_str,count);
			///Do AI function
			String AI_trafficLightTable = Search_AI(count);
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
