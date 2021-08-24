# QR codes using UICC API

Experimental, have fun...

The latest applet will display a basic 'hello world' QR code on the ttFone 240 and the Alcatel Pixi 3. The QR code is 27x27 pixels and can be read using a smartphone camera.

Obviously lots to do here! but the very basics are at least in place.

![photo1629720537](https://user-images.githubusercontent.com/10000317/130613129-770badb6-1bf4-4f28-9e85-6d3f4e72b975.jpeg)


# Build and Install

You first need to prep your sysmoISIM-SJA2 Java Card by installing the necessary icon files (and their supporting directories and filesystem components) onto the card. To do this, open `host/setup_fc.py` and insert the `ADM1` pin for your specific card on line 107. __Note: if you use the wrong ADM PIN you may brick your card and be unable to continue with this process.__

Once updated, make sure your Java Card is inserted in the reader in your machine and available (e.g., by running `pcsctest` and verifying the output corresponding to your reader number). If necessary update the `reader_no` on line 106.

Run:

```
python3 setup_fs.py
```

Only continue if the script completes successfully. Now that the filesystem is configured, build and install the applet as follows.]

First, build the applet by running

```
ant
```

Next, load and install the applet onto your Java Card. It is vitally important that you replace `KIC1` and `KID1` with the specific keys for your card. These are provided at the time of purchase and enable the Over The Air (OTA) security needed for loading applets to your card.

```
python2 ../sim-tools/shadysim/shadysim_isim.py --pcsc \
      -l ./bin/QRSTK.cap \
      -i ./bin/QRSTK.cap \
      --kic KIC1 \
      --kid KID1 \
      --instance-aid f07002CA44900101 \
      --module-aid f07002CA44900101 \
      --nonvolatile-memory-required 01ff \
      --volatile-memory-for-install 01ff \
      --enable-uicc-toolkit \
      --enable-uicc-file-access \
      --access-domain 020101 \
      --max-menu-entry-text 20 \
      --max-menu-entries 06
```

Note the additional parameters used for this applet : `--enable-uicc-toolkit` and `--enable-uicc-file-access`. 

To uninstall the applet, again replace `KIC1` and `KID1` with your card keys and then run the following.

```
python2 ../sim-tools/shadysim/shadysim_isim.py --pcsc \
      --kic KIC1 \
      --kid KID1 \
      -d f07002CA44
```

# Credits and Gratitude

 We are very thankful for @mrlnc's [HelloSTK2 repository](https://github.com/mrlnc/HelloSTK2) which made getting STK to work on the sysmoISIM-SJA2 a walk in the park!
* We are grateful to @petr's [HOTP via NDEF on JavaCard](https://github.com/petrs/hotp_via_ndef) implementation which provides the HOTP code we first imported.
* Big thank you to @herlesupreeth for the [sim-tools](https://github.com/herlesupreeth/sim-tools.git) fork. This fork adds support for the Sysmocom sysmoISIM-SJA2 ISIM.
* The `UICC API` library comees from [ETSI TS 102 241](https://www.etsi.org/deliver/etsi_ts/102200_102299/102241/17.01.00_60/).
* This work was supported, in whole or in part, by the Bill & Melinda Gates Foundation [INV-001309].
