fle_lst = []
for i in range(1,8):
    fle = open('flow090'+str(i)+'.txt','rb').read().split('\n')
    fle_lst.append(fle)
n = len(fle_lst[0])
outtxt = ""
for rdid in range(n):
    rdvalue = []
    for i in range(7):
        rdvalue.append(fle_lst[i][rdid].split(','))
    m = len(rdvalue[0])
    outtxt+=rdvalue[0][0]+','+rdvalue[0][1]
    for per in range(2,m):
        a = []
        for i in range(7):
            a.append(int(rdvalue[i][per]))
        a.sort()
        #print a
        newvalue = (a[2]+a[3]+a[4]+1)/3#mod 0,1->0;mod 2->+1
        outtxt+=','+str(newvalue)
    outtxt+='\n'
output = open('Allmeanflow.txt','w')
print >>output,outtxt
            
