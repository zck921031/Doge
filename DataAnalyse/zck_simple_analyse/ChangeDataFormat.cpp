#include<bits/stdc++.h>
using namespace std;
const string path = "../../StaticLoop/data/";
const string filename[]={"flow0901.txt","flow0902.txt","flow0903.txt","flow0904.txt","flow0905.txt","flow0906.txt","flow0907.txt"};
map<string, int> roadkey2roadid;
const int ROAD = 155, TIM = 1680, DAY = 7;
int flow[ROAD][TIM][DAY];
char str_v[1024],str_u[1024];
void analyse();
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
    return 0;
}
void analyse(){
    vector< vector<double> > mean(ROAD, vector<double>(TIM,0) );
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

    double m1=0, m2=0;
    for (int r=0; r<ROAD; r++){
        me = mse = 0;
        for (int tim=0; tim<TIM; tim++){
            for (int day=0; day<DAY; day++){
                me += fabs( mean[r][tim] - flow[r][tim][day] );
                mse += ( mean[r][tim] - flow[r][tim][day] ) * ( mean[r][tim] - flow[r][tim][day] );
            }
            if ( (tim+1)%60 == 0 ){
                me /= 60*DAY;
                mse /= 60*DAY;
                m1 = max(m1, me );
                m2 = max(m2, mse);
                me = mse = 0;
            }
        }
//        cout<<"For Road "<<r<<":"<<endl;
//        cout<<"mean error: "<<fixed<<setprecision(6)<<me<<endl;
//        cout<<"mean square error: "<<fixed<<setprecision(6)<<mse<<endl;
    }
    cout<<"road max mean error: "<<fixed<<setprecision(6)<<m1<<endl;
    cout<<"road max mean square error: "<<fixed<<setprecision(6)<<m2<<endl;

}
