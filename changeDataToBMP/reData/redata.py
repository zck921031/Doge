import os,sys
import Image

def getDataFromBMP(bmpFile,partID,data):
    pic = Image.open(bmpFile)
    picContext = pic.getdata()
    print pic.size,picContext[0][0]
    startID = 0
    midID = 1500*750/8
    endID = midID
    if partID!=1:
        startID = midID
        endID   = 155*1682
    COL = pic.size[0]
    ROW = pic.size[1]
    SX = 0
    SY = 0
    while picContext[20*COL+SX][0]>128: SX+=1
    while picContext[SY*COL+20][0]>128: SY+=1
    print SX,SY
    SX+=5
    SY+=5
    tmpID = startID
    bitpos = 0
    bits = 0
    while tmpID<endID:
        pix = (tmpID-startID)*8+bitpos
        r = pix/1500 + SY
        c = pix%1500 + SX
        value = picContext[r*COL+c][0]
        if value<128:
            bits |= 1<<bitpos
        bitpos+=1
        if bitpos>=8:
            tmpID+=1
            data.append(bits)
            bits=0
            bitpos=0
            
def buildTxt(data,outfile):
    n = len(data)
    if n!=1682*155:
        print "Bad data!!"
        return
    fle = open(outfile,'w')
    for i in range(155):
        lntxt = 'tl'+str(data[i*1682])+',tl'+str(data[i*1682+1])
        for j in range(2,1682):
            lntxt+=','+str(data[i*1682+j])
        print >>fle,lntxt
        

out0901=[]
getDataFromBMP('flow0901p1.bmp',1,out0901)
getDataFromBMP('flow0901p2.bmp',2,out0901)
buildTxt(out0901,'flow0901_XX.txt')

for i in range(1,8):
    out=[]
    getDataFromBMP('round2_090'+str(i)+'p1.bmp',1,out)
    getDataFromBMP('round2_090'+str(i)+'p2.bmp',2,out)
    buildTxt(out,'round2_090'+str(i)+'.txt')


