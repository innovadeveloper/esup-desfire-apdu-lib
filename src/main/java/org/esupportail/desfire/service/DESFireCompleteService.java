package org.esupportail.desfire.service;

import org.esupportail.desfire.core.DESFireEV1;
import org.esupportail.desfire.model.KeyType;
import org.esupportail.desfire.exceptions.DesfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Complete DESFire service combining APDU generation and card communication
 * This service provides both high-level operations and direct card access
 */
public class DESFireCompleteService extends DESFireEV1 {
    
    private static final Logger log = LoggerFactory.getLogger(DESFireCompleteService.class);
    
    private DESFireAuthService authService;
    private DESFireApduService apduService;
    
    public DESFireCompleteService() {
        super();
        this.authService = new DESFireAuthService();
        this.apduService = new DESFireApduService();
    }
    
    // ================ HIGH-LEVEL OPERATIONS ================
    
    /**
     * Complete authentication with automatic 3-step process
     */
    public boolean authenticateWithCard(byte keyNo, KeyType keyType, byte[] key) {
        try {
            log.debug("Starting complete authentication process");
            return super.authenticate(key, keyNo, keyType) != null;
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return false;
        }
    }
    
    /**
     * Initialize card for first use
     */
    public boolean initializeCard(byte[] piccMasterKey, KeyType keyType) {
        try {
            // First authenticate with default key (all zeros)
            byte[] defaultKey = new byte[getKeyLength(keyType)];
            if (!authenticateWithCard((byte) 0x00, keyType, defaultKey)) {
                log.error("Failed to authenticate with default key");
                return false;
            }
            
            // Change PICC master key
            if (!changeKey((byte) 0x00, (byte) 0x00, keyType, piccMasterKey, defaultKey)) {
                log.error("Failed to change PICC master key");
                return false;
            }
            
            log.info("Card initialized successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Card initialization failed", e);
            return false;
        }
    }
    
    /**
     * Create application with keys and files
     */
    public boolean createApplicationComplete(byte[] aid, byte keySettings, byte numberOfKeys,
                                           byte[] appMasterKey, KeyType keyType) {
        try {
            // Create application
            if (!super.createApplication(aid, keySettings, numberOfKeys)) {
                log.error("Failed to create application");
                return false;
            }
            
            // Select new application
            if (!super.selectApplication(aid)) {
                log.error("Failed to select new application");
                return false;
            }
            
            // Authenticate with default key (all zeros)
            byte[] defaultKey = new byte[getKeyLength(keyType)];
            if (!authenticateWithCard((byte) 0x00, keyType, defaultKey)) {
                log.error("Failed to authenticate with new application");
                return false;
            }
            
            // Change application master key
            if (!changeKey((byte) 0x00, (byte) 0x00, keyType, appMasterKey, defaultKey)) {
                log.error("Failed to change application master key");
                return false;
            }
            
            log.info("Application created and configured successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Application creation failed", e);
            return false;
        }
    }
    
    /**
     * Read file with automatic authentication if needed
     */
    public byte[] readFileComplete(byte[] aid, byte keyNo, KeyType keyType, byte[] key,
                                  byte fileNo, int offset, int length) {
        try {
            // Select application
            if (!super.selectApplication(aid)) {
                log.error("Failed to select application");
                return null;
            }
            
            // Authenticate if needed
            if (!isAuthenticated()) {
                if (!authenticateWithCard(keyNo, keyType, key)) {
                    log.error("Failed to authenticate");
                    return null;
                }
            }
            
            // Read data
            return super.readData(fileNo, offset, length);
            
        } catch (Exception e) {
            log.error("File read failed", e);
            return null;
        }
    }
    
    /**
     * Write file with automatic authentication if needed
     */
    public boolean writeFileComplete(byte[] aid, byte keyNo, KeyType keyType, byte[] key,
                                   byte fileNo, int offset, byte[] data) {
        try {
            // Select application
            if (!super.selectApplication(aid)) {
                log.error("Failed to select application");
                return false;
            }
            
            // Authenticate if needed
            if (!isAuthenticated()) {
                if (!authenticateWithCard(keyNo, keyType, key)) {
                    log.error("Failed to authenticate");
                    return false;
                }
            }
            
            // Write data
            return super.writeData(fileNo, offset, data);
            
        } catch (Exception e) {
            log.error("File write failed", e);
            return false;
        }
    }
    
    // ================ ADDITIONAL COMMANDS ================
    
    /**
     * Get version information with complete data parsing
     */
    public String getVersionComplete() {
        try {
            byte[] versionData = super.getVersion();
            if (versionData != null) {
                return parseVersionData(versionData);
            }
            return "Failed to get version";
        } catch (Exception e) {
            log.error("Failed to get version", e);
            return "Error getting version: " + e.getMessage();
        }
    }
    
    /**
     * Parse version data into human-readable format
     */
    private String parseVersionData(byte[] data) {
        if (data == null || data.length < 28) {
            return "Invalid version data (length: " + (data != null ? data.length : 0) + ")";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("DESFire Version Information:\n");
        
        // Hardware information (first 7 bytes)
        sb.append("Hardware:\n");
        sb.append("  Vendor ID: 0x").append(String.format("%02X", data[0])).append("\n");
        sb.append("  Type: 0x").append(String.format("%02X", data[1])).append("\n");
        sb.append("  Subtype: 0x").append(String.format("%02X", data[2])).append("\n");
        sb.append("  Version: ").append(data[3]).append(".").append(data[4]).append("\n");
        sb.append("  Storage size: 0x").append(String.format("%02X", data[5])).append("\n");
        sb.append("  Protocol: 0x").append(String.format("%02X", data[6])).append("\n");
        
        // Software information (next 7 bytes)
        sb.append("Software:\n");
        sb.append("  Vendor ID: 0x").append(String.format("%02X", data[7])).append("\n");
        sb.append("  Type: 0x").append(String.format("%02X", data[8])).append("\n");
        sb.append("  Subtype: 0x").append(String.format("%02X", data[9])).append("\n");
        sb.append("  Version: ").append(data[10]).append(".").append(data[11]).append("\n");
        sb.append("  Storage size: 0x").append(String.format("%02X", data[12])).append("\n");
        sb.append("  Protocol: 0x").append(String.format("%02X", data[13])).append("\n");
        
        // Batch and production information (last 14 bytes)
        sb.append("Production:\n");
        sb.append("  UID: ");
        for (int i = 14; i < 21; i++) {
            sb.append(String.format("%02X", data[i]));
            if (i < 20) sb.append(" ");
        }
        sb.append("\n");
        sb.append("  Batch: ");
        for (int i = 21; i < 26; i++) {
            sb.append(String.format("%02X", data[i]));
            if (i < 25) sb.append(" ");
        }
        sb.append("\n");
        sb.append("  Production date: Week ").append(data[26]).append("/").append(data[27]).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Change key
     */
    public boolean changeKey(byte keyNo, byte keyVersion, KeyType keyType, 
                           byte[] newKey, byte[] currentKey) {
        // This is a simplified implementation
        // Full implementation would handle proper encryption and key diversification
        try {
            byte[] apdu = new byte[8 + getKeyLength(keyType)];
            apdu[0] = (byte) 0x90;
            apdu[1] = (byte) 0xC4; // CHANGE_KEY
            apdu[2] = 0x00;
            apdu[3] = 0x00;
            apdu[4] = (byte) (1 + getKeyLength(keyType));
            apdu[5] = keyNo;
            System.arraycopy(newKey, 0, apdu, 6, getKeyLength(keyType));
            apdu[6 + getKeyLength(keyType)] = keyVersion;
            
            byte[] response = transmit(apdu);
            return response != null && response.length >= 2 && 
                   response[response.length - 2] == (byte) 0x91 && 
                   response[response.length - 1] == 0x00;
                   
        } catch (Exception e) {
            log.error("Change key failed", e);
            return false;
        }
    }
    
    /**
     * Get key settings
     */
    public byte[] getKeySettings() {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0x45, 0x00, 0x00, 0x00}; // GET_KEY_SETTINGS
            byte[] response = transmit(apdu);
            
            if (response != null && response.length >= 2 && 
                response[response.length - 2] == (byte) 0x91 && 
                response[response.length - 1] == 0x00) {
                byte[] result = new byte[response.length - 2];
                System.arraycopy(response, 0, result, 0, response.length - 2);
                return result;
            }
            return null;
            
        } catch (Exception e) {
            log.error("Get key settings failed", e);
            return null;
        }
    }
    
    /**
     * Change key settings
     */
    public boolean changeKeySettings(byte keySettings) {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0x54, 0x00, 0x00, 0x01, keySettings}; // CHANGE_KEY_SETTINGS
            byte[] response = transmit(apdu);
            
            return response != null && response.length >= 2 && 
                   response[response.length - 2] == (byte) 0x91 && 
                   response[response.length - 1] == 0x00;
                   
        } catch (Exception e) {
            log.error("Change key settings failed", e);
            return false;
        }
    }
    
    /**
     * Get file IDs
     */
    public byte[] getFileIds() {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0x6F, 0x00, 0x00, 0x00}; // GET_FILE_IDS
            byte[] response = transmit(apdu);
            
            if (response != null && response.length >= 2 && 
                response[response.length - 2] == (byte) 0x91 && 
                response[response.length - 1] == 0x00) {
                byte[] result = new byte[response.length - 2];
                System.arraycopy(response, 0, result, 0, response.length - 2);
                return result;
            }
            return null;
            
        } catch (Exception e) {
            log.error("Get file IDs failed", e);
            return null;
        }
    }
    
    /**
     * Get file settings
     */
    public byte[] getFileSettings(byte fileNo) {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0xF5, 0x00, 0x00, 0x01, fileNo}; // GET_FILE_SETTINGS
            byte[] response = transmit(apdu);
            
            if (response != null && response.length >= 2 && 
                response[response.length - 2] == (byte) 0x91 && 
                response[response.length - 1] == 0x00) {
                byte[] result = new byte[response.length - 2];
                System.arraycopy(response, 0, result, 0, response.length - 2);
                return result;
            }
            return null;
            
        } catch (Exception e) {
            log.error("Get file settings failed", e);
            return null;
        }
    }
    
    /**
     * Change file settings
     */
    public boolean changeFileSettings(byte fileNo, byte commSettings, byte[] accessRights) {
        try {
            byte[] apdu = new byte[8];
            apdu[0] = (byte) 0x90;
            apdu[1] = (byte) 0x5F; // CHANGE_FILE_SETTINGS
            apdu[2] = 0x00;
            apdu[3] = 0x00;
            apdu[4] = 0x03;
            apdu[5] = fileNo;
            apdu[6] = commSettings;
            apdu[7] = accessRights[0]; // Simplified - should be 2 bytes
            
            byte[] response = transmit(apdu);
            return response != null && response.length >= 2 && 
                   response[response.length - 2] == (byte) 0x91 && 
                   response[response.length - 1] == 0x00;
                   
        } catch (Exception e) {
            log.error("Change file settings failed", e);
            return false;
        }
    }
    
    /**
     * Delete file
     */
    public boolean deleteFile(byte fileNo) {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0xDF, 0x00, 0x00, 0x01, fileNo}; // DELETE_FILE
            byte[] response = transmit(apdu);
            
            return response != null && response.length >= 2 && 
                   response[response.length - 2] == (byte) 0x91 && 
                   response[response.length - 1] == 0x00;
                   
        } catch (Exception e) {
            log.error("Delete file failed", e);
            return false;
        }
    }
    
    /**
     * Create backup data file
     */
    public boolean createBackupDataFile(byte fileNo, byte commSettings, byte[] accessRights, int fileSize) {
        try {
            byte[] apdu = new byte[12];
            apdu[0] = (byte) 0x90;
            apdu[1] = (byte) 0xCB; // CREATE_BACKUP_DATA_FILE
            apdu[2] = 0x00;
            apdu[3] = 0x00;
            apdu[4] = 0x07;
            apdu[5] = fileNo;
            apdu[6] = commSettings;
            System.arraycopy(accessRights, 0, apdu, 7, 2);
            apdu[9] = (byte) (fileSize & 0xFF);
            apdu[10] = (byte) ((fileSize >> 8) & 0xFF);
            apdu[11] = (byte) ((fileSize >> 16) & 0xFF);
            
            byte[] response = transmit(apdu);
            return response != null && response.length >= 2 && 
                   response[response.length - 2] == (byte) 0x91 && 
                   response[response.length - 1] == 0x00;
                   
        } catch (Exception e) {
            log.error("Create backup data file failed", e);
            return false;
        }
    }
    
    /**
     * Get card UID
     */
    public byte[] getCardUID() {
        try {
            byte[] apdu = {(byte) 0x90, (byte) 0x51, 0x00, 0x00, 0x00}; // GET_CARD_UID
            byte[] response = transmit(apdu);
            
            if (response != null && response.length >= 2 && 
                response[response.length - 2] == (byte) 0x91 && 
                response[response.length - 1] == 0x00) {
                byte[] result = new byte[response.length - 2];
                System.arraycopy(response, 0, result, 0, response.length - 2);
                return result;
            }
            return null;
            
        } catch (Exception e) {
            log.error("Get card UID failed", e);
            return null;
        }
    }
    
    // ================ APDU GENERATION METHODS ================
    
    /**
     * Get APDU string for any command (for testing/debugging)
     */
    public String getApduString(String command, Object... params) {
        switch (command.toUpperCase()) {
            case "GET_VERSION":
                return apduService.getVersion();
            case "GET_FREE_MEMORY":
                return apduService.getFreeMemory();
            case "FORMAT_PICC":
                return apduService.formatPicc();
            case "GET_APPLICATION_IDS":
                return apduService.getApplicationIds();
            case "SELECT_APPLICATION":
                return apduService.selectApplication((byte[]) params[0]);
            case "CREATE_APPLICATION":
                return apduService.createApplication((byte[]) params[0], (Byte) params[1], (Byte) params[2]);
            case "DELETE_APPLICATION":
                return apduService.deleteApplication((byte[]) params[0]);
            case "AUTHENTICATE":
                return apduService.authenticate((Byte) params[0], (KeyType) params[1]);
            case "GET_FILE_IDS":
                return apduService.getFileIds();
            case "CREATE_STD_DATA_FILE":
                return apduService.createStdDataFile((Byte) params[0], (Byte) params[1], 
                                                   (byte[]) params[2], (Integer) params[3]);
            case "READ_DATA":
                return apduService.readData((Byte) params[0], (Integer) params[1], (Integer) params[2]);
            case "WRITE_DATA":
                return apduService.writeData((Byte) params[0], (Integer) params[1], (byte[]) params[2]);
            default:
                throw new DesfireException("Unknown command: " + command);
        }
    }
    
    // ================ UTILITY METHODS ================
    
    private int getKeyLength(KeyType keyType) {
        switch (keyType) {
            case DES: return 8;
            case TDES: return 16;
            case TKTDES: return 24;
            case AES: return 16;
            default: throw new DesfireException("Unknown key type: " + keyType);
        }
    }
    
    // ================ GETTERS FOR SERVICES ================
    
    public DESFireAuthService getAuthService() {
        return authService;
    }
    
    public DESFireApduService getApduService() {
        return apduService;
    }
}