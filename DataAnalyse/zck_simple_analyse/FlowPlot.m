clear all;
clc;
g = ReadFlowByFile();

roadid = 92;%52,78
timebegin = 1;
timeend = 1680;
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

%figure(2);
%hold off;
%for i=1:7
%    plot( reshape( g(i, roadid, timebegin:timeend ), 1, timeend-timebegin+1 ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
%    hold on;
%    disp( [r, i, sum( g(i, r, timebegin:timeend ) ) ] );
%end

cut = 10;
gg=zeros(7,155,1680/cut);
alpha = 0.1;
for i=1:7
    for road=1:155
        for j=1:1680
                gg(i,road,ceil(j/cut))=gg(i,road,ceil(j/cut))+g(i,road,j);
        end
    end
end

% ggg=gg;
% for i=1:7
%     for road=1:155
%         for j=1:ceil(1680/cut)
%             if j>1
%                 ggg(i,road,j)=ggg(i,road,j)*alpha+gg(i,road,j)*(1-alpha);
%             end
%         end
%     end
% end

set(gca,'ytick',0:16)
figure(3);
hold off;
for i=1:7
     subplot(2,4,i);
     plot( reshape( gg(i, roadid, 1:ceil(1680/cut) ), 1, ceil(1680/cut) ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
     hold on;
end

figure(4);
hold off;
for i=1:7
    plot( reshape( gg(i, roadid, 1:ceil(1680/cut) ), 1, ceil(1680/cut) ), 'color', [ 1-rem(floor(i/4),2) , 1-rem(floor(i/2),2) , 1-rem(i,2) ] );
    hold on;
    %disp( [r, i, sum( gg(i, r, timebegin:timeend ) ) ] );
end

figure(5);
for t=1:7
    f = zeros(1680);
    for i = 1:1680
        f(i) = g(t,roadid,i);
    end
    y = fft(f);
    %plot( abs(y) );
    for i = 1:1680
        if i>8
            y(i)=0;
        end
    end
    %plot( abs(y) );
    iy = real(ifft(y));
    %plot( iy )
    plot( iy , 'color', [ 1-rem(floor(t/4),2) , 1-rem(floor(t/2),2) , 1-rem(t,2) ] );
    hold on;
end

