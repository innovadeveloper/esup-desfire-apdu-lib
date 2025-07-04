package org.esupportail.desfire.service;

import org.esupportail.desfire.core.util.*;
import org.esupportail.desfire.model.KeyType;
import org.esupportail.desfire.exceptions.DesfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Complete 3-step DESFire authentication service
 */
public class DESFireAuthService extends DESFireApduService {
    
    private static final Logger log = LoggerFactory.getLogger(DESFireAuthService.class);
    
    // Authentication state
    private byte[] currentKey;
    private KeyType currentAuthKeyType;
    private byte currentAuthKeyNo;
    private byte[] sessionKey;
    private byte[] iv;
    private byte[] randA;
    private byte[] randB;
    private boolean authenticationInProgress = false;
    
    public DESFireAuthService() {
        super();
    }
    
    /**
     * Complete 3-step authentication process
     * @param keyNo Key number to authenticate with
     * @param keyType Type of key (DES, 3DES, AES)
     * @param key The secret key bytes
     * @return true if authentication successful
     */
    public boolean authenticateComplete(byte keyNo, KeyType keyType, byte[] key) {
        try {
            if (!validateKey(key, keyType)) {
                throw new DesfireException("Invalid key for type " + keyType);
            }
            
            this.currentKey = key.clone();
            this.currentAuthKeyType = keyType;
            this.currentAuthKeyNo = keyNo;
            this.authenticationInProgress = true;
            
            // Remove version bits from Triple DES keys
            if (keyType != KeyType.AES) {
                setKeyVersion(this.currentKey, 0, this.currentKey.length, (byte) 0x00);
            }
            
            log.debug("Starting authentication with key {} type {}", keyNo, keyType);
            return true;
            
        } catch (Exception e) {
            log.error("Authentication failed", e);
            resetAuthState();
            return false;
        }
    }
    
    /**
     * Step 1: Initialize authentication
     */
    public String authenticate1(byte keyNo, KeyType keyType) {
        this.currentAuthKeyNo = keyNo;
        this.currentAuthKeyType = keyType;
        this.authenticationInProgress = true;
        
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
        apdu[4] = 0x01;
        apdu[5] = keyNo;
        apdu[6] = 0x00;
        
        // Initialize IV
        this.iv = currentAuthKeyType == KeyType.AES ? new byte[16] : new byte[8];
        
        log.debug("Auth step 1 - keyNo: {}, keyType: {}", keyNo, keyType);
        return DesfireUtils.byteArrayToHexString(apdu);
    }
    
    /**
     * Step 2: Process challenge from card and prepare response
     */
    public String authenticate2(byte[] key, byte keyNo, KeyType keyType, byte[] randBEncrypted) {
        if (!authenticationInProgress) {
            throw new DesfireException("Authentication not in progress");
        }
        
        try {
            this.currentKey = key.clone();
            
            // Decrypt randB from card
            this.randB = decrypt(key, randBEncrypted, keyType, iv);
            if (this.randB == null) {
                throw new DesfireException("Failed to decrypt randB");
            }
            
            // Rotate randB left by 1 byte
            byte[] randBRotated = rotateLeft(this.randB);
            
            // Generate our random A
            this.randA = new byte[this.randB.length];
            new SecureRandom().nextBytes(this.randA);
            
            // Combine randA + randB_rotated
            byte[] plaintext = new byte[this.randA.length + randBRotated.length];
            System.arraycopy(this.randA, 0, plaintext, 0, this.randA.length);
            System.arraycopy(randBRotated, 0, plaintext, this.randA.length, randBRotated.length);
            
            // Encrypt the combined data
            byte[] ciphertext = encrypt(key, plaintext, keyType, iv);
            if (ciphertext == null) {
                throw new DesfireException("Failed to encrypt response");
            }
            
            // Build APDU
            byte[] apdu = new byte[5 + ciphertext.length];
            apdu[0] = (byte) 0x90;
            apdu[1] = (byte) ApduCommand.ADDITIONAL_FRAME;
            apdu[2] = 0x00;
            apdu[3] = 0x00;
            apdu[4] = (byte) ciphertext.length;
            System.arraycopy(ciphertext, 0, apdu, 5, ciphertext.length);
            
            log.debug("Auth step 2 - sending encrypted randA + randB_rot");
            return DesfireUtils.byteArrayToHexString(apdu);
            
        } catch (Exception e) {
            log.error("Authentication step 2 failed", e);
            resetAuthState();
            throw new DesfireException("Authentication step 2 failed", e);
        }
    }
    
    /**
     * Step 3: Verify final response from card
     */
    public boolean authenticate3(byte[] key, byte keyNo, KeyType keyType, byte[] cardResponse) {
        if (!authenticationInProgress) {
            throw new DesfireException("Authentication not in progress");
        }
        
        try {
            // Decrypt card's response
            byte[] decryptedResponse = decrypt(key, cardResponse, keyType, iv);
            if (decryptedResponse == null) {
                log.error("Failed to decrypt card response");
                resetAuthState();
                return false;
            }
            
            // Card should return randA rotated left by 1 byte
            byte[] expectedRandA = rotateLeft(this.randA);
            
            // Verify the response
            if (!Arrays.equals(decryptedResponse, expectedRandA)) {
                log.error("Card response verification failed");
                resetAuthState();
                return false;
            }
            
            // Generate session key
            this.sessionKey = generateSessionKey(key, keyType, this.randA, this.randB);
            
            // Mark as authenticated
            super.authenticated = true;
            this.authenticationInProgress = false;
            
            log.debug("Authentication successful - session key generated");
            return true;
            
        } catch (Exception e) {
            log.error("Authentication step 3 failed", e);
            resetAuthState();
            return false;
        }
    }
    
    /**
     * Encrypt data using current session key
     */
    public byte[] encryptData(byte[] data) {
        if (!isAuthenticated() || sessionKey == null) {
            throw new DesfireException("Not authenticated");
        }
        
        return encrypt(sessionKey, data, currentAuthKeyType, iv);
    }
    
    /**
     * Decrypt data using current session key  
     */
    public byte[] decryptData(byte[] data) {
        if (!isAuthenticated() || sessionKey == null) {
            throw new DesfireException("Not authenticated");
        }
        
        return decrypt(sessionKey, data, currentAuthKeyType, iv);
    }
    
    /**
     * Generate CMAC for data integrity
     */
    public byte[] generateCmac(byte[] data) {
        if (!isAuthenticated() || sessionKey == null) {
            throw new DesfireException("Not authenticated");
        }
        
        CMAC.Type cmacType = currentAuthKeyType == KeyType.AES ? CMAC.Type.AES : CMAC.Type.TKTDES;
        return CMAC.get(cmacType, sessionKey, data);
    }
    
    // ================ PRIVATE HELPER METHODS ================
    
    private boolean validateKey(byte[] key, KeyType type) {
        if (key == null) return false;
        
        switch (type) {
            case DES: return key.length == 8;
            case TDES: return key.length == 16;
            case TKTDES: return key.length == 24;
            case AES: return key.length == 16;
            default: return false;
        }
    }
    
    private void setKeyVersion(byte[] key, int offset, int length, byte version) {
        for (int i = offset; i < offset + length; i += 8) {
            if (i + 7 < key.length) {
                key[i + 7] = (byte) (key[i + 7] & 0xFE | version & 0x01);
            }
        }
    }
    
    private byte[] encrypt(byte[] key, byte[] data, KeyType keyType, byte[] iv) {
        switch (keyType) {
            case DES:
                return DES.encrypt(iv, key, data);
            case TDES:
            case TKTDES:
                return TripleDES.encrypt(iv, key, data);
            case AES:
                return AES.encrypt(iv, key, data);
            default:
                throw new DesfireException("Unsupported key type for encryption: " + keyType);
        }
    }
    
    private byte[] decrypt(byte[] key, byte[] data, KeyType keyType, byte[] iv) {
        switch (keyType) {
            case DES:
                return DES.decrypt(iv, key, data);
            case TDES:
            case TKTDES:
                return TripleDES.decrypt(iv, key, data);
            case AES:
                return AES.decrypt(iv, key, data);
            default:
                throw new DesfireException("Unsupported key type for decryption: " + keyType);
        }
    }
    
    private byte[] rotateLeft(byte[] data) {
        if (data == null || data.length == 0) return data;
        
        byte[] result = new byte[data.length];
        System.arraycopy(data, 1, result, 0, data.length - 1);
        result[data.length - 1] = data[0];
        return result;
    }
    
    private byte[] generateSessionKey(byte[] key, KeyType keyType, byte[] randA, byte[] randB) {
        int keyLength = keyType == KeyType.AES ? 16 : 8;
        byte[] sessionKey = new byte[keyLength];
        
        // Session key generation varies by key type
        switch (keyType) {
            case AES:
                // For AES: session key = randA[0..3] || randB[0..3] || randA[12..15] || randB[12..15]
                System.arraycopy(randA, 0, sessionKey, 0, 4);
                System.arraycopy(randB, 0, sessionKey, 4, 4);
                System.arraycopy(randA, 12, sessionKey, 8, 4);
                System.arraycopy(randB, 12, sessionKey, 12, 4);
                break;
                
            case DES:
            case TDES:
            case TKTDES:
                // For DES/3DES: session key = randA[0..3] || randB[0..3]
                System.arraycopy(randA, 0, sessionKey, 0, 4);
                System.arraycopy(randB, 0, sessionKey, 4, 4);
                break;
        }
        
        return sessionKey;
    }
    
    private void resetAuthState() {
        this.currentKey = null;
        this.currentAuthKeyType = null;
        this.currentAuthKeyNo = -1;
        this.sessionKey = null;
        this.iv = null;
        this.randA = null;
        this.randB = null;
        this.authenticationInProgress = false;
        super.authenticated = false;
    }
    
    @Override
    public void reset() {
        super.reset();
        resetAuthState();
    }
    
    // ================ GETTERS ================
    
    public byte[] getSessionKey() {
        return sessionKey != null ? sessionKey.clone() : null;
    }
    
    public boolean isAuthenticationInProgress() {
        return authenticationInProgress;
    }
    
    public KeyType getCurrentAuthKeyType() {
        return currentAuthKeyType;
    }
    
    public byte getCurrentAuthKeyNo() {
        return currentAuthKeyNo;
    }
}