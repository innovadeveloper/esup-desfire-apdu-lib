/* ****************************************
 * Copyright (c) 2013, Daniel Andrade
 * All rights reserved.
 * 
 * (1) Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. (2) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. (3) The name of the author may not be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Modified BSD License (3-clause BSD)
 */
package org.esupportail.desfire.core;

import org.esupportail.desfire.core.util.*;
import org.esupportail.desfire.model.KeyType;
import org.esupportail.desfire.exceptions.DesfireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Core DESFire EV1 implementation with direct card communication
 * This class handles actual APDU transmission and responses
 * 
 * @author Daniel Andrade (adapted)
 * @version 1.0.0
 */
public class DESFireEV1 extends SimpleSCR {
    
    protected final static Logger log = LoggerFactory.getLogger(DESFireEV1.class);
    
    /** A file/key number that does not exist. */
    private final static byte FAKE_NO = -1;

    // Command codes
    public enum Command {
        CREATE_APPLICATION(0xCA),
        DELETE_APPLICATION(0xDA),
        GET_APPLICATION_IDS(0x6A),
        SELECT_APPLICATION(0x5A),
        FORMAT_PICC(0xFC),
        GET_VERSION(0x60),
        FREE_MEMORY(0x6E),
        SET_CONFIGURATION(0x5C),
        GET_CARD_UID(0x51),
        GET_FILE_IDS(0x6F),
        GET_FILE_SETTINGS(0xF5),
        CHANGE_FILE_SETTINGS(0x5F),
        CREATE_STD_DATA_FILE(0xCD),
        CREATE_BACKUP_DATA_FILE(0xCB),
        CREATE_LINEAR_RECORD_FILE(0xC1),
        CREATE_CYCLIC_RECORD_FILE(0xC0),
        DELETE_FILE(0xDF),
        READ_DATA(0xBD),
        WRITE_DATA(0x3D),
        GET_VALUE(0x6C),
        CREDIT(0x0C),
        DEBIT(0xDC),
        LIMITED_CREDIT(0x1C),
        WRITE_RECORD(0x3B),
        READ_RECORDS(0xBB),
        CLEAR_RECORD_FILE(0xEB),
        COMMIT_TRANSACTION(0xC7),
        ABORT_TRANSACTION(0xA7),
        AUTHENTICATE_DES_2K3DES(0x0A),
        AUTHENTICATE_3K3DES(0x1A),
        AUTHENTICATE_AES(0xAA),
        CHANGE_KEY_SETTINGS(0x54),
        GET_KEY_SETTINGS(0x45),
        CHANGE_KEY(0xC4),
        GET_KEY_VERSION(0x64);

        private final int code;

        Command(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    // Response codes
    public enum Response {
        OPERATION_OK(0x00),
        NO_CHANGES(0x0C),
        OUT_OF_EEPROM_ERROR(0x0E),
        ILLEGAL_COMMAND_CODE(0x1C),
        INTEGRITY_ERROR(0x1E),
        NO_SUCH_KEY(0x40),
        LENGTH_ERROR(0x7E),
        PERMISSION_DENIED(0x9D),
        PARAMETER_ERROR(0x9E),
        APPLICATION_NOT_FOUND(0xA0),
        APPL_INTEGRITY_ERROR(0xA1),
        AUTHENTICATION_ERROR(0xAE),
        ADDITIONAL_FRAME(0xAF),
        BOUNDARY_ERROR(0xBE),
        PICC_INTEGRITY_ERROR(0xC1),
        COMMAND_ABORTED(0xCA),
        PICC_DISABLED_ERROR(0xCD),
        COUNT_ERROR(0xCE),
        DUPLICATE_ERROR(0xDE),
        EEPROM_ERROR(0xEE),
        FILE_NOT_FOUND(0xF0),
        FILE_INTEGRITY_ERROR(0xF1);

        private final int code;

        Response(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Response getResponse(int code) {
            for (Response r : Response.values()) {
                if (r.getCode() == code) {
                    return r;
                }
            }
            return null;
        }
    }

    // Communication settings
    public enum CommunicationSetting {
        PLAIN,
        MACED,
        ENCIPHERED
    }

    // Instance variables
    private KeyType ktype;
    private byte kno;
    private byte[] aid;
    private byte[] iv;
    private byte[] skey;
    private byte fileNo;
    private byte[] fileSett;
    private int code;

    public DESFireEV1() {
        reset();
        aid = new byte[3];
    }

    @Override
    public boolean disconnect() {
        reset();
        return super.disconnect();
    }

    /**
     * Reset the attributes of this instance to their default values.
     */
    private void reset() {
        ktype = null;
        kno = FAKE_NO;
        iv = null;
        skey = null;
        fileNo = FAKE_NO;
        fileSett = null;
    }

    // ================ AUTHENTICATION ================

    /**
     * Mutual authentication between PCD and PICC.
     */
    public byte[] authenticate(byte[] key, byte keyNo, KeyType type) {
        if (!validateKey(key, type))
            return null;
        if (type != KeyType.AES) {
            setKeyVersion(key, 0, key.length, (byte) 0x00);
        }

        final byte[] iv0 = type == KeyType.AES ? new byte[16] : new byte[8];
        byte[] apdu;
        CommandAPDU command;
        ResponseAPDU response;

        // 1st message exchange
        apdu = new byte[7];
        apdu[0] = (byte) 0x90;
        switch (type) {
            case DES:
            case TDES:
                apdu[1] = (byte) Command.AUTHENTICATE_DES_2K3DES.getCode();
                break;
            case TKTDES:
                apdu[1] = (byte) Command.AUTHENTICATE_3K3DES.getCode();
                break;
            case AES:
                apdu[1] = (byte) Command.AUTHENTICATE_AES.getCode();
                break;
            default:
                throw new DesfireException("Invalid key type: " + type);
        }
        apdu[4] = 0x01;
        apdu[5] = keyNo;
        command = new CommandAPDU(apdu);
        response = transmit(command);
        this.code = response.getSW2();
        
        if (response.getSW2() != 0xAF)
            return null;

        // step 3
        byte[] randB = recv(key, response.getData(), type, iv0);
        if (randB == null)
            return null;
        byte[] randBr = rotateLeft(randB);
        byte[] randA = new byte[randB.length];
        new SecureRandom().nextBytes(randA);

        // step 3: encryption
        byte[] plaintext = new byte[randA.length + randBr.length];
        System.arraycopy(randA, 0, plaintext, 0, randA.length);
        System.arraycopy(randBr, 0, plaintext, randA.length, randBr.length);
        byte[] iv1 = Arrays.copyOfRange(response.getData(),
                response.getData().length - iv0.length, response.getData().length);
        byte[] ciphertext = send(key, plaintext, type, iv1);
        if (ciphertext == null)
            return null;

        // 2nd message exchange
        apdu = new byte[5 + ciphertext.length + 1];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) 0xAF;
        apdu[4] = (byte) ciphertext.length;
        System.arraycopy(ciphertext, 0, apdu, 5, ciphertext.length);
        command = new CommandAPDU(apdu);
        response = transmit(command);
        this.code = response.getSW2();
        
        if (response.getSW2() != 0x00)
            return null;

        // step 5
        byte[] iv2 = Arrays.copyOfRange(ciphertext,
                ciphertext.length - iv0.length, ciphertext.length);
        byte[] randAr = recv(key, response.getData(), type, iv2);
        if (randAr == null)
            return null;
        byte[] randAr2 = rotateLeft(randA);
        for (int i = 0; i < randAr2.length; i++)
            if (randAr[i] != randAr2[i])
                return null;

        // step 6 - generate session key
        byte[] sessionKey = generateSessionKey(randA, randB, type);

        this.ktype = type;
        this.kno = keyNo;
        this.iv = iv0;
        this.skey = sessionKey;

        return sessionKey;
    }

    // ================ PICC LEVEL COMMANDS ================

    /**
     * Get version information from PICC
     */
    public byte[] getVersion() {
        byte[] apdu = {(byte) 0x90, (byte) Command.GET_VERSION.getCode(), 0x00, 0x00, 0x00};
        return readDataFromCard(apdu);
    }

    /**
     * Format PICC (destroys all data)
     */
    public boolean formatPICC() {
        byte[] apdu = {(byte) 0x90, (byte) Command.FORMAT_PICC.getCode(), 0x00, 0x00, 0x00};
        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();
        
        if (response.getSW2() == Response.OPERATION_OK.getCode()) {
            reset(); // Formatting resets authentication
            return true;
        }
        return false;
    }

    /**
     * Get free memory
     */
    public int getFreeMemory() {
        byte[] apdu = {(byte) 0x90, (byte) Command.FREE_MEMORY.getCode(), 0x00, 0x00, 0x00};
        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();
        
        if (response.getSW2() == Response.OPERATION_OK.getCode() && response.getData().length >= 3) {
            byte[] data = response.getData();
            return (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16);
        }
        return -1;
    }

    /**
     * Get application IDs
     */
    public byte[] getApplicationIds() {
        return readDataFromCard(Command.GET_APPLICATION_IDS);
    }

    /**
     * Create application
     */
    public boolean createApplication(byte[] aid, byte keySettings, byte numberOfKeys) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }

        byte[] apdu = new byte[10];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.CREATE_APPLICATION.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x05;
        System.arraycopy(aid, 0, apdu, 5, 3);
        apdu[8] = keySettings;
        apdu[9] = numberOfKeys;

        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();

        return response.getSW2() == Response.OPERATION_OK.getCode();
    }

    /**
     * Delete application
     */
    public boolean deleteApplication(byte[] aid) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }

        byte[] apdu = new byte[8];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.DELETE_APPLICATION.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x03;
        System.arraycopy(aid, 0, apdu, 5, 3);

        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();

        return response.getSW2() == Response.OPERATION_OK.getCode();
    }

    /**
     * Select application
     */
    public boolean selectApplication(byte[] aid) {
        if (aid.length != 3) {
            throw new DesfireException("AID must be 3 bytes");
        }

        byte[] apdu = new byte[8];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.SELECT_APPLICATION.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x03;
        System.arraycopy(aid, 0, apdu, 5, 3);

        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();

        if (response.getSW2() == Response.OPERATION_OK.getCode()) {
            System.arraycopy(aid, 0, this.aid, 0, 3);
            reset(); // Selection resets authentication
            return true;
        }
        return false;
    }

    // ================ FILE OPERATIONS ================

    /**
     * Create standard data file
     */
    public boolean createStdDataFile(byte fileNo, byte commSettings, byte[] accessRights, int fileSize) {
        byte[] apdu = new byte[12];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.CREATE_STD_DATA_FILE.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07;
        apdu[5] = fileNo;
        apdu[6] = commSettings;
        System.arraycopy(accessRights, 0, apdu, 7, 2);
        apdu[9] = (byte) (fileSize & 0xFF);
        apdu[10] = (byte) ((fileSize >> 8) & 0xFF);
        apdu[11] = (byte) ((fileSize >> 16) & 0xFF);

        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();

        return response.getSW2() == Response.OPERATION_OK.getCode();
    }

    /**
     * Read data from file
     */
    public byte[] readData(byte fileNo, int offset, int length) {
        byte[] apdu = new byte[12];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.READ_DATA.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = 0x07;
        apdu[5] = fileNo;
        apdu[6] = (byte) (offset & 0xFF);
        apdu[7] = (byte) ((offset >> 8) & 0xFF);
        apdu[8] = (byte) ((offset >> 16) & 0xFF);
        apdu[9] = (byte) (length & 0xFF);
        apdu[10] = (byte) ((length >> 8) & 0xFF);
        apdu[11] = (byte) ((length >> 16) & 0xFF);

        return readDataFromCard(apdu);
    }

    /**
     * Write data to file
     */
    public boolean writeData(byte fileNo, int offset, byte[] data) {
        byte[] apdu = new byte[12 + data.length];
        apdu[0] = (byte) 0x90;
        apdu[1] = (byte) Command.WRITE_DATA.getCode();
        apdu[2] = 0x00;
        apdu[3] = 0x00;
        apdu[4] = (byte) (7 + data.length);
        apdu[5] = fileNo;
        apdu[6] = (byte) (offset & 0xFF);
        apdu[7] = (byte) ((offset >> 8) & 0xFF);
        apdu[8] = (byte) ((offset >> 16) & 0xFF);
        apdu[9] = (byte) (data.length & 0xFF);
        apdu[10] = (byte) ((data.length >> 8) & 0xFF);
        apdu[11] = (byte) ((data.length >> 16) & 0xFF);
        System.arraycopy(data, 0, apdu, 12, data.length);

        CommandAPDU command = new CommandAPDU(apdu);
        ResponseAPDU response = transmit(command);
        this.code = response.getSW2();

        return response.getSW2() == Response.OPERATION_OK.getCode();
    }

    // ================ UTILITY METHODS ================

    private byte[] readDataFromCard(Command cmd) {
        byte[] apdu = {(byte) 0x90, (byte) cmd.getCode(), 0x00, 0x00, 0x00};
        return readDataFromCard(apdu);
    }

    private byte[] readDataFromCard(byte[] apdu) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            CommandAPDU command = new CommandAPDU(apdu);
            ResponseAPDU response = transmit(command);
            this.code = response.getSW2();

            while (response.getSW2() == Response.ADDITIONAL_FRAME.getCode()) {
                result.write(response.getData());
                // Send additional frame command
                byte[] moreApdu = {(byte) 0x90, (byte) 0xAF, 0x00, 0x00, 0x00};
                command = new CommandAPDU(moreApdu);
                response = transmit(command);
                this.code = response.getSW2();
            }

            if (response.getSW2() == Response.OPERATION_OK.getCode()) {
                result.write(response.getData());
                return result.toByteArray();
            }

            return null;
        } catch (IOException e) {
            log.error("Error reading data from card", e);
            return null;
        }
    }

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

    private byte[] rotateLeft(byte[] data) {
        if (data == null || data.length == 0) return data;
        byte[] result = new byte[data.length];
        System.arraycopy(data, 1, result, 0, data.length - 1);
        result[data.length - 1] = data[0];
        return result;
    }

    private byte[] generateSessionKey(byte[] randA, byte[] randB, KeyType type) {
        switch (type) {
            case DES:
                byte[] skey8 = new byte[8];
                System.arraycopy(randA, 0, skey8, 0, 4);
                System.arraycopy(randB, 0, skey8, 4, 4);
                return skey8;

            case TDES:
                byte[] skey16 = new byte[16];
                System.arraycopy(randA, 0, skey16, 0, 4);
                System.arraycopy(randB, 0, skey16, 4, 4);
                System.arraycopy(randA, 4, skey16, 8, 4);
                System.arraycopy(randB, 4, skey16, 12, 4);
                return skey16;

            case TKTDES:
                byte[] skey24 = new byte[24];
                System.arraycopy(randA, 0, skey24, 0, 4);
                System.arraycopy(randB, 0, skey24, 4, 4);
                System.arraycopy(randA, 6, skey24, 8, 4);
                System.arraycopy(randB, 6, skey24, 12, 4);
                System.arraycopy(randA, 12, skey24, 16, 4);
                System.arraycopy(randB, 12, skey24, 20, 4);
                return skey24;

            case AES:
                byte[] skeyAES = new byte[16];
                System.arraycopy(randA, 0, skeyAES, 0, 4);
                System.arraycopy(randB, 0, skeyAES, 4, 4);
                System.arraycopy(randA, 12, skeyAES, 8, 4);
                System.arraycopy(randB, 12, skeyAES, 12, 4);
                return skeyAES;

            default:
                throw new DesfireException("Unknown key type: " + type);
        }
    }

    private byte[] send(byte[] key, byte[] data, KeyType type, byte[] iv) {
        switch (type) {
            case DES:
                return DES.encrypt(iv, key, data);
            case TDES:
            case TKTDES:
                return TripleDES.encrypt(iv, key, data);
            case AES:
                return AES.encrypt(iv, key, data);
            default:
                return null;
        }
    }

    private byte[] recv(byte[] key, byte[] data, KeyType type, byte[] iv) {
        switch (type) {
            case DES:
                return DES.decrypt(iv, key, data);
            case TDES:
            case TKTDES:
                return TripleDES.decrypt(iv, key, data);
            case AES:
                return AES.decrypt(iv, key, data);
            default:
                return null;
        }
    }

    // ================ GETTERS ================

    public int getLastResponseCode() {
        return code;
    }

    public boolean isAuthenticated() {
        return skey != null;
    }

    public KeyType getCurrentKeyType() {
        return ktype;
    }

    public byte getCurrentKeyNo() {
        return kno;
    }

    public byte[] getCurrentAid() {
        return aid != null ? aid.clone() : null;
    }

    public byte[] getSessionKey() {
        return skey != null ? skey.clone() : null;
    }
}