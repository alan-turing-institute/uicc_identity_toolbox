#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" pySim: Card programmation logic
"""

#
# Copyright (C) 2009-2010  Sylvain Munaut <tnt@246tNt.com>
# Copyright (C) 2011  Harald Welte <laforge@gnumonks.org>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

from pySim.utils import b2h, h2b, swap_nibbles, rpad, lpad


class Card(object):

	def __init__(self, scc):
		self._scc = scc

	def _e_iccid(self, iccid):
		return swap_nibbles(rpad(iccid, 20))

	def _e_imsi(self, imsi):
		"""Converts a string imsi into the value of the EF"""
		l = (len(imsi) + 1) // 2	# Required bytes
		oe = len(imsi) & 1			# Odd (1) / Even (0)
		ei = '%02x' % l + swap_nibbles(lpad('%01x%s' % ((oe<<3)|1, imsi), 16))
		return ei

	def _e_plmn(self, mcc, mnc):
		"""Converts integer MCC/MNC into 6 bytes for EF"""
		return swap_nibbles(lpad('%d' % mcc, 3) + lpad('%d' % mnc, 3))

	def reset(self):
		self._scc.reset_card()


class _MagicSimBase(Card):
	"""
	Theses cards uses several record based EFs to store the provider infos,
	each possible provider uses a specific record number in each EF. The
	indexes used are ( where N is the number of providers supported ) :
	 - [2 .. N+1] for the operator name
     - [1 .. N] for the programable EFs

	* 3f00/7f4d/8f0c : Operator Name

	bytes 0-15 : provider name, padded with 0xff
	byte  16   : length of the provider name
	byte  17   : 01 for valid records, 00 otherwise

	* 3f00/7f4d/8f0d : Programmable Binary EFs

	* 3f00/7f4d/8f0e : Programmable Record EFs

	"""

	@classmethod
	def autodetect(kls, scc):
		try:
			for p, l, t in kls._files.values():
				if not t:
					continue
				if scc.record_size(['3f00', '7f4d', p]) != l:
					return None
		except:
			return None

		return kls(scc)

	def _get_count(self):
		"""
		Selects the file and returns the total number of entries
		and entry size
		"""
		f = self._files['name']

		r = self._scc.select_file(['3f00', '7f4d', f[0]])
		rec_len = int(r[-1][28:30], 16)
		tlen = int(r[-1][4:8],16)
		rec_cnt = (tlen / rec_len) - 1;

		if (rec_cnt < 1) or (rec_len != f[1]):
			raise RuntimeError('Bad card type')

		return rec_cnt

	def program(self, p):
		# Go to dir
		self._scc.select_file(['3f00', '7f4d'])

		# Home PLMN in PLMN_Sel format
		hplmn = self._e_plmn(p['mcc'], p['mnc'])

		# Operator name ( 3f00/7f4d/8f0c )
		self._scc.update_record(self._files['name'][0], 2,
			rpad(b2h(p['name']), 32)  + ('%02x' % len(p['name'])) + '01'
		)

		# ICCID/IMSI/Ki/HPLMN ( 3f00/7f4d/8f0d )
		v = ''

			# inline Ki
		if self._ki_file is None:
			v += p['ki']

			# ICCID
		v += '3f00' + '2fe2' + '0a' + self._e_iccid(p['iccid'])

			# IMSI
		v += '7f20' + '6f07' + '09' + self._e_imsi(p['imsi'])

			# Ki
		if self._ki_file:
			v += self._ki_file + '10' + p['ki']

			# PLMN_Sel
		v+= '6f30' + '18' +  rpad(hplmn, 36)

		self._scc.update_record(self._files['b_ef'][0], 1,
			rpad(v, self._files['b_ef'][1]*2)
		)

		# SMSP ( 3f00/7f4d/8f0e )
			# FIXME

		# Write PLMN_Sel forcefully as well
		r = self._scc.select_file(['3f00', '7f20', '6f30'])
		tl = int(r[-1][4:8], 16)

		hplmn = self._e_plmn(p['mcc'], p['mnc'])
		self._scc.update_binary('6f30', hplmn + 'ff' * (tl-3))

	def erase(self):
		# Dummy
		df = {}
		for k, v in self._files.iteritems():
			ofs = 1
			fv = v[1] * 'ff'
			if k == 'name':
				ofs = 2
				fv = fv[0:-4] + '0000'
			df[v[0]] = (fv, ofs)

		# Write
		for n in range(0,self._get_count()):
			for k, (msg, ofs) in df.iteritems():
				self._scc.update_record(['3f00', '7f4d', k], n + ofs, msg)


class SuperSim(_MagicSimBase):

	name = 'supersim'

	_files = {
		'name' : ('8f0c', 18, True),
		'b_ef' : ('8f0d', 74, True),
		'r_ef' : ('8f0e', 50, True),
	}

	_ki_file = None


class MagicSim(_MagicSimBase):

	name = 'magicsim'

	_files = {
		'name' : ('8f0c', 18, True),
		'b_ef' : ('8f0d', 130, True),
		'r_ef' : ('8f0e', 102, False),
	}

	_ki_file = '6f1b'


class FakeMagicSim(Card):
	"""
	Theses cards have a record based EF 3f00/000c that contains the provider
	informations. See the program method for its format. The records go from
	1 to N.
	"""

	name = 'fakemagicsim'

	@classmethod
	def autodetect(kls, scc):
		try:
			if scc.record_size(['3f00', '000c']) != 0x5a:
				return None
		except:
			return None

		return kls(scc)

	def _get_infos(self):
		"""
		Selects the file and returns the total number of entries
		and entry size
		"""

		r = self._scc.select_file(['3f00', '000c'])
		rec_len = int(r[-1][28:30], 16)
		tlen = int(r[-1][4:8],16)
		rec_cnt = (tlen / rec_len) - 1;

		if (rec_cnt < 1) or (rec_len != 0x5a):
			raise RuntimeError('Bad card type')

		return rec_cnt, rec_len

	def program(self, p):
		# Home PLMN
		r = self._scc.select_file(['3f00', '7f20', '6f30'])
		tl = int(r[-1][4:8], 16)

		hplmn = self._e_plmn(p['mcc'], p['mnc'])
		self._scc.update_binary('6f30', hplmn + 'ff' * (tl-3))

		# Get total number of entries and entry size
		rec_cnt, rec_len = self._get_infos()

		# Set first entry
		entry = (
			'81' +								#  1b  Status: Valid & Active
			rpad(b2h(p['name'][0:14]), 28) +	# 14b  Entry Name
			self._e_iccid(p['iccid']) +			# 10b  ICCID
			self._e_imsi(p['imsi']) +			#  9b  IMSI_len + id_type(9) + IMSI
			p['ki'] +							# 16b  Ki
			lpad(p['smsp'], 80)					# 40b  SMSP (padded with ff if needed)
		)
		self._scc.update_record('000c', 1, entry)

	def erase(self):
		# Get total number of entries and entry size
		rec_cnt, rec_len = self._get_infos()

		# Erase all entries
		entry = 'ff' * rec_len
		for i in range(0, rec_cnt):
			self._scc.update_record('000c', 1+i, entry)

class GrcardSim(Card):
	"""
	Greencard (grcard.cn) HZCOS GSM SIM
	These cards have a much more regular ISO 7816-4 / TS 11.11 structure,
	and use standard UPDATE RECORD / UPDATE BINARY commands except for Ki.
	"""

	name = 'grcardsim'

	@classmethod
	def autodetect(kls, scc):
		return None

	def program(self, p):
		# We don't really know yet what ADM PIN 4 is about
		#self._scc.verify_chv(4, h2b("4444444444444444"))

		# Authenticate using ADM PIN 5
		self._scc.verify_chv(5, h2b("4444444444444444"))

		# EF.ICCID
		r = self._scc.select_file(['3f00', '2fe2'])
		data, sw = self._scc.update_binary('2fe2', self._e_iccid(p['iccid']))

		# EF.IMSI
		r = self._scc.select_file(['3f00', '7f20', '6f07'])
		data, sw = self._scc.update_binary('6f07', self._e_imsi(p['imsi']))

		# EF.ACC
		#r = self._scc.select_file(['3f00', '7f20', '6f78'])
		#self._scc.update_binary('6f78', self._e_imsi(p['imsi'])

		# EF.SMSP
		r = self._scc.select_file(['3f00', '7f10', '6f42'])
		data, sw = self._scc.update_record('6f42', 1, lpad(p['smsp'], 80))

		# Set the Ki using proprietary command
		pdu = '80d4020010' + p['ki']
		data, sw = self._scc._tp.send_apdu(pdu)

		# EF.HPLMN
		r = self._scc.select_file(['3f00', '7f20', '6f30'])
		size = int(r[-1][4:8], 16)
		hplmn = self._e_plmn(p['mcc'], p['mnc'])
		self._scc.update_binary('6f30', hplmn + 'ff' * (size-3))

		# EF.SPN (Service Provider Name)
		r = self._scc.select_file(['3f00', '7f20', '6f30'])
		size = int(r[-1][4:8], 16)
		# FIXME

		# FIXME: EF.MSISDN

	def erase(self):
		return

class SysmoSIMgr1(GrcardSim):
	"""
	sysmocom sysmoSIM-GR1
	These cards have a much more regular ISO 7816-4 / TS 11.11 structure,
	and use standard UPDATE RECORD / UPDATE BINARY commands except for Ki.
	"""
	name = 'sysmosim-gr1'

	# In order for autodetection ...

class SysmoUSIMgr1(Card):
	"""
	sysmocom sysmoUSIM-GR1
	"""
	name = 'sysmoUSIM-GR1'

	@classmethod
	def autodetect(kls, scc):
		# TODO: Access the ATR
		return None

	def program(self, p):
		# TODO: check if verify_chv could be used or what it needs
		# self._scc.verify_chv(0x0A, [0x33,0x32,0x32,0x31,0x33,0x32,0x33,0x32])
		# Unlock the card..
		data, sw = self._scc._tp.send_apdu_checksw("0020000A083332323133323332")

		# TODO: move into SimCardCommands
		par = ( p['ki'] +			# 16b  K
			p['opc'] +			# 32b  OPC
			self._e_iccid(p['iccid']) +	# 10b  ICCID
			self._e_imsi(p['imsi'])		#  9b  IMSI_len + id_type(9) + IMSI
			)
		data, sw = self._scc._tp.send_apdu_checksw("0099000033" + par)

	def erase(self):
		return

class TaisysSIMoMEVault(UsimCard, IsimCard):
	"""
	Taisys SIMoME VAULT
	"""

	name = 'Taisys-SIMoME-VAULT'

	def __init__(self, ssc):
		super(TaisysSIMoMEVault, self).__init__(ssc)
		self._scc.cla_byte = "00"
		self._scc.sel_ctrl = "0004" #request an FCP

	@classmethod
	def autodetect(kls, scc):
		try:
			# Try card model #1
			atr = "3B 9F 95 80 3F C3 A0 80 31 E0 73 FE 21 13 63 8D 43 42 83 F0 90 00 34"
			if scc.get_atr() == toBytes(atr):
				return kls(scc)

		except:
			return None
		return None

	def verify_adm(self, key):
		# authenticate as ADM using default key (written on the card..)
		if not key:
			raise ValueError("Please provide a PIN-ADM as there is no default one")
		(res, sw) = self._scc.verify_chv(0x0A, key)
		return sw

	def program(self, p):
		self.verify_adm(h2b(p['pin_adm']))

		# This type of card does not allow to reprogram the ICCID.
		# Reprogramming the ICCID would mess up the card os software
		# license management, so the ICCID must be kept at its factory
		# setting!
		if p.get('iccid'):
			print("Warning: Programming of the ICCID is not implemented for this type of card.")

		# select DF_GSM
		self._scc.select_path(['7f20'])

		# set Service Provider Name
		if p.get('name') is not None:
			self.update_spn(p['name'], True, True)

		# write EF.IMSI
		if p.get('imsi'):
			self._scc.update_binary('6f07', enc_imsi(p['imsi']))

		# EF.PLMNsel
		if p.get('mcc') and p.get('mnc'):
			sw = self.update_plmnsel(p['mcc'], p['mnc'])
			if sw != '9000':
				print("Programming PLMNsel failed with code %s"%sw)

		# EF.PLMNwAcT
		if p.get('mcc') and p.get('mnc'):
			sw = self.update_plmn_act(p['mcc'], p['mnc'])
			if sw != '9000':
				print("Programming PLMNwAcT failed with code %s"%sw)

		# EF.OPLMNwAcT
		if p.get('mcc') and p.get('mnc'):
			sw = self.update_oplmn_act(p['mcc'], p['mnc'])
			if sw != '9000':
				print("Programming OPLMNwAcT failed with code %s"%sw)

		# EF.HPLMNwAcT
		if p.get('mcc') and p.get('mnc'):
			sw = self.update_hplmn_act(p['mcc'], p['mnc'])
			if sw != '9000':
				print("Programming HPLMNwAcT failed with code %s"%sw)

		# EF.AD
		if (p.get('mcc') and p.get('mnc')) or p.get('opmode'):
			if p.get('mcc') and p.get('mnc'):
				mnc = p['mnc']
			else:
				mnc = None
			sw = self.update_ad(mnc=mnc, opmode=p.get('opmode'))
			if sw != '9000':
				print("Programming AD failed with code %s"%sw)

		# EF.SMSP
		if p.get('smsp'):
			r = self._scc.select_path(['3f00', '7f10'])
			data, sw = self._scc.update_record('6f42', 1, lpad(p['smsp'], 104), force_len=True)

		# EF.MSISDN
		# TODO: Alpha Identifier (currently 'ff'O * 20)
		# TODO: Capability/Configuration1 Record Identifier
		# TODO: Extension1 Record Identifier
		if p.get('msisdn') is not None:
			msisdn = enc_msisdn(p['msisdn'])
			content = 'ff' * 20 + msisdn

			r = self._scc.select_path(['3f00', '7f10'])
			data, sw = self._scc.update_record('6F40', 1, content, force_len=True)

		# EF.ACC
		if p.get('acc'):
			sw = self.update_acc(p['acc'])
			if sw != '9000':
				print("Programming ACC failed with code %s"%sw)

		# Populate AIDs
		self.read_aids()

		# update EF-SIM_AUTH_KEY (and EF-USIM_AUTH_KEY_2G, which is
		# hard linked to EF-USIM_AUTH_KEY)
		self._scc.select_path(['3f00'])
		self._scc.select_path(['a515'])
		if p.get('ki'):
			self._scc.update_binary('6f20', p['ki'], 1)
		if p.get('opc'):
			self._scc.update_binary('6f20', p['opc'], 17)

		# update EF-USIM_AUTH_KEY in ADF.ISIM
		data, sw = self.select_adf_by_aid(adf="isim")
		if sw == '9000':
			if p.get('ki'):
				self._scc.update_binary('af20', p['ki'], 1)
			if p.get('opc'):
				self._scc.update_binary('af20', p['opc'], 17)

			# update EF.P-CSCF in ADF.ISIM
			if self.file_exists(EF_ISIM_ADF_map['PCSCF']):
				if p.get('pcscf'):
					sw = self.update_pcscf(p['pcscf'])
				else:
					sw = self.update_pcscf("")
				if sw != '9000':
					print("Programming P-CSCF failed with code %s"%sw)


			# update EF.DOMAIN in ADF.ISIM
			if self.file_exists(EF_ISIM_ADF_map['DOMAIN']):
				if p.get('ims_hdomain'):
					sw = self.update_domain(domain=p['ims_hdomain'])
				else:
					sw = self.update_domain()

				if sw != '9000':
					print("Programming Home Network Domain Name failed with code %s"%sw)

			# update EF.IMPI in ADF.ISIM
			# TODO: Validate IMPI input
			if self.file_exists(EF_ISIM_ADF_map['IMPI']):
				if p.get('impi'):
					sw = self.update_impi(p['impi'])
				else:
					sw = self.update_impi()
				if sw != '9000':
					print("Programming IMPI failed with code %s"%sw)

			# update EF.IMPU in ADF.ISIM
			# TODO: Validate IMPU input
			# Support multiple IMPU if there is enough space
			if self.file_exists(EF_ISIM_ADF_map['IMPU']):
				if p.get('impu'):
					sw = self.update_impu(p['impu'])
				else:
					sw = self.update_impu()
				if sw != '9000':
					print("Programming IMPU failed with code %s"%sw)

		data, sw = self.select_adf_by_aid(adf="usim")
		if sw == '9000':
			# update EF-USIM_AUTH_KEY in ADF.USIM
			if p.get('ki'):
				self._scc.update_binary('af20', p['ki'], 1)
			if p.get('opc'):
				self._scc.update_binary('af20', p['opc'], 17)

			# update EF.EHPLMN in ADF.USIM
			if self.file_exists(EF_USIM_ADF_map['EHPLMN']):
				if p.get('mcc') and p.get('mnc'):
					sw = self.update_ehplmn(p['mcc'], p['mnc'])
					if sw != '9000':
						print("Programming EHPLMN failed with code %s"%sw)

			# update EF.ePDGId in ADF.USIM
			if self.file_exists(EF_USIM_ADF_map['ePDGId']):
				if p.get('epdgid'):
					sw = self.update_epdgid(p['epdgid'])
				else:
					sw = self.update_epdgid("")
				if sw != '9000':
					print("Programming ePDGId failed with code %s"%sw)

			# update EF.ePDGSelection in ADF.USIM
			if self.file_exists(EF_USIM_ADF_map['ePDGSelection']):
				if p.get('epdgSelection'):
					epdg_plmn = p['epdgSelection']
					sw = self.update_ePDGSelection(epdg_plmn[:3], epdg_plmn[3:])
				else:
					sw = self.update_ePDGSelection("", "")
				if sw != '9000':
					print("Programming ePDGSelection failed with code %s"%sw)


			# After successfully programming EF.ePDGId and EF.ePDGSelection,
			# Set service 106 and 107 as available in EF.UST
			# Disable service 95, 99, 115 if ISIM application is present
			if self.file_exists(EF_USIM_ADF_map['UST']):
				if p.get('epdgSelection') and p.get('epdgid'):
					sw = self.update_ust(106, 1)
					if sw != '9000':
						print("Programming UST failed with code %s"%sw)
					sw = self.update_ust(107, 1)
					if sw != '9000':
						print("Programming UST failed with code %s"%sw)

				sw = self.update_ust(95, 0)
				if sw != '9000':
					print("Programming UST failed with code %s"%sw)
				sw = self.update_ust(99, 0)
				if sw != '9000':
					print("Programming UST failed with code %s"%sw)
				sw = self.update_ust(115, 0)
				if sw != '9000':
					print("Programming UST failed with code %s"%sw)

		return

_cards_classes = [ FakeMagicSim, SuperSim, MagicSim, GrcardSim,
		   SysmoSIMgr1, SysmoUSIMgr1, TaisysSIMoMEVault ]
