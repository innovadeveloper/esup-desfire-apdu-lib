package org.esupportail.desfire.examples;

import org.esupportail.desfire.service.*;
import org.esupportail.desfire.model.KeyType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Complete example demonstrating all library features
 */
public class CompleteExampleTest {

    @Test
    public void testCompleteWorkflow() {
        // 1. APDU Generation Only
        DESFireApduService apduService = new DESFireApduService();
        
        String versionApdu = apduService.getVersion();
        assertEquals("9060000000", versionApdu);
        
        byte[] aid = {0x12, 0x34, 0x56};
        String selectApdu = apduService.selectApplication(aid);
        assertEquals("905A000003123456", selectApdu);
        
        // 2. Authentication Service
        DESFireAuthService authService = new DESFireAuthService();
        
        // Test authentication step 1
        String auth1 = authService.authenticate1((byte) 0x00, KeyType.AES);
        assertEquals("90AA00000100", auth1);
        
        // Test complete authentication setup
        byte[] aesKey = new byte[16]; // Test key
        boolean authSetup = authService.authenticateComplete((byte) 0x00, KeyType.AES, aesKey);
        assertTrue("Authentication setup should succeed", authSetup);
        
        // 3. Complete Service
        DESFireCompleteService completeService = new DESFireCompleteService();
        
        // Test APDU string generation
        String getVersionApdu = completeService.getApduString("GET_VERSION");
        assertEquals("9060000000", getVersionApdu);
        
        String createAppApdu = completeService.getApduString("CREATE_APPLICATION", 
                                    aid, (byte) 0x0F, (byte) 0x01);
        assertEquals("90CA0000051234560F01", createAppApdu);
        
        // Test service access
        assertNotNull(completeService.getAuthService());
        assertNotNull(completeService.getApduService());
        
        System.out.println("✅ All basic functionality tests passed!");
    }

    @Test
    public void testCryptographicUtilities() {
        // Test hex conversions
        String hex = "48656C6C6F"; // "Hello"
        byte[] bytes = DesfireUtils.hexStringToByteArray(hex);
        String hexBack = DesfireUtils.byteArrayToHexString(bytes);
        assertEquals(hex, hexBack);
        
        // Test authentication with different key types
        DESFireAuthService authService = new DESFireAuthService();
        
        // DES key (8 bytes)
        byte[] desKey = new byte[8];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.DES, desKey));
        
        // 3DES key (16 bytes)
        byte[] tdesKey = new byte[16];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.TDES, tdesKey));
        
        // 3K3DES key (24 bytes)
        byte[] tktdesKey = new byte[24];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.TKTDES, tktdesKey));
        
        // AES key (16 bytes)
        byte[] aesKey = new byte[16];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.AES, aesKey));
        
        System.out.println("✅ All cryptographic utility tests passed!");
    }

    @Test
    public void testFileOperations() {
        DESFireApduService service = new DESFireApduService();
        
        // Test file creation
        byte fileNo = 0x01;
        byte commSettings = 0x00;
        byte[] accessRights = {0x00, 0x00};
        int fileSize = 32;
        
        String createFileApdu = service.createStdDataFile(fileNo, commSettings, accessRights, fileSize);
        assertTrue("Create file APDU should be valid", createFileApdu.length() > 0);
        assertEquals("90CD000007010000002000", createFileApdu);
        
        // Test data operations
        byte[] testData = "Test data".getBytes();
        String writeApdu = service.writeData(fileNo, 0, testData);
        assertTrue("Write APDU should contain data", writeApdu.contains(DesfireUtils.byteArrayToHexString(testData)));
        
        String readApdu = service.readData(fileNo, 0, testData.length);
        assertTrue("Read APDU should be valid", readApdu.length() > 0);
        
        System.out.println("✅ All file operation tests passed!");
    }

    @Test
    public void testErrorHandling() {
        DESFireAuthService authService = new DESFireAuthService();
        
        // Test invalid key length
        byte[] invalidKey = new byte[10]; // Invalid for AES
        boolean result = authService.authenticateComplete((byte) 0x00, KeyType.AES, invalidKey);
        assertFalse("Should fail with invalid key length", result);
        
        // Test operations when not authenticated
        try {
            authService.encryptData("test".getBytes());
            fail("Should throw exception when not authenticated");
        } catch (Exception e) {
            assertTrue("Should mention authentication", e.getMessage().contains("Not authenticated"));
        }
        
        DESFireCompleteService completeService = new DESFireCompleteService();
        
        // Test unknown command
        try {
            completeService.getApduString("UNKNOWN_COMMAND");
            fail("Should throw exception for unknown command");
        } catch (Exception e) {
            assertTrue("Should mention unknown command", e.getMessage().contains("Unknown command"));
        }
        
        System.out.println("✅ All error handling tests passed!");
    }

    /**
     * Demonstration method showing typical usage patterns
     */
    public void demonstrateUsage() {
        System.out.println("=== DESFire APDU Library Usage Demo ===\n");
        
        // 1. Simple APDU generation
        System.out.println("1. Simple APDU Generation:");
        DESFireApduService apduService = new DESFireApduService();
        System.out.println("   Get Version: " + apduService.getVersion());
        System.out.println("   Get Memory:  " + apduService.getFreeMemory());
        
        // 2. Authentication
        System.out.println("\n2. Authentication:");
        DESFireAuthService authService = new DESFireAuthService();
        byte[] key = new byte[16]; // AES key
        String auth1 = authService.authenticate1((byte) 0x00, KeyType.AES);
        System.out.println("   Auth Step 1: " + auth1);
        
        // 3. Complete service
        System.out.println("\n3. Complete Service:");
        DESFireCompleteService completeService = new DESFireCompleteService();
        byte[] aid = {0x12, 0x34, 0x56};
        String createApp = completeService.getApduString("CREATE_APPLICATION", aid, (byte) 0x0F, (byte) 0x01);
        System.out.println("   Create App:  " + createApp);
        
        // 4. Crypto utilities
        System.out.println("\n4. Crypto Utilities:");
        String hex = DesfireUtils.byteArrayToHexString("Hello".getBytes());
        System.out.println("   'Hello' in hex: " + hex);
        
        System.out.println("\n=== Demo Complete ===");
    }
}