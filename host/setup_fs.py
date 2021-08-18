#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Chris Hicks 16-9-21
# See pySim doc at https://ftp.osmocom.org/docs/latest/osmopysim-usermanual.pdf
# 1. Writes DF_GRAPHICS (5f50), EF_IMG (4f20) and EF_INSTANCE (4f01) to a Sysmocom SJ-A2
# 2. Inserts EF_INSTANCE entry into EF.IMG
from pySim.transport.pcsc import PcscSimLink
from pySim.card_handler import card_handler
from pySim.cards import card_detect, Card
from pySim.commands import SimCardCommands
from pySim.utils import h2b, sanitize_pin_adm
from pySim.filesystem import CardMF, RuntimeState, CardDF, CardADF
from pySim.ts_51_011 import CardProfileSIM, DF_TELECOM, DF_GSM
from pySim.ts_102_221 import CardProfileUICC, CardCommandSet
from pySim.ts_31_102 import CardApplicationUSIM
from pySim.ts_31_103 import CardApplicationISIM

FID_MF = '3f00'
FID_DF_TELECOM = '7f10'
FID_DF_GRAPHICS = '5f50'
FID_EF_IMG = '4f20'
FID_EF_INSTANCE = '4f01'
FID_EF_INSTANCE2 = '4f02'

# See ETSI TS 102 222 V15.0.0 e.g. Table 3: Coding of the data field of the CREATE FILE command (in case of creation of a DF/ADF)
create_df_graphics = '82027821' 					 	# Tag File Descriptor, length, FD = '78' (DF), data coding
create_df_graphics = create_df_graphics + '83025F50' 	# Tag File ID, length, File ID: DF_GRAPHICS = 5F50
create_df_graphics = create_df_graphics + '8A0105' 	 	# Tag LCSI, length, LCSI = 0x05 (activated)
create_df_graphics = create_df_graphics + '8B032F0601'	# Tag Security Attributes Referenced (8B), length, attributes: EF_ARR FID, record number
create_df_graphics = create_df_graphics + '81020000'	# Tag Total DF size, length, total file size 
create_df_graphics = create_df_graphics + 'C609'		# Tag PIN Status Template DO, length
create_df_graphics = create_df_graphics + '900180'		# Tag PS_DO, len, PS_DO - bit indicates which following pin(s) enabled
create_df_graphics = create_df_graphics + '950100'		# Tag Usage Qualifier for PS_DO, length, usage qualifier
create_df_graphics = create_df_graphics + '83010a'		# Tag PIN, length, PIN reference
create_df_graphics = create_df_graphics + '830101'		# ...
create_df_graphics = '62' + '{:02x}'.format(len(create_df_graphics)//2) + create_df_graphics

# See ETSI TS 102 222 V15.0.0 e.g. Table 4: Coding of the data field of the CREATE FILE command (in case of the creation of an EF)
create_ef_img = '82044221000A' 					# Tag File Descriptor, length, FD = '42' (linear fixed EF), data coding ='21' (const.), record length, n_records
create_ef_img = create_ef_img + '83024F20' 		# Tag File ID, length, File ID: EF_IMG = 4F20
create_ef_img = create_ef_img + '8A0105' 	 	# Tag LCSI, length, LCSI = 0x05 (activated)
create_ef_img = create_ef_img + '8B032F0602'	# Tag Security Attributes Referenced (8B), length, attributes: EF_ARR FID, record number
create_ef_img = create_ef_img + '80020028'		# Tag Reserved file size, length, total file size = n_records*record_len i.e. 4*10
create_ef_img = create_ef_img + '8800'			# SFI (Short File Identifier), length, SFI
create_ef_img = '62' + '{:02x}'.format(len(create_ef_img)//2) + create_ef_img

ef_instance_data = '2E280000000000000001FF80'\
				   '0000000FFF0000000077FE00'\
				   '000001BFF800000006FFE000'\
				   '00001A03800000006BF6BC00'\
				   '0001AFD838000006BF602000'\
				   '001AFD804000006BF6008000'\
				   '01A01F02000006FFE4040000'\
				   '1BFF901000006DEE40400001'\
				   'BFF90100006FFFE40400001B'\
				   'FF901000006FFE40400001BF'\
				   'F901000006FFE60400001BFF'\
				   '881000006FFE20400001BFF8'\
				   '66000006FFE0F000001BFF80'\
				   '8000007FFE00000003000C00'\
				   '00001FFFF800000000000000'\
				   '000000000000000000000000'\
				   '1C210844EE0048C431922001'\
				   '251145508007144515438012'\
				   '711C4D08004A2489322001C8'\
				   '9E244EE0'

ef_instance_data2 = '0505FEEBBFFFFFFF'

ef_img_record_1 = '01' + '2E' + '28' + '11' + FID_EF_INSTANCE + '0000' + '{:04x}'.format(len(ef_instance_data)//2)

ef_img_record_2 = '01' + '05' + '05' + '11' + FID_EF_INSTANCE2 + '0000' + '{:04x}'.format(len(ef_instance_data2)//2)

# See ETSI TS 131 102 V16.7.0 4.6.12 Image Instance Data Files for file atributes
# 4FXX, transparent EF. From TS 102 221, FD = 0b01000001 = 0x41
create_ef_instance = '82024121' 						# Tag File Descriptor, length, FD = '41' (transparent EF), data coding ='21' (const.)
create_ef_instance = create_ef_instance + '8302' + FID_EF_INSTANCE 	# Tag File ID, length, File ID
create_ef_instance = create_ef_instance + '8A0105' 	 	# Tag LCSI, length, LCSI = 0x05 (activated)
create_ef_instance = create_ef_instance + '8B032F0602'	# Tag Security Attributes Referenced (8B), length, attributes: EF_ARR FID, record number
create_ef_instance = create_ef_instance + '800200E8'	# Tag Reserved file size, length, total image file size 
create_ef_instance = create_ef_instance + '8800'		# SFI (Short File Identifier), length, SFI
create_ef_instance = '62' + '{:02x}'.format(len(create_ef_instance)//2) + create_ef_instance

create_ef_instance2 = create_ef_instance[0:16] + FID_EF_INSTANCE2 + create_ef_instance[20:40] + '{:04x}'.format(len(ef_instance_data2)//2)
create_ef_instance2 = create_ef_instance2 + create_ef_instance[44:48]


def main():

	reader_no = 0
	pin_adm1 = '98877146'

	sl = PcscSimLink(reader_no)

	# Create command layer
	scc = SimCardCommands(transport=sl)

	sl.wait_for_card();

	cardhandler = card_handler(sl)

	card = card_detect("auto", scc)
	if card is None:
		print("No card detected!")
		sys.exit(-1)

	profile = CardProfileUICC()
	profile.add_application(CardApplicationUSIM)
	profile.add_application(CardApplicationISIM)

	rs = RuntimeState(card, profile)
	sl.set_sw_interpreter(rs) # inform the transport that we can do context-specific SW interpretation

	rs.mf.add_file(DF_TELECOM())
	rs.mf.add_file(DF_GSM())

	# Use PIN ADM1 
	pin = sanitize_pin_adm(pin_adm1)
	if pin:
		try:
			card.verify_adm(h2b(pin))
		except Exception as e:
			print(e)

	print()

	df_graphics_check = scc.try_select_path([FID_MF, FID_DF_TELECOM, FID_DF_GRAPHICS])
	resp = sl.send_apdu_checksw(scc.cla_byte + "a4" + scc.sel_ctrl + "02" + FID_MF) #scc.select_file(FID_MF)
	resp = scc.select_file(FID_DF_TELECOM)
	
	if df_graphics_check[-1][1] != '9000':
		print('Error: DF.GRAPHICS not found at {}'.format(FID_DF_GRAPHICS))
		print('Attempting to Create DF.GRAPHICS...')

		# Using ETSI TS 102 222 V15.0.0 (2018-06)
		# CREATE FILE: CLA INS='E0' P1='00' P2='00' data_length data '{:02x}'.format(len(create_df_graphics)//2)
		sl.send_apdu_checksw(scc.cla_byte + "e0" + '0000' + '{:02x}'.format(len(create_df_graphics)//2) + create_df_graphics)
		# File is NOT automatically selected after creation
		
	resp = scc.select_file(FID_DF_GRAPHICS)
	df_img_check = scc.try_select_path([FID_EF_IMG])
	if df_img_check[-1][1] != '9000':
		print('Error: EF.IMG not found at {}'.format(FID_EF_IMG))
		print('Attempting to Create EF.IMG...')
		sl.send_apdu_checksw(scc.cla_byte + "e0" + '0000' + '{:02x}'.format(len(create_ef_img)//2) + create_ef_img)


	df_img_instance_check = scc.try_select_path([FID_EF_INSTANCE])
	if df_img_instance_check[-1][1] != '9000':
		print('Error: EF.INSTANCE not found at {}'.format(FID_EF_INSTANCE))
		print('Attempting to Create sample EF.INSTANCE at {}...'.format(FID_EF_INSTANCE))
		sl.send_apdu_checksw(scc.cla_byte + "e0" + '0000' + '{:02x}'.format(len(create_ef_instance)//2) + create_ef_instance)

	df_img_instance_check = scc.try_select_path([FID_EF_INSTANCE2])
	if df_img_instance_check[-1][1] != '9000':
		print('Error: EF.INSTANCE not found at {}'.format(FID_EF_INSTANCE2))
		print('Attempting to Create sample EF.INSTANCE at {}...'.format(FID_EF_INSTANCE2))
		sl.send_apdu_checksw(scc.cla_byte + "e0" + '0000' + '{:02x}'.format(len(create_ef_instance2)//2) + create_ef_instance2)


	print('Writing EF.INSTANCE data to {}...'.format(FID_EF_INSTANCE))
	resp = scc.update_binary(FID_EF_INSTANCE, ef_instance_data, offset=0, verify=True)

	print('Writing EF.INSTANCE data to {}...'.format(FID_EF_INSTANCE2))
	resp = scc.update_binary(FID_EF_INSTANCE2, ef_instance_data2, offset=0, verify=True)

	print('Patching EF.IMG...')
	resp = scc.update_record(FID_EF_IMG, 1, ef_img_record_1, verify=True)
	resp = scc.update_record(FID_EF_IMG, 2, ef_img_record_2, verify=True)

	print('Done! :)')


if __name__ == '__main__':
	main()