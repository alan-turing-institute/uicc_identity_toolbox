package QRSTK;
// Chris Hicks 
// The Alan Turing Institute
// Aug. 2021

import uicc.system.*;
import uicc.access.*;
import uicc.access.fileadministration.*; 
import uicc.toolkit.*;
import uicc.toolkit.ToolkitConstants.*;
import javacard.framework.*;

public class QRSTK extends Applet implements ToolkitInterface
{

	// Dedicated FileView objects
   	AdminFileView fvEF_DF_TELECOM;
	AdminFileView fvEF_DF_GRAPHICS;

	// Elementary FileView objects 
	FileView fvEF_SUME;
	FileView fvEF_IMG;
	FileView fvEF_INSTANCE;

	// FIDs
	static final short FID_EF_SUME 		= (short)0x6F54;
	static final short FID_EF_IMG  		= (short)0x4F20;
	static final short FID_EF_INSTANCE  = (short)0x4F06;

	private byte [] ef_img_record = { 
		(byte)0x01, (byte)0x2E, (byte)0x28, (byte)0x11, (byte)0x4F, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xE8, (byte)0xFF, (byte)0xFF,
		(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF
	};

	// Title(s) of the STK menu item(s) 
	private static byte [] menuTitle = {(byte)'C', (byte)'h', (byte)'a', (byte)'n', (byte)'g', (byte)'e', 
		(byte)' ', (byte)'t', (byte)'i', (byte)'t',(byte)'l',  (byte)'e'};

	private static byte [] menuTitle2 = {(byte)'S', (byte)'h', (byte)'o', (byte)'w', (byte)' ', (byte)'I', 
		(byte)'D', (byte)' ', (byte)'c', (byte)'o',(byte)'d',  (byte)'e'};

	private static byte [] menuTitle3 = {(byte)'S', (byte)'h', (byte)'o', (byte)'w', (byte)' ', (byte)'I', 
		(byte)'D', (byte)' ', (byte)'c', (byte)'o',(byte)'d',  (byte)'e', (byte)' ',  (byte)'2'};

	private static byte [] menuTitle4 = {(byte)'S', (byte)'h', (byte)'o', (byte)'w', (byte)' ', (byte)'I', 
		(byte)'D', (byte)' ', (byte)'c', (byte)'o',(byte)'d',  (byte)'e', (byte)' ',  (byte)'3'};

	private static byte [] menuTitle5 = {(byte)'N', (byte)'e', (byte)'w', (byte)' ', (byte)'Q', (byte)'R', 
		(byte)' ', (byte)'c', (byte)'o',(byte)'d',  (byte)'e', (byte)' '};


	private static byte [] id_string = {(byte)'I', (byte)'D', (byte)' ', (byte)' '};

	private static byte [] timing_string = {(byte)' ', (byte)' ', (byte)' ', (byte)' ', (byte)' ', (byte)' ', (byte)' '};

	private static byte [] qr_message = {(byte)'h', (byte)'e', (byte)'l', (byte)'l', (byte)'o', (byte)' ', (byte)'c', (byte)'h', (byte)'r', (byte)'i', (byte)'s'};


  	// Text shown to the user when changing menu title file
	private static byte [] insertNewTitle_text = {(byte)'I', (byte)'n', (byte)'s', (byte)'e', (byte)'r', (byte)'t',
		(byte)' ',(byte)'n',(byte)'e',(byte)'w',(byte)' ', (byte)'t', (byte)'i', (byte)'t',(byte)'l',  (byte)'e'};	
  	
	// Template for the resize command
  	private static byte [] resizeCommand =  {(byte) 0x83, (byte) 0x02, (byte) 0x6F, (byte) 0x54,
											 (byte) 0x80, (byte) 0x02, (byte) 0x00, (byte) 0x00 };
	
	// Display icon TAG, length, iconQualifier, iconRef in EF_IMG	
	// iconQualifier: 'bit 1 = 0': icon is self-explanatory, i.e. if displayed, it replaces the alpha identifier or text string								
	private static byte [] iconTLV =  {(byte)0x1E, (byte)0x02, (byte)0x00, (byte)0x02};
	private static byte [] iconTLV2 = {(byte)0x1E, (byte)0x02, (byte)0x00, (byte)0x01};
	private static byte [] iconTLV3 = {(byte)0x1E, (byte)0x02, (byte)0x00, (byte)0x03};

	// Offset in the resize command template for the file length
	static final short FILE_LENGTH_OFFSET = (short)6;

	// Reference to the toolkit registry
	private static ToolkitRegistry tkRegistry;

	// Reference to an Edit Handler object, used to execute filesystem commands
	private EditHandler editHandler;

	private byte menuItem1;
	private byte menuItem2;
	private byte menuItem3;
	private byte menuItem4;
	private byte menuItem5;

	static final byte DTQ_HIGH_PRIORITY = (byte)0x01;
	static final byte DTQ_WAIT_FOR_USER = (byte)0x80;
	static final byte DCS_8_BIT_DATA = (byte)0x04;

	// ADPU definitions
	// e.g. final static byte CLA = ISO7816.OFFSET_CLA;

	/*
	 * Constructor
	 *
	 * All the applet objects are created in init Toolkit method. 
	 **/
	public QRSTK()
	{
		register();
		initToolkit();
	}

	/**
	 * Toolkit initialization
	 *
	 * Virtual method invoked after the registration to the toolkit
	 **/
	public void initToolkit()
	{
		tkRegistry = ToolkitRegistrySystem.getEntry(); // Registering to the toolkit 
		
		/*menuItem1 = tkRegistry.initMenuEntry(menuTitle, (short)0, (short)menuTitle.length,
																(byte)0,	// Next action
																false,		// Help supported
																(byte)0,	// Icon Qualifier
																(short)0); 	// Icon Identifier*/

		menuItem2 = tkRegistry.initMenuEntry(menuTitle2, (short)0, (short)menuTitle2.length,
																(byte)0,	// Next action
																false,		// Help supported
																(byte)0,	// Icon Qualifier
																(short)0); 	// Icon Identifier

		menuItem3 = tkRegistry.initMenuEntry(menuTitle3, (short)0, (short)menuTitle3.length,
																(byte)0,	// Next action
																false,		// Help supported
																(byte)0,	// Icon Qualifier
																(short)0); 	// Icon Identifier

		menuItem4 = tkRegistry.initMenuEntry(menuTitle4, (short)0, (short)menuTitle4.length,
																(byte)0,	// Next action
																false,		// Help supported
																(byte)0,	// Icon Qualifier
																(short)0); 	// Icon Identifier

		menuItem5 = tkRegistry.initMenuEntry(menuTitle5, (short)0, (short)menuTitle5.length,
																(byte)0,	// Next action
																false,		// Help supported
																(byte)0,	// Icon Qualifier
																(short)0); 	// Icon Identifier


		// The DF Telecom and the EF SUME are selected in the two FileView and AdminFileView objects.
		// DF Telecom points to an administrative File View object to enable a resize operation on EF_SUME
		fvEF_DF_TELECOM = AdminFileViewBuilder.getTheUICCAdminFileView(JCSystem.NOT_A_TRANSIENT_OBJECT);
		fvEF_SUME = UICCSystem.getTheUICCView(JCSystem.NOT_A_TRANSIENT_OBJECT);

		fvEF_DF_GRAPHICS = AdminFileViewBuilder.getTheUICCAdminFileView(JCSystem.NOT_A_TRANSIENT_OBJECT);
		fvEF_IMG = UICCSystem.getTheUICCView(JCSystem.NOT_A_TRANSIENT_OBJECT);

		fvEF_SUME.select(UICCConstants.FID_DF_TELECOM); 
		fvEF_SUME.select(FID_EF_SUME);

		// DF.TELECOM->DF.GRAPHICS->EF.IMG
		fvEF_IMG.select(UICCConstants.FID_DF_TELECOM);
		fvEF_IMG.select(UICCConstants.FID_DF_GRAPHICS);
		fvEF_IMG.select(FID_EF_INSTANCE);
		
		/*fvEF_DF_GRAPHICS.select(UICCConstants.FID_DF_TELECOM); 
		try {
			fvEF_DF_GRAPHICS.select(UICCConstants.FID_DF_GRAPHICS); 

		} catch (UICCException e) { 
			if (e.getReason() == UICCException.FILE_NOT_FOUND) { // File not found routine UICCException.FILE_NOT_FOUND
				editHandler = (EditHandler)HandlerBuilder.buildTLVHandler(HandlerBuilder.EDIT_HANDLER, (short)create_df_graphics.length);
				createDF_Graphics();
			}
		}*/

		fvEF_DF_TELECOM.select(UICCConstants.FID_DF_TELECOM);

		// The edit handler is allocated with exactly size of the Resize Command template
		editHandler = (EditHandler)HandlerBuilder.buildTLVHandler(HandlerBuilder.EDIT_HANDLER, (short)resizeCommand.length);
	}

	/**
	 * Installation method
	 *
	 * After the applet instantiation and registration, all toolkit initialization
	 * operations are performed
	 **/
	public static void install(byte bArray[], short bOffset, byte bLength)
	{
		QRSTK menuResizeApplet = new QRSTK();
	}

	/**
	 * processToolkit
	 * Entry point for the toolkit events
	 * The application is registered only to the menu selection event
	 **/
	public void processToolkit(short event)
	{
		byte res;
		byte[] globalBuffer;
		short dataLen;
		if (event == ToolkitConstants.EVENT_MENU_SELECTION)
		{
			
			ProactiveHandler proh = ProactiveHandlerSystem.getTheHandler();
			EnvelopeHandler envh = EnvelopeHandlerSystem.getTheHandler();

			byte selectedItemId = envh.getItemIdentifier();

			if (selectedItemId == menuItem1) {

				// User is prompted with a get input to change the menu title
				proh.initGetInput( (byte)0x01,							// CQ see TS 102 223. Bits 1-4 toggle alphabet(s) 
									DCS_8_BIT_DATA,
									insertNewTitle_text, 
									(short)0,							// Prompt text offset
									(short)insertNewTitle_text.length, 	// Length of displayed text string (prompt)
									(short)1, 							// Min resp. length
									(short)20 							// Max. resp. length
									);

				res = proh.send();

				// If the user presses ok...
				if (res == (byte)0x00)
				{

				// The text is retrieved by the Proactive Response Handler
				ProactiveResponseHandler proresh = ProactiveResponseHandlerSystem.getTheHandler();

				// To perform copy operation, the Volatile Byte Array is used (no memory allocation)
				globalBuffer = UICCPlatform.getTheVolatileByteArray();
				dataLen = proresh.copyTextString(globalBuffer, (short)2);

				// Data is stored inside the Menu Resize in a TLV structure
				globalBuffer[0] = ToolkitConstants.TAG_ALPHA_IDENTIFIER; globalBuffer[1] = (byte)(dataLen-2);
					
				// The file is resized to exactly fit the buffer
				resizeMenuFile(dataLen);

				// File content is updated
				fvEF_SUME.updateBinary((short)0, globalBuffer, (short)0, dataLen); }
			
			} else if (selectedItemId == menuItem2) {

				proh.init(ToolkitConstants.PRO_CMD_DISPLAY_TEXT, 
							(byte)(DTQ_HIGH_PRIORITY|DTQ_WAIT_FOR_USER), 
							ToolkitConstants.DEV_ID_DISPLAY);
			
				proh.appendTLV((byte)(ToolkitConstants.TAG_TEXT_STRING|ToolkitConstants.TAG_SET_CR),
								DCS_8_BIT_DATA, 
								id_string, 
								(short) 0x0000, 
								(short) id_string.length);

				/*proh.initDisplayText((byte)81, 			// Command Qualifier. TS 102 223. 81h = high priority, wait for user 
									DCS_8_BIT_DATA, 
									id_string, 
									(short) 0, // Offset
									(short)id_string.length);*/

				// ETSI TS 102 223 
				// The UICC indicates to the terminal whether the icon replaces an alpha identifier or text string
				// NOTE: Designers should be aware that icons provided by the application may not be displayed by the terminal.
				// Icon qualifier coding: bit 1 = icon is self explanatory (replaces text), bits 2-8 = 0
				//proh.appendTLV(ToolkitConstants.TAG_ICON_IDENTIFIER, (byte) 0x01, (byte) 0x02);
				
				proh.appendArray(iconTLV, (short)0x00, (short)iconTLV.length);
				
				res = proh.send();
				id_string[3] = (byte)(0xff&(0x30+res));
				

			} else if (selectedItemId == menuItem3) {
				proh.init(ToolkitConstants.PRO_CMD_DISPLAY_TEXT, 
							(byte)(DTQ_HIGH_PRIORITY|DTQ_WAIT_FOR_USER), 
							ToolkitConstants.DEV_ID_DISPLAY);
			
				proh.appendTLV((byte)(ToolkitConstants.TAG_TEXT_STRING|ToolkitConstants.TAG_SET_CR),
								DCS_8_BIT_DATA, 
								id_string, 
								(short) 0x0000, 
								(short) id_string.length);

				proh.appendArray(iconTLV2, (short)0x00, (short)iconTLV2.length);
				
				res = proh.send();
				id_string[3] = (byte)(0xff&(0x30+res));

			} else if (selectedItemId == menuItem4) {

				proh.init(ToolkitConstants.PRO_CMD_DISPLAY_TEXT, 
							(byte)(DTQ_HIGH_PRIORITY|DTQ_WAIT_FOR_USER), 
							ToolkitConstants.DEV_ID_DISPLAY);

			
				proh.appendTLV((byte)(ToolkitConstants.TAG_TEXT_STRING|ToolkitConstants.TAG_SET_CR),
								DCS_8_BIT_DATA, 
								id_string, 
								(short) 0x0000, 
								(short) id_string.length);

				proh.appendArray(iconTLV3, (short)0x00, (short)iconTLV3.length);				
				res = proh.send();

			} else if (selectedItemId == menuItem5) {

				proh.init(ToolkitConstants.PRO_CMD_DISPLAY_TEXT, 
							(byte)(DTQ_HIGH_PRIORITY|DTQ_WAIT_FOR_USER), 
							ToolkitConstants.DEV_ID_DISPLAY);

				JCQRencoder.encode(qr_message);

				timing_string[0] = JCQRencoder.icon_bytes[0];
				timing_string[1] = JCQRencoder.icon_bytes[1];
				timing_string[2] = JCQRencoder.icon_bytes[2];

				// updateBinary(short fileOffset, byte[] data, short dataOffset, short dataLength)
				try {
					fvEF_IMG.updateBinary((short)0, JCQRencoder.icon_bytes, (short)0, (short)JCQRencoder.icon_bytes.length);
				} catch (UICCException e) {
					timing_string[0] = (byte)'u';
					timing_string[1] = (byte)'i';
					timing_string[2] = (byte)':';
					timing_string[3] = (byte)(e.getReason()&0xff);
					timing_string[4] = (byte)((e.getReason()<<8)&0xff);
				} catch (ArrayIndexOutOfBoundsException e) {
					timing_string[0] = (byte)'a';
					timing_string[1] = (byte)'i';
					timing_string[2] = (byte)'o';
					timing_string[3] = (byte)'o';
					timing_string[4] = (byte)'b';
				} catch (NullPointerException e) {
					timing_string[0] = (byte)'n';
					timing_string[1] = (byte)'u';
					timing_string[2] = (byte)'l';
					timing_string[3] = (byte)'l';
					timing_string[4] = (byte)'p';
					timing_string[4] = (byte)'e';
				}
				
				
				proh.appendTLV((byte)(ToolkitConstants.TAG_TEXT_STRING|ToolkitConstants.TAG_SET_CR),
								DCS_8_BIT_DATA, 
								timing_string, 
								(short) 0x0000, 
								(short) timing_string.length);
				
				
				//proh.appendArray(iconTLV3, (short)0x00, (short)iconTLV3.length);				
				res = proh.send();

			}

		}

	}

	/**
	* Resize Menu File
	*
	* It performs the resizing of the EF Sume file. 
	**/
	public void resizeMenuFile(short newFileLength) {
		// The new length is set in the byte array
		Util.setShort(resizeCommand, FILE_LENGTH_OFFSET, newFileLength); 
		
		editHandler.clear();
		editHandler.appendArray(resizeCommand, (short)0, (short)resizeCommand.length);

		// The resize operation is performed
		fvEF_DF_TELECOM.resizeFile(editHandler); 
	}

	/**
	 * Create Dedicated File (DF) at the file ID specified
	 * See ETSI TS 102 222 V15.0.0 e.g. Table 3: Coding of the data field of the CREATE FILE command
	 * 
	 * @param adminFileView AdminFileView interface
	 * @param dfID File ID location to create DF
	 */
	/*public void createDF_Graphics() {

	
		// Create DF_dfID
		editHandler.clear();
		editHandler.appendArray(create_df_graphics, (short)0, (short)create_df_graphics.length);

		//editHandler.appendTLV((byte)0x82, fileDescriptor, (short)0x00, (short)fileDescriptor.length); // value, val. offset, val. length
		//editHandler.appendTLV((byte)0x83, (byte)0x02, dfID);					// Tag: File ID, length of fileID, fileID
		//editHandler.appendTLV((byte)0x8A, (byte)0x01, LCSI_ACTIVATED);			// Tag: LCSI, length of LCSI, LCSI
		// Tag: Security Attributes Referenced ('8B'), securityattributes
		//editHandler.appendTLV((byte)0x8B, securityAttributes, (short)0x00, (short)securityAttribute.length); 
		//editHandler.appendTLV((byte)0x80, fileSize);
		//editHandler.appendArray(sfiTLV, (short)0x00, (short) sfiTLV.length); 

		try{
			fvEF_DF_GRAPHICS.createFile(editHandler);
		} catch (UICCException e) {
			Util.arrayCopy(error_unknown, (short)0, insertNewTitle_text, (short)0, (short)error_unknown.length);
			insertNewTitle_text[(short)(error_unknown.length)] = (byte)' ';
			insertNewTitle_text[(short)(error_unknown.length+1)] = (byte)(48+e.getReason());
		} catch (AdminException e) {
			// CONDITIONS_OF_USE_NOT_SATISFIED, NOT_ENOUGH_MEMORY_SPACE, INCORRECT_PARAMETERS
			if (e.getReason() == AdminException.INCORRECT_PARAMETERS) {
				Util.arrayCopy(error_incorrect_params, (short)0, insertNewTitle_text, (short)0, (short)error_incorrect_params.length);
			} else if (e.getReason() == AdminException.FILE_ALREADY_EXISTS) {
				Util.arrayCopy(error_file_exists, (short)0, insertNewTitle_text, (short)0, (short)error_file_exists.length);
			} else if (e.getReason() == AdminException.CONDITIONS_OF_USE_NOT_SATISFIED) {
				Util.arrayCopy(error_bad_cond, (short)0, insertNewTitle_text, (short)0, (short)error_bad_cond.length);
			} else if (e.getReason() == AdminException.NOT_ENOUGH_MEMORY_SPACE) {
				Util.arrayCopy(error_no_space, (short)0, insertNewTitle_text, (short)0, (short)error_no_space.length);
			} else {
				//Util.arrayCopy(error_unknown, (short)0, insertNewTitle_text, (short)0, (short)error_unknown.length);
			}
		}
		
	
	}*/

	/** 
	 * process any *incoming* ADPU command (i.e., not STK, those must be handled in processTooolkit)
	 * The applet receives the APDU instance to process from the Java Card runtime environment 
	 * The first five header bytes [ CLA, INS, P1, P2, P3 ] are available in the APDU buffer.
	**/
	public void process(APDU apdu)
	{
	}

	public Shareable getShareableInterfaceObject(AID clientAID, byte parameter)
	{
	if (clientAID == null) 			// System invoked
		return((Shareable)this);
	return null;
	}
}