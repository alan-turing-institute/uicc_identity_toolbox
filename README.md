# UICC Digital Identity Toolbox
This repository comprises a framework of Java Card applets which provide trustworthy enhancements to digital identity platforms. 

The aims of this framework are to provide a suite of tools for improving the security, privacy and reliability of authentication options for foundational national identity platforms. These identity platforms, which can now be found throughout many parts of the developing world, are constrained such that few assumptions should be made about the resources available to their beneficiaries and users. In particular, legacy and low-cost mobile phones are more prevalent than smartphones and there are many areas wirthout reliable access to cellular or terrestrial digital network operators. In light of these constraints, these tools provide several mechanisms by which low-cost mobile phones can be utilised as authentication tokens within foundational identity platforms. These tools operate despite limited or absent network connectivity and support a wide range of low-cost mobile handsets. 

At the moment there are two main components:
* A [text_based_applet](https://github.com/alan-turing-institute/hotp_stk/tree/main/text_based_applet) which provides [RFC-4226](https://datatracker.ietf.org/doc/html/rfc4226) HMAC-Based One-Time Passwords (HOTP) on even the most basic handsets.
* A [QR_based_applet](https://github.com/alan-turing-institute/hotp_stk/tree/main/QR_based_applet) that allows many low-cost mobile devvices to display standard machine-readable Quick Response (QR) codes for authenticating their identity.


# Requirements

Tested with:
* [sysmoISIM-SJA2](http://shop.sysmocom.de/products/sysmoISIM-SJA2) with ADM keys. Do not unlock the card.
* Nokia 106 Feature Phone, TTfone TT240 Smart-Feature Phone, Alcatel 'Pixi 3(3.5)' 4009X Android smartphone.
* PCSC card reader e.g. Gemalto IDBridge CT30
* OpenJDK 11
* Debian bullseye and OSX X 10.15.7
* Python 2.7.18
* The pycrypto and pyscard libraries
```
pip2 install --user pycrypto pyscard
```

Recommended:
* FF to 2FF smartcard converter or equivalent for your development device, [for example](https://www.aliexpress.com/item/32769577127.html?spm=a2g0s.9042311.0.0.5b4b4c4d68yrxs).


# Pre-Requisites

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


# Credits and Gratitude

* We are very thankful for @mrlnc's [HelloSTK2 repository](https://github.com/mrlnc/HelloSTK2) which made getting STK to work on the sysmoISIM-SJA2 a walk in the park!
* We are grateful to @petr's [HOTP via NDEF on JavaCard](https://github.com/petrs/hotp_via_ndef) implementation which provides the HOTP code we first imported.
* Big thank you to @herlesupreeth for the [sim-tools](https://github.com/herlesupreeth/sim-tools.git) fork. This fork adds support for the Sysmocom sysmoISIM-SJA2 ISIM.
* The `sim` library comes from [3GPP TS43.019](http://www.3gpp.org/ftp/Specs/archive/43_series/43.019/43019-560.zip), see [this Stack Overflow comment](https://stackoverflow.com/a/22471187)
* This work was supported, in whole or in part, by the Bill & Melinda Gates Foundation [INV-001309].
