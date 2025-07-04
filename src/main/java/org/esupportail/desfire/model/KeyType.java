package org.esupportail.desfire.model;

/**
 * DESFire key types
 */
public enum KeyType {
    DES,    // DES 8 bytes
    TDES,   // 3DES 16 bytes  
    TKTDES, // 3K3DES 24 bytes
    AES     // AES 16 bytes
}