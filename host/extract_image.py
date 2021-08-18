#!/usr/bin/env python3 
# chicks 19/07/21 
import sys
from card.USIM import *
from card.SIM import *

u = SIM()
usim = USIM()

#services = u.get_services()
#for s in services:
#    print(s)

#u.dbg = 1
#u.explore_fs('sim_fs.txt')
#g = make_graph(u.FS)
#g.write_png('sim_fs.png')

MF              = [0x3F, 0x00]
DF_TELECOM      = [0x7F, 0x10]
EF_SMS          = [0x6F, 0x3C]
DF_GRAPHICS     = [0x5F, 0x50]
EF_IMG          = [0x4F, 0x20]

print("\n\nSelecting MF")
ret = usim.select(MF)
print(ret) 

print("\n\nSelecting DF_TELECOM")
ret = usim.select(DF_TELECOM)
print(ret) 

print("\n\nSelecting DF_GRAPHICS")
ret = usim.select(DF_GRAPHICS)
print(ret)    

print("\n\nSelecting EF_IMG (1)")
ret = usim.select(EF_IMG)
print(ret)
print(ret.keys())
print(ret['Data'])
u.disconnect()