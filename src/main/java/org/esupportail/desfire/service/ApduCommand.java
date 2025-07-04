package org.esupportail.desfire.service;

/**
 * DESFire APDU Command definitions
 */
public class ApduCommand {
    
    // PICC level commands
    public static final byte CREATE_APPLICATION = (byte) 0xCA;
    public static final byte DELETE_APPLICATION = (byte) 0xDA;
    public static final byte GET_APPLICATION_IDS = (byte) 0x6A;
    public static final byte GET_FREE_MEMORY = (byte) 0x6E;
    public static final byte GET_VERSION = (byte) 0x60;
    public static final byte FORMAT_PICC = (byte) 0xFC;
    public static final byte SET_CONFIGURATION = (byte) 0x5C;
    
    // Application level commands
    public static final byte SELECT_APPLICATION = (byte) 0x5A;
    public static final byte GET_FILE_IDS = (byte) 0x6F;
    public static final byte GET_FILE_SETTINGS = (byte) 0xF5;
    public static final byte CHANGE_FILE_SETTINGS = (byte) 0x5F;
    public static final byte CREATE_STD_DATA_FILE = (byte) 0xCD;
    public static final byte CREATE_BACKUP_DATA_FILE = (byte) 0xCB;
    public static final byte CREATE_LINEAR_RECORD_FILE = (byte) 0xC1;
    public static final byte CREATE_CYCLIC_RECORD_FILE = (byte) 0xC0;
    public static final byte DELETE_FILE = (byte) 0xDF;
    
    // Data manipulation commands  
    public static final byte READ_DATA = (byte) 0xBD;
    public static final byte WRITE_DATA = (byte) 0x3D;
    public static final byte GET_VALUE = (byte) 0x6C;
    public static final byte CREDIT = (byte) 0x0C;
    public static final byte DEBIT = (byte) 0xDC;
    public static final byte LIMITED_CREDIT = (byte) 0x1C;
    public static final byte WRITE_RECORD = (byte) 0x3B;
    public static final byte READ_RECORDS = (byte) 0xBB;
    public static final byte CLEAR_RECORD_FILE = (byte) 0xEB;
    public static final byte COMMIT_TRANSACTION = (byte) 0xC7;
    public static final byte ABORT_TRANSACTION = (byte) 0xA7;
    
    // Authentication commands
    public static final byte AUTHENTICATE_DES_2K3DES = (byte) 0x0A;
    public static final byte AUTHENTICATE_3K3DES = (byte) 0x1A;
    public static final byte AUTHENTICATE_AES = (byte) 0xAA;
    public static final byte CHANGE_KEY_SETTINGS = (byte) 0x54;
    public static final byte GET_KEY_SETTINGS = (byte) 0x45;
    public static final byte CHANGE_KEY = (byte) 0xC4;
    public static final byte GET_KEY_VERSION = (byte) 0x64;
    
    // Additional commands
    public static final byte ADDITIONAL_FRAME = (byte) 0xAF;
    public static final byte GET_CARD_UID = (byte) 0x51;
    
    private ApduCommand() {
        // Utility class - prevent instantiation
    }
}