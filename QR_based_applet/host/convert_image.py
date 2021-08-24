#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Chris Hicks 19-8-21
# Creates QR images in ETSI TS 131 102 format for display using Proactive (U)SIM commands
#
# Dependencies:
#   qrcode 6.1  https://pypi.org/project/qrcode/
#   matplotlib  https://pypi.org/project/matplotlib/
#   numpy       https://pypi.org/project/numpy/

import qrcode
import matplotlib.pyplot as plt
import base64
import math
from math import sqrt, pi, exp
import string
import random
import json
import numpy as np
from PIL import Image


'''
Generate a QR code as a np.array of booleans
	- also displays the code and saves it as an image to disk
'''
def generate_qr(qr_code_filename):

    # Define QR code specification
    qr = qrcode.QRCode(
        version=2,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=1,
        border=1,
    )

    with open("qr.json", "r") as read_file:
        qr_data = json.load(read_file)

    qr.add_data(qr_data)

    try:
        qr.make(fit=True)
        print('QR Version: {}'.format(qr.version))
    except qrcode.exceptions.DataOverflowError:
        print('User data too big for QR code.')
        return False

    import matplotlib.pyplot as plt

    qr_img = qr.make_image(fill_color="black", back_color="white")
    qr_array = np.array(qr_img)
    img = Image.fromarray(qr_array)
    img.save(qr_code_filename)
    img.show()

    print('Example QR code output to {}'.format(qr_code_filename))
    print('Done.')
    return qr_array


if __name__ == '__main__':

	qr_code_filename = "STK_qr.png"
	qr_array = generate_qr(qr_code_filename)

	# ETSI TS 131 102 raster icons
	# The status of each raster image point is coded in one bit, to indicate whether the point is set (status = 1) or not set (status = 0)
	# Unused bits shall be set to 1.

	x_size = qr_array.shape[0]
	y_size = qr_array.shape[1]
	qr_array = qr_array.flatten()
	print('QR size (bits): {}'.format(qr_array.size))

	raster_remainder = qr_array.size%8
	raster_bytes = qr_array.size//8
	if raster_remainder > 0:
		raster_bytes += 1

	raster_bits = ['1']*raster_bytes*8

	for b in range(0, len(raster_bits)):
		try:
			if qr_array[b] == False:
				raster_bits[b] = '0'
		except IndexError as e:
			break;

	corrected_raster_bits = ''
	raster_bits = ''.join(raster_bits)

	for by_idx in range(raster_bytes):
		little_end_byte = raster_bits[by_idx*8:(by_idx*8)+8]
		corrected_raster_bits += '{:02x}'.format(int(little_end_byte,2))

	print('Image hex string:')
	print('{:02x}'.format(x_size) + '{:02x}'.format(y_size) + corrected_raster_bits)

	quit()
