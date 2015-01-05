function [ ret ] = ReadFlowByFile( filename )
    ret = textread('C:\Users\zck\Documents\GitHub\Doge\StaticLoop\data\flow0901.txt','%s','delimiter',',');
    ret = reshape(ret,1682,155)';
end

