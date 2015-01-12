clear all;
clc;
g = ReadFlowByFile();

roadid = 5;
timebegin = 600;
timeend = 720;
figure(1);
for i=1:7
     subplot(2,4,i);
     plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
     hold on;
end

figure(2);
for i=1:7
    plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
    hold on;
    disp( [i, sum( g(i, roadid, timebegin:timeend ) ) ] );
end