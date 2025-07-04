package org.esupportail.desfire.model;

import java.io.Serializable;

/**
 * Represents a DESFire file
 */
public class DesfireFile implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileNumber; // File number (hex string)
    private String fileSize; // File size (hex string)
    private String fileSettings; // File settings (hex string)
    private String fileKey; // File access key
    private String communicationSettings; // Communication settings
    private String accessRights; // Access rights (hex string)
    private String initialValue; // Initial data value
    
    // Constructors
    public DesfireFile() {}
    
    public DesfireFile(String fileNumber, String fileSize) {
        this.fileNumber = fileNumber;
        this.fileSize = fileSize;
    }
    
    // Getters and Setters
    public String getFileNumber() {
        return fileNumber;
    }

    public void setFileNumber(String fileNumber) {
        this.fileNumber = fileNumber;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSettings() {
        return fileSettings;
    }

    public void setFileSettings(String fileSettings) {
        this.fileSettings = fileSettings;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getCommunicationSettings() {
        return communicationSettings;
    }

    public void setCommunicationSettings(String communicationSettings) {
        this.communicationSettings = communicationSettings;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }
}