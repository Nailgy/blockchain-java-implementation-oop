package com.nali.blockchain.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilTest {

    @Test
    void shouldReturnKnownSha256Hash() {
        // Known SHA-256 value of "abc".
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", HashUtil.sha256("abc"));
    }

    @Test
    void smallChangeShouldGiveCompletelyDifferentHash() {
        assertNotEquals(HashUtil.sha256("block 1"), HashUtil.sha256("block 2"));
    }
}
