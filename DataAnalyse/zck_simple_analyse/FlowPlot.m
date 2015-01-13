clear all;
clc;
g = ReadFlowByFile();

roadid = 3;
timebegin = 600;
timeend = 720;
score = zeros(1,155);
for r=1:155
    tmp = zeros(1,7);
    for i=1:7
        tmp(i) = mean( g(i, r, timebegin:timeend ) );
    end
    m = mean(tmp);
    for i=1:7
        score(r) = score(r) + abs( tmp(i) - m );
    end
end
[a, b] = sort(score);
disp([b' a']);

figure(1);
hold off;
for i=1:7
     subplot(2,4,i);
     plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
     hold on;
end

figure(2);
hold off;
for i=1:7
    plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
    hold on;
    disp( [r, i, sum( g(i, r, timebegin:timeend ) ) ] );
end