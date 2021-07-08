import sys
from card.USIM import *
from card.SIM import *

u = SIM()

#u = USIM()
#print("u.AID", u.AID)
#print("u.get_imsi()",  u.get_imsi())
#print("u.interpret_AID(u.USIM_AID)",  u.interpret_AID(u.USIM_AID))


services = u.get_services()
for s in services:
    print(s)


u.dbg = 1
u.explore_fs('sim_fs.txt')
#g = make_graph(u.FS)
#g.write_png('sim_fs.png')


MF              = [0x3F, 0x00]
DF_TELECOM      = [0x7F, 0x10]
EF_SMS          = [0x6F, 0x3C]
DF_GRAPHICS     = [0x5F, 0x50]
EF_IMG          = [0x4F, 0x20]

print("\n\nSelecting MF")
ret = u.select(MF)
for k in ret:
    print(ret[k])

print("\n\nSelecting DF_TELECOM")
ret = u.select(DF_TELECOM)
for k in ret:
    print(ret[k])

print("\n\nSelecting EF_IMG (1)")
ret = u.select(EF_IMG)
print(ret)
u.disconnect()