function [ ret ] = ReadFlowByFile( )
    g = zeros(7,155,1680);
    for i=1:7
        %['flow090' int2str(i)  '.csv']
        g(i,:,:) = csvread( ['flow090' int2str(i)  '.csv'] );
    end
    ret = g;
end

