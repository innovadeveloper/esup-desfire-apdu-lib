package org.esupportail.desfire.service;

import org.esupportail.desfire.core.SimpleSCR;
import org.esupportail.desfire.model.KeyType;
import org.esupportail.desfire.exceptions.DesfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplified DESFire APDU service for generating command strings
 * This class provides high-level methods that return APDU commands as hex strings
 */
public class DESFireApduService extends SimpleSCR {
    
    private static final Logger log = LoggerFactory.getLogger(DESFireApduService.class);
    
    private KeyType currentKeyType;
    private byte currentKeyNo;
    private byte[] currentAid = new byte[3];
    protected boolean authenticated = false;
    
    public DESFireApduService() {
        reset();
    }
    
    /**
     * Reset authentication state
     */
    public void reset() {
        currentKeyType = null;
        currentKeyNo = -1;
        authenticated = false;
    }
    
    // ================ PICC LEVEL COMMANDS ================
    
    /**
     * Get version information from the PICC
     */
    public String getVersion() {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_VERSION, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Get free memory available on PICC
     */
    public String getFreeMemory() {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_FREE_MEMORY, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Format the PICC (destroys all data)
     */
    public String formatPicc() {
        byte[] apdu = {(byte) 0x90, ApduCommand.FORMAT_PICC, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Get list of application IDs
     */
    public String getApplicationIds() {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_APPLICATION_IDS, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Create a new application
     */
    public String createApplication(byte[] aid, byte keySettings, byte numberOfKeys) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }
        
        byte[] apdu = new byte[10];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.CREATE_APPLICATION;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x05; // data length
        System.arraycopy(aid, 0, apdu, 5, 3);
        apdu[8] = keySettings;
        apdu[9] = numberOfKeys;
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Delete an application
     */
    public String deleteApplication(byte[] aid) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }
        
        byte[] apdu = new byte[8];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.DELETE_APPLICATION;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x03; // data length
        System.arraycopy(aid, 0, apdu, 5, 3);
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    // ================ APPLICATION LEVEL COMMANDS ================
    
    /**
     * Select an application
     */
    public String selectApplication(byte[] aid) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }
        
        byte[] apdu = new byte[8];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.SELECT_APPLICATION;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x03; // data length
        System.arraycopy(aid, 0, apdu, 5, 3);
        
        System.arraycopy(aid, 0, currentAid, 0, 3);
        reset(); // Reset authentication when selecting new app
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Get file IDs in current application
     */
    public String getFileIds() {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_FILE_IDS, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Get file settings
     */
    public String getFileSettings(byte fileNo) {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_FILE_SETTINGS, 0x00, 0x00, 0x01, fileNo};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Create standard data file
     */
    public String createStdDataFile(byte fileNo, byte commSettings, byte[] accessRights, 
                                   int fileSize) {
        byte[] apdu = new byte[12];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.CREATE_STD_DATA_FILE;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07; // data length
        apdu[5] = fileNo;
        apdu[6] = commSettings;
        System.arraycopy(accessRights, 0, apdu, 7, 2);
        apdu[9] = (byte) (fileSize & 0xFF);
        apdu[10] = (byte) ((fileSize >> 8) & 0xFF);
        apdu[11] = (byte) ((fileSize >> 16) & 0xFF);
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    // ================ DATA MANIPULATION COMMANDS ================
    
    /**
     * Read data from file
     */
    public String readData(byte fileNo, int offset, int length) {
        byte[] apdu = new byte[12];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.READ_DATA;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07; // data length
        apdu[5] = fileNo;
        apdu[6] = (byte) (offset & 0xFF);
        apdu[7] = (byte) ((offset >> 8) & 0xFF);
        apdu[8] = (byte) ((offset >> 16) & 0xFF);
        apdu[9] = (byte) (length & 0xFF);
        apdu[10] = (byte) ((length >> 8) & 0xFF);
        apdu[11] = (byte) ((length >> 16) & 0xFF);
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Write data to file
     */
    public String writeData(byte fileNo, int offset, byte[] data) {
        byte[] apdu = new byte[12 + data.length];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.WRITE_DATA;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte) (7 + data.length); // data length
        apdu[5] = fileNo;
        apdu[6] = (byte) (offset & 0xFF);
        apdu[7] = (byte) ((offset >> 8) & 0xFF);
        apdu[8] = (byte) ((offset >> 16) & 0xFF);
        apdu[9] = (byte) (data.length & 0xFF);
        apdu[10] = (byte) ((data.length >> 8) & 0xFF);
        apdu[11] = (byte) ((data.length >> 16) & 0xFF);
        System.arraycopy(data, 0, apdu, 12, data.length);
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    // ================ AUTHENTICATION COMMANDS ================
    
    /**
     * Start authentication (step 1)
     */
    public String authenticate(byte keyNo, KeyType keyType) {
        byte[] apdu = new byte[7];
        apdu[0] = (byte) 0x90;
        
        switch (keyType) {
            case DES:
            case TDES:
                apdu[1] = ApduCommand.AUTHENTICATE_DES_2K3DES;
                break;
            case TKTDES:
                apdu[1] = ApduCommand.AUTHENTICATE_3K3DES;
                break;
            case AES:
                apdu[1] = ApduCommand.AUTHENTICATE_AES;
                break;
            default:
                throw new DesfireException("Unsupported key type: " + keyType);
        }
        
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x01; // data length
        apdu[5] = keyNo;
        apdu[6] = 0x00; // Le
        
        currentKeyNo = keyNo;
        currentKeyType = keyType;
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Change key
     */
    public String changeKey(byte keyNo, byte keyVersion, KeyType keyType, 
                           byte[] newKey, byte[] oldKey) {
        // This is a simplified version - full implementation would handle encryption
        int keyLength = getKeyLength(keyType);
        byte[] apdu = new byte[7 + keyLength + 1];
        apdu[0] = (byte) 0x90;
        apdu[1] = ApduCommand.CHANGE_KEY;
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte) (keyLength + 1); // data length
        apdu[5] = keyNo;
        System.arraycopy(newKey, 0, apdu, 6, keyLength);
        apdu[6 + keyLength] = keyVersion;
        
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    // ================ UTILITY METHODS ================
    
    /**
     * Continue reading additional frames
     */
    public String getAdditionalFrame() {
        byte[] apdu = {(byte) 0x90, ApduCommand.ADDITIONAL_FRAME, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Get card UID
     */
    public String getCardUid() {
        byte[] apdu = {(byte) 0x90, ApduCommand.GET_CARD_UID, 0x00, 0x00, 0x00};
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    private int getKeyLength(KeyType keyType) {
        switch (keyType) {
            case DES: return 8;
            case TDES: return 16;
            case TKTDES: return 24;
            case AES: return 16;
            default: throw new DesfireException("Unknown key type: " + keyType);
        }
    }
    
    // ================ GETTERS ================
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public KeyType getCurrentKeyType() {
        return currentKeyType;
    }
    
    public byte getCurrentKeyNo() {
        return currentKeyNo;
    }
    
    public byte[] getCurrentAid() {
        return currentAid.clone();
    }
}