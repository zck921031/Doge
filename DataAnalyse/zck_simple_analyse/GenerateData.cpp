#include<bits/stdc++.h>
using namespace std;
const char* outputfile = "flow0908_test.txt";
const string path = "../../StaticLoop/data/";
const string filename[]={"flow0901.txt","flow0902.txt","flow0903.txt","flow0904.txt","flow0905.txt","flow0906.txt","flow0907.txt"};
map<string, int> roadkey2roadid;
const int ROAD = 155, TIM = 1680, DAY = 7;
int flow[ROAD][TIM][DAY];
vector< vector<double> > mean(ROAD, vector<double>(TIM,0) );
char str_v[1024],str_u[1024];
void analyse();
void allrand_gen();
void rand_road_gen();
void output();
int main(){
    FILE *fin;
    for (int day=0; day<DAY; day++){
        fin = fopen( ( path+filename[day] ).c_str(), "r" );
        for ( int r=0; r<ROAD; r++){
            fscanf(fin, "%[^,],%[^,]", str_v, str_u);
            string str = string(str_v)+"-"+str_u;
            int road = -1;
            if ( day == 0 ){
                road = r;
                roadkey2roadid[str] = road;
            }else{
                assert( roadkey2roadid.count(str) );
                road = roadkey2roadid[str];
            }

            for (int tim=0; tim<TIM; tim++){
                fscanf(fin, ",%d", &flow[road][tim][day]);
            }
            fgets(str_v, 1024, fin);
        }
        fclose(fin);
    }
    analyse();
    //allrand_gen();
    rand_road_gen();
    output();
    return 0;
}
void output(){
    FILE *fout = fopen(outputfile, "w");
    for (auto s: roadkey2roadid){
        string key = s.first;
        int roadid = s.second;
        key.replace( key.find("-"),1 ,"," );
        cout<<key<<" "<<roadid<<endl;
        fprintf( fout, "%s", key.c_str() );
        for (int i=0; i<TIM; i++){
            fprintf( fout, ",%d", (int)round(mean[roadid][i]) );
        }
        fprintf(fout, "\n");
    }
    fclose(fout);
}
/**
平均值为u，所有路口的流量服从均匀分布[0, 2u]
*/
void allrand_gen(){
    double sum = 0, sumcnt=0;
    for ( auto a:mean ){
        for (auto b:a ){
            sum += b;
            sumcnt += 1;
        }
    }
    double C = sum/sumcnt;
    for (int i=mean.size()-1; i>=0; i--){
        for (int j=mean[0].size()-1; j>=0; j--){
            mean[i][j] = C * 2* abs(rand())/RAND_MAX;
        }
    }
}
/**
将路口分为2种，有概率k流量比平均值多[u, (1/k)*u]，有概率(1-k)流量比平均值少[0,u]
*/
const double k = 0.1;
const double m000 = 3;//每个路口平均车辆数
void rand_road_gen(){
    srand( abs( time(NULL) ) );
    int cnt = 0;
    vector<int> sf(ROAD, 0);
    for (int i=0; i<ROAD; i++) sf[i] = i+1;
    random_shuffle(begin(sf), end(sf) );

    for (int i=0; i<ROAD; i++){
//        double m=0;
//        for (int j=0; j<TIM; j++){
//            m += mean[i][j];
//        }
//        m/=TIM;
        int kind;
        if ( sf[i] < k*ROAD ) kind = 0; else kind = 1;
        cnt += kind;
        for (int j=0; j<TIM; j++){
            double C = (double)rand()/(double)RAND_MAX;
            //double C = 0.5;
            if ( 0 == kind ){
                mean[i][j] = m000 + (1.0/k - 1.0 )*m000*C;
                //mean[i][j] = 3;
            }else{
                mean[i][j] = C * m000;
                //mean[i][j] = 3;
            }
        }
    }
    cout<<cnt<<endl;
    double sum = 0;
    for ( auto a:mean ){
        for (auto b:a ){
            sum += b;
        }
    }
    cout<<"sum="<<sum<<endl;
}
void analyse(){
    for (int r=0; r<ROAD; r++)
    for (int tim=0; tim<TIM; tim++)
    for (int day=0; day<DAY; day++){
        mean[r][tim] += (double)flow[r][tim][day]/(double)DAY;
    }

    double mse = 0, me = 0;
    for (int r=0; r<ROAD; r++)
    for (int tim=0; tim<TIM; tim++)
    for (int day=0; day<DAY; day++){
        me += fabs( mean[r][tim] - flow[r][tim][day] );
        mse += ( mean[r][tim] - flow[r][tim][day] ) * ( mean[r][tim] - flow[r][tim][day] );
    }
    me /= (ROAD*TIM*DAY);
    mse /= (ROAD*TIM*DAY);
    cout<<"mean error: "<<fixed<<setprecision(6)<<me<<endl;
    cout<<"mean square error: "<<fixed<<setprecision(6)<<mse<<endl;

//    double m1=0, m2=0;
//    for (int r=0; r<ROAD; r++){
//        me = mse = 0;
//        for (int tim=0; tim<TIM; tim++){
//            for (int day=0; day<DAY; day++){
//                me += fabs( mean[r][tim] - flow[r][tim][day] );
//                mse += ( mean[r][tim] - flow[r][tim][day] ) * ( mean[r][tim] - flow[r][tim][day] );
//            }
//            if ( (tim+1)%60 == 0 ){
//                me /= 60*DAY;
//                mse /= 60*DAY;
//                m1 = max(m1, me );
//                m2 = max(m2, mse);
//                me = mse = 0;
//            }
//        }
//        cout<<"For Road "<<r<<":"<<endl;
//        cout<<"mean error: "<<fixed<<setprecision(6)<<me<<endl;
//        cout<<"mean square error: "<<fixed<<setprecision(6)<<mse<<endl;
//    }
//    cout<<"road max mean error: "<<fixed<<setprecision(6)<<m1<<endl;
//    cout<<"road max mean square error: "<<fixed<<setprecision(6)<<m2<<endl;

}
