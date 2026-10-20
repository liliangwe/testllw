package com.example.demo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmServiceTest {

    private final AlgorithmService service = new AlgorithmService();

    @Test
    void helloUsesWorldWhenNameMissingOrEmpty() {
        assertEquals("Hello, World!", service.hello(null));
        assertEquals("Hello, World!", service.hello(""));
    }

    @Test
    void helloWithProvidedName() {
        assertEquals("Hello, Alice!", service.hello("Alice"));
    }

    @Test
    void sha256MatchesKnownVector() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                service.sha256("abc"));
    }

    @Test
    void sha256IsLowercaseHexOfLength64() {
        String digest = service.sha256("example");
        assertEquals(64, digest.length());
        assertTrue(digest.matches("[0-9a-f]{64}"));
    }

    @Test
    void bubbleSortReturnsSortedArrayWithSwaps() {
        BubbleSortResult result = service.bubbleSortWithSwaps(new int[]{5, 3, 8, 1});
        assertArrayEquals(new int[]{1, 3, 5, 8}, result.sorted());
        assertEquals(4, result.swaps());
    }

    @Test
    void bubbleSortDoesNotMutateInput() {
        int[] input = {5, 3, 8, 1};
        service.bubbleSort(input);
        assertArrayEquals(new int[]{5, 3, 8, 1}, input);
    }

    @Test
    void bubbleSortAlreadySortedHasZeroSwaps() {
        BubbleSortResult result = service.bubbleSortWithSwaps(new int[]{1, 2, 3});
        assertArrayEquals(new int[]{1, 2, 3}, result.sorted());
        assertEquals(0, result.swaps());
    }
}