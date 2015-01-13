clear all;
clc;
g = ReadFlowByFile();

roadid = 94;
timebegin = 600;
timeend = 720;
for r=1:155
for i=1:7
    disp( [r, i, sum( g(i, r, timebegin:timeend ) ) ] );
end
end

figure(1);
for i=1:7
     subplot(2,4,i);
     plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
     hold on;
end
hold off;

figure(2);
for i=1:7
    plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
    hold on;
end
hold off;