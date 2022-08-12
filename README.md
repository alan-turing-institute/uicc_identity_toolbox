# UICC Digital Identity Toolbox
This repository comprises a toolbox of Java Card applets which provide trustworthiness enhancements, based on low-cost and legacy mobile devices, to foundational digital identity platforms. 

The aim of this repository is to provide a suite of tools for improving the security, privacy and reliability of authentication mechanisms for foundational national identity platforms. These identity platforms, which can now be found throughout many parts of the developing world, are constrained such that few assumptions should be made about the resources available to their beneficiaries and users. In particular, legacy and low-cost mobile phones are more prevalent than smartphones and there are many areas wirthout reliable access to cellular network operators. In light of these constraints, these tools provide a suite of mechanisms by which low-cost mobile phones can be utilised as authentication tokens within foundational identity platforms. These tools operate despite limited or absent network connectivity and support a wide range of low-cost mobile handsets. 

At the moment there are two applets:
* A [QR Code Applet](https://github.com/alan-turing-institute/SIMple-ID) that allows many low-cost mobile devvices to display standard machine-readable Quick Response (QR) codes for authenticating their identity.
* A [Text Applet](https://github.com/alan-turing-institute/hotp_stk/tree/main/text_based_applet) which provides [RFC-4226](https://datatracker.ietf.org/doc/html/rfc4226) HMAC-Based One-Time Passwords (HOTP) on even the most basic handsets.

We are also working towards sound-based and Near-Field Contact (NFC) based solutions which will be added here in due course.

# Gratitude

* This work was supported, in whole or in part, by the Bill & Melinda Gates Foundation [INV-001309].
