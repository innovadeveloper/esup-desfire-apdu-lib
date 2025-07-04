package org.esupportail.desfire.service;

import org.esupportail.desfire.model.KeyType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for DESFireAuthService complete authentication
 */
public class DESFireAuthServiceTest {

    private DESFireAuthService authService;

    @Before
    public void setUp() {
        authService = new DESFireAuthService();
    }

    @Test
    public void testAuthenticate1AES() {
        byte keyNo = 0x00;
        String result = authService.authenticate1(keyNo, KeyType.AES);
        
        // Expected: 90AA00000100
        assertEquals("90AA00000100", result);
        assertEquals(KeyType.AES, authService.getCurrentAuthKeyType());
        assertEquals(keyNo, authService.getCurrentAuthKeyNo());
        assertTrue(authService.isAuthenticationInProgress());
    }

    @Test
    public void testAuthenticate1DES() {
        byte keyNo = 0x01;
        String result = authService.authenticate1(keyNo, KeyType.DES);
        
        // Expected: 900A00000101
        assertEquals("900A00000101", result);
        assertEquals(KeyType.DES, authService.getCurrentAuthKeyType());
    }

    @Test
    public void testAuthenticate13DES() {
        byte keyNo = 0x02;
        String result = authService.authenticate1(keyNo, KeyType.TKTDES);
        
        // Expected: 901A00000102
        assertEquals("901A00000102", result);
        assertEquals(KeyType.TKTDES, authService.getCurrentAuthKeyType());
    }

    @Test
    public void testAuthenticateCompleteAES() {
        byte[] key = new byte[16]; // AES key - 16 bytes
        byte keyNo = 0x00;
        
        boolean result = authService.authenticateComplete(keyNo, KeyType.AES, key);
        assertTrue("Authentication should succeed with valid AES key", result);
        assertEquals(KeyType.AES, authService.getCurrentAuthKeyType());
        assertEquals(keyNo, authService.getCurrentAuthKeyNo());
    }

    @Test
    public void testAuthenticateCompleteInvalidKey() {
        byte[] key = new byte[10]; // Invalid length for AES
        byte keyNo = 0x00;
        
        boolean result = authService.authenticateComplete(keyNo, KeyType.AES, key);
        assertFalse("Authentication should fail with invalid key length", result);
    }

    @Test
    public void testValidateKeys() {
        // Test DES key validation
        byte[] desKey = new byte[8];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.DES, desKey));
        
        // Test 3DES key validation  
        byte[] tdesKey = new byte[16];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.TDES, tdesKey));
        
        // Test 3K3DES key validation
        byte[] tktdesKey = new byte[24];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.TKTDES, tktdesKey));
        
        // Test AES key validation
        byte[] aesKey = new byte[16];
        assertTrue(authService.authenticateComplete((byte) 0x00, KeyType.AES, aesKey));
    }

    @Test
    public void testReset() {
        // Setup some state
        authService.authenticate1((byte) 0x00, KeyType.AES);
        assertTrue(authService.isAuthenticationInProgress());
        
        // Reset
        authService.reset();
        
        // Verify reset
        assertFalse(authService.isAuthenticated());
        assertFalse(authService.isAuthenticationInProgress());
        assertNull(authService.getCurrentAuthKeyType());
        assertEquals(-1, authService.getCurrentAuthKeyNo());
        assertNull(authService.getSessionKey());
    }

    @Test
    public void testEncryptDecryptNotAuthenticated() {
        byte[] data = "test data".getBytes();
        
        try {
            authService.encryptData(data);
            fail("Should throw exception when not authenticated");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not authenticated"));
        }
        
        try {
            authService.decryptData(data);
            fail("Should throw exception when not authenticated");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not authenticated"));
        }
    }

    @Test
    public void testCmacNotAuthenticated() {
        byte[] data = "test data".getBytes();
        
        try {
            authService.generateCmac(data);
            fail("Should throw exception when not authenticated");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Not authenticated"));
        }
    }
}