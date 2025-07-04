package org.esupportail.desfire.service;

/**
 * DESFire APDU Response codes
 */
public class ApduResponse {
    
    // Success codes
    public static final int OPERATION_OK = 0x00;
    public static final int NO_CHANGES = 0x0C;
    public static final int OUT_OF_EEPROM_ERROR = 0x0E;
    public static final int ILLEGAL_COMMAND_CODE = 0x1C;
    public static final int INTEGRITY_ERROR = 0x1E;
    public static final int NO_SUCH_KEY = 0x40;
    public static final int LENGTH_ERROR = 0x7E;
    public static final int PERMISSION_DENIED = 0x9D;
    public static final int PARAMETER_ERROR = 0x9E;
    public static final int APPLICATION_NOT_FOUND = 0xA0;
    public static final int APPL_INTEGRITY_ERROR = 0xA1;
    public static final int AUTHENTICATION_ERROR = 0xAE;
    public static final int ADDITIONAL_FRAME = 0xAF;
    public static final int BOUNDARY_ERROR = 0xBE;
    public static final int PICC_INTEGRITY_ERROR = 0xC1;
    public static final int COMMAND_ABORTED = 0xCA;
    public static final int PICC_DISABLED_ERROR = 0xCD;
    public static final int COUNT_ERROR = 0xCE;
    public static final int DUPLICATE_ERROR = 0xDE;
    public static final int EEPROM_ERROR = 0xEE;
    public static final int FILE_NOT_FOUND = 0xF0;
    public static final int FILE_INTEGRITY_ERROR = 0xF1;
    
    private ApduResponse() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Check if response code indicates success
     */
    public static boolean isSuccess(int responseCode) {
        return responseCode == OPERATION_OK;
    }
    
    /**
     * Check if response indicates more data available
     */
    public static boolean hasMoreData(int responseCode) {
        return responseCode == ADDITIONAL_FRAME;
    }
    
    /**
     * Get response description
     */
    public static String getDescription(int responseCode) {
        switch (responseCode) {
            case OPERATION_OK: return "Operation OK";
            case NO_CHANGES: return "No changes";
            case OUT_OF_EEPROM_ERROR: return "Out of EEPROM error";
            case ILLEGAL_COMMAND_CODE: return "Illegal command code";
            case INTEGRITY_ERROR: return "Integrity error";
            case NO_SUCH_KEY: return "No such key";
            case LENGTH_ERROR: return "Length error";
            case PERMISSION_DENIED: return "Permission denied";
            case PARAMETER_ERROR: return "Parameter error";
            case APPLICATION_NOT_FOUND: return "Application not found";
            case APPL_INTEGRITY_ERROR: return "Application integrity error";
            case AUTHENTICATION_ERROR: return "Authentication error";
            case ADDITIONAL_FRAME: return "Additional frame";
            case BOUNDARY_ERROR: return "Boundary error";
            case PICC_INTEGRITY_ERROR: return "PICC integrity error";
            case COMMAND_ABORTED: return "Command aborted";
            case PICC_DISABLED_ERROR: return "PICC disabled error";
            case COUNT_ERROR: return "Count error";
            case DUPLICATE_ERROR: return "Duplicate error";
            case EEPROM_ERROR: return "EEPROM error";
            case FILE_NOT_FOUND: return "File not found";
            case FILE_INTEGRITY_ERROR: return "File integrity error";
            default: return "Unknown error code: 0x" + Integer.toHexString(responseCode);
        }
    }
}