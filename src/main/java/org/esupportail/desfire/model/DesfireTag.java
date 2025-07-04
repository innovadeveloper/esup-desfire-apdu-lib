package org.esupportail.desfire.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DESFire EV1 tag with its applications
 */
public class DesfireTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DesfireApplication> applications = new ArrayList<>();
    private boolean formatBeforeWrite = false;
    
    // PICC master key settings
    private String keyStart = "0000000000000000";
    private KeyType keyTypeStart = KeyType.DES;
    private String keyFinish;
    private String keyVersionFinish;
    private KeyType keyTypeFinish;
    
    // Constructors
    public DesfireTag() {}
    
    public DesfireTag(List<DesfireApplication> applications) {
        this.applications = applications;
    }
    
    // Getters and Setters
    public List<DesfireApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<DesfireApplication> applications) {
        this.applications = applications;
    }

    public boolean isFormatBeforeWrite() {
        return formatBeforeWrite;
    }

    public void setFormatBeforeWrite(boolean formatBeforeWrite) {
        this.formatBeforeWrite = formatBeforeWrite;
    }

    public String getKeyStart() {
        return keyStart;
    }

    public void setKeyStart(String keyStart) {
        this.keyStart = keyStart;
    }

    public KeyType getKeyTypeStart() {
        return keyTypeStart;
    }

    public void setKeyTypeStart(KeyType keyTypeStart) {
        this.keyTypeStart = keyTypeStart;
    }

    public String getKeyFinish() {
        return keyFinish;
    }

    public void setKeyFinish(String keyFinish) {
        this.keyFinish = keyFinish;
    }

    public String getKeyVersionFinish() {
        return keyVersionFinish;
    }

    public void setKeyVersionFinish(String keyVersionFinish) {
        this.keyVersionFinish = keyVersionFinish;
    }

    public KeyType getKeyTypeFinish() {
        return keyTypeFinish;
    }

    public void setKeyTypeFinish(KeyType keyTypeFinish) {
        this.keyTypeFinish = keyTypeFinish;
    }
    
    // Convenience methods
    public void addApplication(DesfireApplication application) {
        if (applications == null) {
            applications = new ArrayList<>();
        }
        applications.add(application);
    }
}