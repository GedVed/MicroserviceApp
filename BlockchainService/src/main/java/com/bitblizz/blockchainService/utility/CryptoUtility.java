package com.bitblizz.blockchainService.utility;


import java.security.*;


public class CryptoUtility {
    public static String hash(String... args) {
        StringBuilder hashingText = new StringBuilder();
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-256");

            for (String arg : args) {
                hashingText.append(arg);
            }

            byte[] hashedBytes = md.digest(hashingText.toString().getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                String hex = Integer.toHexString(0xff & hashedByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
