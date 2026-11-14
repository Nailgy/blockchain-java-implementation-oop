package com.nali.blockchain.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Helper to calculate SHA-256 hashes.
 */
public final class HashUtil {

    // Only static methods: nobody should create an instance of this class.
    private HashUtil() {
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // Every Java installation must support SHA-256, so this only happens with a broken JDK.
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
