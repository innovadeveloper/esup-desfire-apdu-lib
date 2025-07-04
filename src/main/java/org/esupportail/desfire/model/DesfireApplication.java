package org.esupportail.desfire.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DESFire application
 */
public class DesfireApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private String desfireAppId; // 3 bytes hex string (e.g., "123456")
    private List<DesfireKey> keys = new ArrayList<>();
    private List<DesfireFile> files = new ArrayList<>();
    
    // Application settings
    private String isoId; // ISO DF ID (optional)
    private String isoName; // ISO DF Name (optional)
    private String lsbIsoId; // LSB of ISO ID
    
    // Constructors
    public DesfireApplication() {}
    
    public DesfireApplication(String desfireAppId) {
        this.desfireAppId = desfireAppId;
    }
    
    // Getters and Setters
    public String getDesfireAppId() {
        return desfireAppId;
    }

    public void setDesfireAppId(String desfireAppId) {
        this.desfireAppId = desfireAppId;
    }

    public List<DesfireKey> getKeys() {
        return keys;
    }

    public void setKeys(List<DesfireKey> keys) {
        this.keys = keys;
    }

    public List<DesfireFile> getFiles() {
        return files;
    }

    public void setFiles(List<DesfireFile> files) {
        this.files = files;
    }

    public String getIsoId() {
        return isoId;
    }

    public void setIsoId(String isoId) {
        this.isoId = isoId;
    }

    public String getIsoName() {
        return isoName;
    }

    public void setIsoName(String isoName) {
        this.isoName = isoName;
    }

    public String getLsbIsoId() {
        return lsbIsoId;
    }

    public void setLsbIsoId(String lsbIsoId) {
        this.lsbIsoId = lsbIsoId;
    }
    
    // Convenience methods
    public void addKey(DesfireKey key) {
        if (keys == null) {
            keys = new ArrayList<>();
        }
        keys.add(key);
    }
    
    public void addFile(DesfireFile file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
    }
}