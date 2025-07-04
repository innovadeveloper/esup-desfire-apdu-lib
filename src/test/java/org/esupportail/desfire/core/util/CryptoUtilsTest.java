package org.esupportail.desfire.core.util;

import org.esupportail.desfire.service.DesfireUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for cryptographic utilities
 */
public class CryptoUtilsTest {

    @Test
    public void testHexStringToByteArray() {
        String hex = "48656C6C6F"; // "Hello" in hex
        byte[] expected = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        byte[] result = DesfireUtils.hexStringToByteArray(hex);
        
        assertArrayEquals(expected, result);
    }

    @Test
    public void testHexStringToByteArrayWithSpaces() {
        String hex = "48 65 6C 6C 6F"; // "Hello" in hex with spaces
        byte[] expected = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        byte[] result = DesfireUtils.hexStringToByteArray(hex);
        
        assertArrayEquals(expected, result);
    }

    @Test
    public void testByteArrayToHexString() {
        byte[] bytes = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        String expected = "48656C6C6F";
        String result = DesfireUtils.byteArrayToHexString(bytes);
        
        assertEquals(expected, result);
    }

    @Test
    public void testHexStringToByte() {
        String hex = "FF";
        byte expected = (byte) 0xFF;
        byte result = DesfireUtils.hexStringToByte(hex);
        
        assertEquals(expected, result);
    }

    @Test
    public void testSwapPairs() {
        byte[] input = {0x12, 0x34, 0x56};
        String result = DesfireUtils.swapPairs(input);
        
        // swapPairs reverses and swaps pairs
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testSwapPairsByte() {
        byte[] input = {0x12, 0x34, 0x56};
        byte[] result = DesfireUtils.swapPairsByte(input);
        
        assertNotNull(result);
        assertEquals(input.length, result.length);
    }

    @Test
    public void testAESEncryptDecrypt() {
        byte[] key = new byte[16]; // All zeros for test
        byte[] iv = new byte[16];  // All zeros for test
        byte[] plaintext = "Hello World 1234".getBytes(); // 16 bytes for AES block
        
        byte[] encrypted = AES.encrypt(iv, key, plaintext);
        assertNotNull("Encryption should succeed", encrypted);
        
        byte[] decrypted = AES.decrypt(iv, key, encrypted);
        assertNotNull("Decryption should succeed", decrypted);
        
        assertArrayEquals("Decrypted should match original", plaintext, decrypted);
    }

    @Test
    public void testDESEncryptDecrypt() {
        byte[] key = new byte[8]; // All zeros for test
        byte[] iv = new byte[8];  // All zeros for test
        byte[] plaintext = "12345678".getBytes(); // 8 bytes for DES block
        
        byte[] encrypted = DES.encrypt(iv, key, plaintext);
        assertNotNull("Encryption should succeed", encrypted);
        
        byte[] decrypted = DES.decrypt(iv, key, encrypted);
        assertNotNull("Decryption should succeed", decrypted);
        
        assertArrayEquals("Decrypted should match original", plaintext, decrypted);
    }

    @Test
    public void testTripleDESEncryptDecrypt() {
        byte[] key = new byte[24]; // All zeros for test
        byte[] iv = new byte[8];   // All zeros for test
        byte[] plaintext = "12345678".getBytes(); // 8 bytes for 3DES block
        
        byte[] encrypted = TripleDES.encrypt(iv, key, plaintext);
        assertNotNull("Encryption should succeed", encrypted);
        
        byte[] decrypted = TripleDES.decrypt(iv, key, encrypted);
        assertNotNull("Decryption should succeed", decrypted);
        
        assertArrayEquals("Decrypted should match original", plaintext, decrypted);
    }

    @Test
    public void testCMACGeneration() {
        byte[] key = new byte[16]; // AES key
        byte[] data = "test data".getBytes();
        
        byte[] cmac = CMAC.get(CMAC.Type.AES, key, data);
        assertNotNull("CMAC should be generated", cmac);
        assertEquals("CMAC should be 16 bytes for AES", 16, cmac.length);
        
        // Generate again - should be same
        byte[] cmac2 = CMAC.get(CMAC.Type.AES, key, data);
        assertArrayEquals("CMAC should be deterministic", cmac, cmac2);
    }

    @Test
    public void testCRC16() {
        byte[] data = "test".getBytes();
        int crc = CRC16.calculate(data);
        
        assertTrue("CRC16 should be calculated", crc >= 0);
        
        // Same data should give same CRC
        int crc2 = CRC16.calculate(data);
        assertEquals("CRC should be deterministic", crc, crc2);
    }

    @Test
    public void testCRC32() {
        byte[] data = "test".getBytes();
        long crc = CRC32.calculate(data);
        
        assertTrue("CRC32 should be calculated", crc >= 0);
        
        // Same data should give same CRC
        long crc2 = CRC32.calculate(data);
        assertEquals("CRC should be deterministic", crc, crc2);
    }
}