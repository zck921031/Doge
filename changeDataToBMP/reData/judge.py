fle1 = open('flow0901_XX.txt','rb').read().replace('\r\n','').replace('\n','')
fle2 = open('flow0901.txt','rb').read().replace('\r\n','').replace('\n','')
if cmp(fle1,fle2)==0:
    print 'AC'
else :
    print 'WA'
