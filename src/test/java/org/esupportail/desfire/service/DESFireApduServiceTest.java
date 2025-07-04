package org.esupportail.desfire.service;

import org.esupportail.desfire.model.KeyType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for DESFireApduService
 */
public class DESFireApduServiceTest {

    private DESFireApduService service;

    @Before
    public void setUp() {
        service = new DESFireApduService();
    }

    @Test
    public void testGetVersion() {
        String result = service.getVersion();
        assertEquals("9060000000", result);
    }

    @Test
    public void testGetFreeMemory() {
        String result = service.getFreeMemory();
        assertEquals("906E000000", result);
    }

    @Test
    public void testSelectApplication() {
        byte[] aid = {0x00, 0x00, 0x00};
        String result = service.selectApplication(aid);
        assertEquals("905A000003000000", result);
    }

    @Test
    public void testCreateApplication() {
        byte[] aid = {0x12, 0x34, 0x56};
        byte keySettings = 0x0F;
        byte numberOfKeys = 0x01;
        
        String result = service.createApplication(aid, keySettings, numberOfKeys);
        assertEquals("90CA00000512345601", result);
    }

    @Test
    public void testAuthenticate() {
        byte keyNo = 0x00;
        String result = service.authenticate(keyNo, KeyType.AES);
        assertEquals("90AA00000100", result);
    }

    @Test
    public void testCreateStdDataFile() {
        byte fileNo = 0x01;
        byte commSettings = 0x00;
        byte[] accessRights = {0x00, 0x00};
        int fileSize = 32;
        
        String result = service.createStdDataFile(fileNo, commSettings, accessRights, fileSize);
        assertEquals("90CD000007010000002000", result);
    }

    @Test
    public void testReadData() {
        byte fileNo = 0x01;
        int offset = 0;
        int length = 16;
        
        String result = service.readData(fileNo, offset, length);
        assertEquals("90BD000007010000001000", result);
    }

    @Test
    public void testWriteData() {
        byte fileNo = 0x01;
        int offset = 0;
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        
        String result = service.writeData(fileNo, offset, data);
        assertEquals("90BD000007010000000500", result.substring(0, 20)); // Check header
        assertTrue(result.contains("48656C6C6F")); // Check data
    }

    @Test
    public void testReset() {
        service.authenticate((byte)0x01, KeyType.AES);
        service.reset();
        
        assertNull(service.getCurrentKeyType());
        assertEquals(-1, service.getCurrentKeyNo());
        assertFalse(service.isAuthenticated());
    }
}