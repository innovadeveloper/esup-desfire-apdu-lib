package org.esupportail.desfire.service;

import org.esupportail.desfire.model.KeyType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for DESFireCompleteService
 */
public class DESFireCompleteServiceTest {

    private DESFireCompleteService service;

    @Before
    public void setUp() {
        service = new DESFireCompleteService();
    }

    @Test
    public void testGetApduStringGetVersion() {
        String result = service.getApduString("GET_VERSION");
        assertEquals("9060000000", result);
    }

    @Test
    public void testGetApduStringSelectApplication() {
        byte[] aid = {0x12, 0x34, 0x56};
        String result = service.getApduString("SELECT_APPLICATION", aid);
        assertEquals("905A000003123456", result);
    }

    @Test
    public void testGetApduStringCreateApplication() {
        byte[] aid = {0x12, 0x34, 0x56};
        byte keySettings = 0x0F;
        byte numberOfKeys = 0x01;
        
        String result = service.getApduString("CREATE_APPLICATION", aid, keySettings, numberOfKeys);
        assertEquals("90CA0000051234560F01", result);
    }

    @Test
    public void testGetApduStringAuthenticate() {
        byte keyNo = 0x00;
        String result = service.getApduString("AUTHENTICATE", keyNo, KeyType.AES);
        assertEquals("90AA00000100", result);
    }

    @Test
    public void testGetApduStringCreateStdDataFile() {
        byte fileNo = 0x01;
        byte commSettings = 0x00;
        byte[] accessRights = {0x00, 0x00};
        int fileSize = 32;
        
        String result = service.getApduString("CREATE_STD_DATA_FILE", fileNo, commSettings, accessRights, fileSize);
        assertEquals("90CD000007010000002000", result);
    }

    @Test
    public void testGetApduStringReadData() {
        byte fileNo = 0x01;
        int offset = 0;
        int length = 16;
        
        String result = service.getApduString("READ_DATA", fileNo, offset, length);
        assertEquals("90BD000007010000001000", result);
    }

    @Test
    public void testGetApduStringWriteData() {
        byte fileNo = 0x01;
        int offset = 0;
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        
        String result = service.getApduString("WRITE_DATA", fileNo, offset, data);
        assertTrue("Should contain file number and data", result.contains("01"));
        assertTrue("Should contain Hello in hex", result.contains("48656C6C6F"));
    }

    @Test
    public void testGetApduStringUnknownCommand() {
        try {
            service.getApduString("UNKNOWN_COMMAND");
            fail("Should throw exception for unknown command");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Unknown command"));
        }
    }

    @Test
    public void testGetKeyLength() {
        // Test through a method that uses getKeyLength internally
        byte[] desKey = new byte[8];
        byte[] tdesKey = new byte[16];
        byte[] tktdesKey = new byte[24];
        byte[] aesKey = new byte[16];
        
        // These should not throw exceptions
        service.initializeCard(desKey, KeyType.DES);
        service.initializeCard(tdesKey, KeyType.TDES);
        service.initializeCard(tktdesKey, KeyType.TKTDES);
        service.initializeCard(aesKey, KeyType.AES);
    }

    @Test
    public void testServiceAccessors() {
        assertNotNull("Auth service should be available", service.getAuthService());
        assertNotNull("APDU service should be available", service.getApduService());
        
        assertTrue("Auth service should be DESFireAuthService", 
                   service.getAuthService() instanceof DESFireAuthService);
        assertTrue("APDU service should be DESFireApduService", 
                   service.getApduService() instanceof DESFireApduService);
    }
}