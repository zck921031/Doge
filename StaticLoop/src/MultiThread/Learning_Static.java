package MultiThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class Learning_Static {
	/**
	 * 线程数
	 */
	private static int cores = 4;
	boolean debuglab = true;
	String rule = StaticRule.rule;
	/**
	 * @Dimension flowdataPool[cores]
	 */
	FlowData flowdataPool[] = new FlowData[cores];
	/**
	 * @Dimension　lightModel_staticPool[cores][flowdata.tlNum][14]
	 */
	int lightModel_staticPool[][][] = null;///14个120T周期分开训练，每个周期用一组策略灯
	FlowData flowdata;
	int lightModel_static[][] = null;///14个120T周期分开训练，每个周期用一组策略灯
	/**
	 * 一个路口红绿灯起始roadA；不变量。实际上roadA是谁并不重要，重要的是roadA不能变。
	 */
	int lightRoadA_static[] = null;
	
	/**
	 * 默认构造函数，初始化 flowdata, lightModel_static, lightRoadA_static
	 */
	Learning_Static(){
		String alltxt = "./data/flow0901.txt;./data/flow0902.txt;./data/flow0903.txt;./data/flow0904.txt;./data/flow0905.txt;./data/flow0907.txt;";
		String[] txts = alltxt.trim().split(";");
		for (int i=0; i<cores; i++){
			flowdataPool[i] = new FlowData();
			flowdataPool[i].initJudgeFromMultiTxts(txts);
		}		
		flowdata = flowdataPool[0];
		Windhunter_firstTimInit_static_FromRuleStr();
	}	
	void Windhunter_firstTimInit_static_FromRuleStr()
	{
		lightModel_static = new int[flowdata.tlNum][14];
		lightRoadA_static = new int[flowdata.tlNum];
		for(int i=1;i<flowdata.tlNum;i++) lightRoadA_static[i] = flowdata.lightLinkRoad[i][0];
		
		String[] PeriodTable = rule.trim().split("@");
		
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
		lightModel_staticPool = new int [cores][flowdata.tlNum][14];
		for (int c=0; c<cores; c++){
			for (int i=0; i<flowdata.tlNum; i++){
				for (int j=0; j<14; j++){
					lightModel_staticPool[c][i][j] = lightModel_static[i][j];
				}
			}
		}
	}
	/**
	 * 用这个方法更新策略，保证辅助数组和原始数组之间的同步。
	 * @param x = AdvanceInfo[0] 
	 * @param y = periodID
	 * @param z = AdvanceInfo[1]
	 */
	void set_lightModel_static(int x, int y, int z){
		lightModel_static[x][y] = z;
		for (int c=0; c<cores; c++){
			lightModel_staticPool[c][x][y] = z;
		}
	}
	String learning(int periodID){
		DebugPrint("Learning Started!");
		int RunningTim = 0;
		int fullAdvance = 0;		
		int[] AdvanceInfo = null;//new int[]{1,1,1};
		
		{
			while( (AdvanceInfo = canAdvance_static(periodID)) != null )
			{
				DebugPrint(""+RunningTim+" : BestTL:"+AdvanceInfo[0]+",BestModel:"+ AdvanceInfo[1]
						+",startRoadA:"+flowdata.RoadString(lightRoadA_static[AdvanceInfo[0]]) +",advance:"+AdvanceInfo[2]+";");
				DebugPrint("the Model is : " + MechanicII_Model.getModelStr( AdvanceInfo[1]));
				fullAdvance += AdvanceInfo[2];
//				lightModel_static[AdvanceInfo[0]][periodID] = AdvanceInfo[1];
//				for (int c=0; c<cores; c++){
//					lightModel_staticPool[c][AdvanceInfo[0]][periodID] = AdvanceInfo[1];
//				}
				set_lightModel_static(AdvanceInfo[0], periodID, AdvanceInfo[1] );
				if(RunningTim>=100) 
				{
					DebugPrint("Out Of Max Tim:" + RunningTim);
					break;
				}
				RunningTim++;
			}
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
	/**
	 * 这个方法改成多线程的了。算梯度的时候所有模型并发算。
	 * @param periodID
	 * @return
	 */
	int[] canAdvance_static(int periodID)
	{
		//int LastPenalty = FastRunning120T_static(periodID);
		int LastPenalty = Run120T.run120T(flowdata, lightModel_static, periodID, lightRoadA_static);
		DebugPrint("last Penalty achieve: " + LastPenalty +"  periodID: "+periodID);
		int BestAtID = 0,BeastModelID = 0,BestAdvanceValue = 0;///value最大提高分数，atID最优时所在路口，fromID标记东西or南北
		for(int tlID=1;tlID<flowdata.tlNum;tlID++)
		{	
			if(flowdata.lightLinkRoad[tlID][0]<=0) continue;
			int oldModelID = lightModel_static[tlID][periodID];
			/**
			 * 用来判断进程是否结束
			 */
			CountDownLatch count = new CountDownLatch(MechanicII_Model.ModelNum);
			/**
			 * mi=[0, Modelnum)，原子操作保证同步
			 */
			AtomicLong mi = new AtomicLong(0);			
			int result[] = new int [MechanicII_Model.ModelNum];
			/**
			 * get gradient multi-threads
			 */
			for (int c=0; c<cores; c++){
				new CalcGradientThreads(count, flowdataPool[c], lightModel_staticPool[c], tlID,
						periodID, lightRoadA_static, mi, result).start();
			}
			///等待所有进程结束
			try {
				count.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(int modelid = 0;modelid < MechanicII_Model.ModelNum;modelid++)
			{
				int tmpPenalty = result[modelid];
				if(LastPenalty-tmpPenalty>BestAdvanceValue)
				{
					BestAdvanceValue = LastPenalty-tmpPenalty;
					BestAtID = tlID;
					BeastModelID = modelid;
				}
				//System.out.println(tmpPenalty);
			}
			for (int c=0; c<cores; c++){
				lightModel_staticPool[c][tlID][periodID] = oldModelID;
			}
		}
		if(BestAdvanceValue>0)
		{
			return new int[]{BestAtID,BeastModelID,BestAdvanceValue};
		}
		return null;
	}
	
	public static void main(String[] args) {
		Learning_Static test = new Learning_Static();
		//test.test1();
		test.learning(0);
	}
//	void test1() {
//		for (int i=0; i<cores; i++){
//			System.out.println( flowdataPool[i].initOK );
//		}
//		for (int i=0; i<120; i++){
//		
//		}
//		AtomicLong cc = new AtomicLong(0);
//		int T = 101;
//		int ii[] = new int [T];
//		CountDownLatch count = new CountDownLatch(T);
//		CountDownLatch core = new CountDownLatch(cores);
//		//Thread td[] = new Thread[T];
//		Run rv = new Run(count, 1, ii, cc);
//		for (int i=0; i<T; i++){
//			//td[i] = new Run(count, i, ii);
//			new Thread( new Run(count, i, ii, cc) ).start();
//		}
////		for (int i=0; i<T; i++){
////			td[i].start();
////		}	
//
//		try {
//			count.await();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println( "finish~" + ii[0] );
//	}
	void DebugPrint(String context){if(debuglab) System.out.println(context);}
}

class Run120T{
	/**
	 * 用多文件计算120T的罚时总和。
	 * @param flowdata
	 * @param lightModel_static
	 * @param periodID
	 * @param lightRoadA_static
	 * @return
	 */
	public static int run120T(FlowData flowdata, int lightModel_static[][], int periodID, int lightRoadA_static[])
	{		
		double SumPenalty = 0;
		for(int dataid = 0;dataid < flowdata.BigDataNum ;dataid++)
		{
			//每组数据需要初始化一次lastTimID
			flowdata.lastTimID = periodID*120-1;
			flowdata.SumPenalty = 0;
			for(int i=0;i<120;i++)
			{
				//注意每组训练数据都要读入一次；
				flowdata.updataMultiData(dataid , periodID*120 + i);
				///注意不需要运行Windhunter_firstTimInit()，因为canAdvance里已经准备好了lightT，lightStartID
				Change_Lights_Static.updateTrafficLight_static(flowdata, 
						lightModel_static, lightRoadA_static, periodID*120 + i);
				flowdata.updataTrafficLightToHistory();
				double tmpPenalty = flowdata.updataPenalty(periodID*120 + i);
				SumPenalty += tmpPenalty;
				///DebugPrint(""+i + " tPenalty : "+tmpPenalty + " ; All_Penalty : "+flowdata.SumPenalty);		
			}
		}
		//DebugPrint(":"+SumPenalty);		
		return (int)SumPenalty; 		
	}
}

class CalcGradientThreads extends Thread{
	CountDownLatch count;
	FlowData flowdata;
	int lightModel_static[][];
	int tlID;
	int periodID;
	int lightRoadA_static[];
	AtomicLong mi;
	int result[];	
	CalcGradientThreads(CountDownLatch _count, FlowData _flowdata, int _lightModel_static[][], int _tlID,
			int _periodID, int _lightRoadA_static[], AtomicLong _mi,int _result[]){
		count = _count;
		flowdata = _flowdata;
		lightModel_static = _lightModel_static;
		tlID = _tlID;
		periodID = _periodID;
		lightRoadA_static = _lightRoadA_static;
		mi = _mi;
		result = _result;
	}
	public void run(){
		int modelid;
		while ( ( modelid = (int)mi.getAndAdd(1) ) < MechanicII_Model.ModelNum ){		
			lightModel_static[tlID][periodID] = modelid;
			result[modelid] = Run120T.run120T(flowdata, lightModel_static, periodID, lightRoadA_static);			
			count.countDown();
		}		
	}
	
}
///**
// * 
// * for test
// *
// */
//class Run extends Thread{
//	CountDownLatch count;
//	int idx;
//	static int id;
//	int ii[];
//	AtomicLong cc;
//	Run(CountDownLatch c, int _idx ,int _ii[], AtomicLong _cc){
//		count = c;
//		idx=_idx;
//		ii = _ii;
//		cc = _cc;
//	}
//	public void run(){		
//		synchronized(count){
////			for (int i=0; i<1e6; i++) 
////			{
////				ii[0]++;
////			}
//			System.out.println( cc.getAndAdd(1) + " : " + ii[0] );
//		}
//		try {
//			Thread.sleep(1000);			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		count.countDown();
//	}
//}