package com.github.alan_turing_institute.HOTP_STK;

import javacard.framework.*;
import javacard.framework.Util;
import sim.toolkit.*;

/*
	2021, Alan Turing Institute.
*/

public class HOTP_STK extends Applet implements ToolkitInterface, ToolkitConstants {
	// DON'T DECLARE USELESS INSTANCE VARIABLES! They get saved to the EEPROM,
	// which has a limited number of write cycles.
	private byte otpMenuItem;
	
	static short hotpDigits = 7;
	static short hotpKeyLen = 16;
	static byte[] hotpKey = {1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6};

	static HMACgenerator hmacGen;

	static byte[] passcodeMsg = new byte[7];
	
	static byte[] menuItemText = new byte[] { 'N', 'e', 'w', ' ', 'P', 'a', 's', 's', 'c', 'o', 'd', 'e'};
	
	private HOTP_STK() {
		// This is the interface to the STK applet registry (which is separate
		// from the JavaCard applet registry!)
		ToolkitRegistry reg = ToolkitRegistry.getEntry();
		hmacGen = new HMACgenerator(hotpKey, hotpKeyLen, hotpDigits);
	
		// Define the applet Menu Entry
		otpMenuItem = reg.initMenuEntry(menuItemText, (short)0, (short)menuItemText.length,
				PRO_CMD_SELECT_ITEM, false, (byte)0, (short)0);
	}
	
	// This method is called by the card when the applet is installed. You must
	// instantiate your applet and register it here.
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		HOTP_STK applet = new HOTP_STK();
		applet.register();
	}
	
	// This processes APDUs sent directly to the applet. For STK applets, this
	// interface isn't really used.
	public void process(APDU arg0) throws ISOException {
		// ignore the applet select command dispached to the process
		if (selectingApplet())
			return;
	}

	// This processes STK events.
	public void processToolkit(byte event) throws ToolkitException {
		EnvelopeHandler envHdlr = EnvelopeHandler.getTheHandler();

		if (event == EVENT_MENU_SELECTION) {
			byte selectedItemId = envHdlr.getItemIdentifier();

			if (selectedItemId == otpMenuItem) {
				showOTP();
			}
		}
	}
	
	private void showOTP() {
		ProactiveHandler proHdlr = ProactiveHandler.getTheHandler();

		// Generate new OTP
		hmacGen.generateHotp(passcodeMsg);

		proHdlr.initDisplayText((byte)0, DCS_8_BIT_DATA, passcodeMsg, (short)0, 
				(short)(passcodeMsg.length));
		proHdlr.send();
		return;
	}
}