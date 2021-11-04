#!/usr/bin/python3
# C.Hicks Nov 2021
# The Alan Turing Institute
# File prints out the static arrays needed for JCQRencoder.java
# Currently fixed to QR version 2

print('static final byte [] icon_bit_index = {', end='')
for n in range(2,80):
    print('{},{},{},{},{},{},{},{},'.format(n,n,n,n,n,n,n,n), end='')
print('80,80,80,80,80,80,80,80};')

