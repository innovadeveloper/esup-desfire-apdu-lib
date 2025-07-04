package org.esupportail.desfire.core.util;

/**
 * Compute the CRC32.
 * 
 * @author Daniel Andrade
 */
public class CRC32 {

	private CRC32() {
		// cannot be instantiated
	}

	/**
	 * Compute the CRC32 of an array of bytes.
	 * 
	 * @param a	the array
	 * @return	4-byte CRC
	 */
	public static byte[] get(byte[] a) {
		return get(a, 0, a.length);
	}

	/**
	 * Calculate CRC32 as long value
	 */
	public static long calculate(byte[] data) {
		byte[] crc = get(data);
		return ((long)(crc[0] & 0xFF) << 24) | 
		       ((long)(crc[1] & 0xFF) << 16) | 
		       ((long)(crc[2] & 0xFF) << 8) | 
		       (long)(crc[3] & 0xFF);
	}

	/**
	 * Compute the CRC32 of an array of bytes.
	 * 
	 * @param a			the array
	 * @param offset	the start byte in the array
	 * @param length	the length of bytes to compute the CRC from
	 * @return			4-byte CRC
	 */
	public static byte[] get(byte[] a, int offset, int length) {
		java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		crc.update(a, offset, length);
		long l = crc.getValue();

		byte[] ret = new byte[4];
		for (int i = 0; i < 4; i++) {
			ret[i] = (byte) (l & 0x00000000000000ff);
			ret[i] = (byte) ~ret[i];
			l >>>= 8;
		}

		return ret;
	}

}