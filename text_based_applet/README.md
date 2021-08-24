# HOTP using STK
This is a [SimToolKit (STK)](http://www.bladox.cz/devel-docs/gen_stk.html) implementation of the [RFC-4226](https://datatracker.ietf.org/doc/html/rfc4226) HMAC-Based One-Time Password (HOTP) authentication algorithm for sysmoISIM-SJA2 SIM cards. 

Note: The current implementation is a concept re-risking demo and comes with no assurances of functionality, standards comformity or safety.

https://user-images.githubusercontent.com/10000317/123816745-77957900-d8ef-11eb-8610-fd559349d5ac.mov

# Requirements

Tested with:
* [sysmoISIM-SJA2](http://shop.sysmocom.de/products/sysmoISIM-SJA2) with ADM keys. Do not unlock the card.
* Nokia 106 Feature Phone and TTfone TT240 Smart-Feature Phone
* PCSC card reader e.g. Gemalto IDBridge CT30
* OpenJDK 11
* Debian bullseye and OSX X 10.15.7
* Python 2.7.18
* The pycrypto and pyscard libraries
```
pip2 install --user pycrypto pyscard
```

Recommended:
* FF to 2FF smartcard converter, or equivalent for your development device, [for example](https://www.aliexpress.com/item/32769577127.html?spm=a2g0s.9042311.0.0.5b4b4c4d68yrxs).

# Build

Ensure you have OpenJDK 11, the `ant` build tool and configure JDK 11 as your Java environment:
```
 sudo apt install openjdk-11-jdk ant
 sudo update-alternatives --config java
```
or
```
brew install openjdk@11 ant
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
```

Clone this repository:
```
git clone https://github.com/alan-turing-institute/hotp_stk.git
cd hotp_stk
```

Clone the Javacard SDK dependencies:
```
git submodule update --init --recursive
```

Then run `ant` to build the Java Card applet, HOTP_STK.cap. 


# Install

Download the `sim-tools` fork by @herlesupreeth. This fork adds support for the Sysmocom sysmoISIM-SJA2 ISIM cards to the [Osmocom sim-tools package](git.osmocom.org/sim/sim-tools/).

```
cd ..
git clone https://github.com/herlesupreeth/sim-tools.git
cd hotp_stk
```

Load and install the STK applet. It is vitally important that you replace `KIC1` and `KID1` with the specific keys for your Java Card. These are provided at the time of purchase and enable the Over The Air (OTA) security needed for loading STK applets to your card.

```
python2 ../sim-tools/shadysim/shadysim_isim.py --pcsc \
      -l ./bin/HOTP_STK.cap \
      -i ./bin/HOTP_STK.cap \
      --kic KIC1 \
      --kid KID1 \
      --instance-aid d07002CA44900101 \
      --module-aid d07002CA44900101 \
      --nonvolatile-memory-required 00ff \
      --volatile-memory-for-install 00ff \
      --enable-sim-toolkit \
      --max-menu-entry-text 15 \
      --max-menu-entries 02 
```

To uninstall the STK applet, again replace `KIC1` and `KID1` with your card keys and then run the following.

```
python2 ../sim-tools/shadysim/shadysim_isim.py --pcsc -d d07002cA44\
      --kic KIC --kid KID
```

# Credits and Gratitude

* We are very thankful for @mrlnc's [HelloSTK2 repository](https://github.com/mrlnc/HelloSTK2) which made getting STK to work on the sysmoISIM-SJA2 a walk in the park!
* We are grateful to @petr's [HOTP via NDEF on JavaCard](https://github.com/petrs/hotp_via_ndef) implementation which provides the HOTP code we first imported.
* The `sim` library comes from [3GPP TS43.019](http://www.3gpp.org/ftp/Specs/archive/43_series/43.019/43019-560.zip), see [this Stack Overflow comment](https://stackoverflow.com/a/22471187)
* This work was supported, in whole or in part, by the Bill & Melinda Gates Foundation [INV-001309].
