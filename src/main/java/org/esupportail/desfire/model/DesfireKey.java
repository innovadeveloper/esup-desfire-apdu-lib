package org.esupportail.desfire.model;

import java.io.Serializable;

/**
 * Represents a DESFire key
 */
public class DesfireKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyNo; // Key number (hex string)
    private String keyValue; // Key value (hex string)
    private String keyVersion; // Key version (hex string)
    private KeyType keyType; // Key type (DES, TDES, TKTDES, AES)
    
    // Constructors
    public DesfireKey() {}
    
    public DesfireKey(String keyNo, String keyValue, KeyType keyType) {
        this.keyNo = keyNo;
        this.keyValue = keyValue;
        this.keyType = keyType;
    }
    
    public DesfireKey(String keyNo, String keyValue, String keyVersion, KeyType keyType) {
        this.keyNo = keyNo;
        this.keyValue = keyValue;
        this.keyVersion = keyVersion;
        this.keyType = keyType;
    }
    
    // Getters and Setters
    public String getKeyNo() {
        return keyNo;
    }

    public void setKeyNo(String keyNo) {
        this.keyNo = keyNo;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }
}