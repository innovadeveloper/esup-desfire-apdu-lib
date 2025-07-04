/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.desfire.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for DESFire hex string conversions and byte array manipulations
 */
public class DesfireUtils {

	protected final static Logger logger = LoggerFactory.getLogger(DesfireUtils.class);

	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/**
	 * Convert hex string to byte array
	 */
	public static byte[] hexStringToByteArray(String s) {
		if(s == null) return null;
		s = s.replace(" ", "");
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	/**
	 * Convert hex string to single byte
	 */
	public static byte hexStringToByte(String s) {
		return (byte) Integer.parseInt(s, 16);
	}

	/**
	 * Convert byte array to hex string
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length*2];
		int v;

		for(int j=0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v>>>4];
			hexChars[j*2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Swap pairs of hex characters for endianness conversion
	 */
	public static String swapPairs(byte[] byteArray) {
		String s = new StringBuilder(byteArrayToHexString(byteArray)).reverse().toString();
		String even = "";
		String odd = "";
		int length = s.length();

		for (int i = 0; i <= length-2; i+=2) {
			even += s.charAt(i+1) + "" + s.charAt(i);
		}

		if (length % 2 != 0) {
			odd = even + s.charAt(length-1);
			return odd;
		} else {
			return even;
		}
	}

	/**
	 * Swap pairs and return as byte array
	 */
	public static byte[] swapPairsByte(byte[] byteArray) {
		String swapString = swapPairs(byteArray);
		return hexStringToByteArray(swapString);
	}
}