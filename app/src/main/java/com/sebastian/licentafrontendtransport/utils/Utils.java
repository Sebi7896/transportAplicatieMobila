package com.sebastian.licentafrontendtransport.utils;

import java.util.Arrays;

public class Utils {

    /**
     * Convertește un șir hex (fără spații) într-un array de bytes.
     * Ex: "0A1B" → {0x0A, 0x1B}
     */
    public static byte[] hexStringToByteArray(String hex) {
        String s = hex.replaceAll("\\s+", "");
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex character in \"" + s + "\"");
            }
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }

    /**
     * Convertește un array de bytes într-un șir hex (fără spații), litere mari.
     * Ex: {0x0A, 0x1B} → "0A1B"
     */
    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * Extrage un sub-array de la indexul `start` (inclusiv) până la `end` (exclusiv).
     */
    public static byte[] subarray(byte[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }
}