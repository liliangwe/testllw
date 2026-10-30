package com.example.demo.algorithm;

import com.example.demo.algorithm.dto.BubbleSortResult;
import com.example.demo.common.BizException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DemoAlgorithmServiceImpl 单元测试。
 */
class DemoAlgorithmServiceImplTest {

    private final DemoAlgorithmServiceImpl service = new DemoAlgorithmServiceImpl();

    @Test
    void should_returnHelloWorld_when_nameAbsent() {
        Map<String, String> data = service.sayHello(null);
        assertEquals("Hello, World!", data.get("message"));
        assertTrue(data.containsKey("timestamp"));
    }

    @Test
    void should_returnHelloName_when_nameGiven() {
        Map<String, String> data = service.sayHello("Tom");
        assertEquals("Hello, Tom!", data.get("message"));
    }

    @Test
    void should_throwDemo001_when_nameTooLong() {
        String longName = "a".repeat(51);
        BizException e = assertThrows(BizException.class, () -> service.sayHello(longName));
        assertEquals("DEMO_001", e.getErrorCode());
    }

    @Test
    void should_returnSha256Digest_when_algorithmDefault() {
        Map<String, Object> data = service.hash("hello", null);
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", data.get("digest"));
        assertEquals("SHA256", data.get("algorithm"));
        assertEquals(5, data.get("inputLength"));
    }

    @Test
    void should_returnMd5Digest_when_algorithmMd5() {
        Map<String, Object> data = service.hash("hello", "MD5");
        assertEquals("5d41402abc4b2a76b9719d911017c592", data.get("digest"));
    }

    @Test
    void should_throwDemo002_when_algorithmUnsupported() {
        BizException e = assertThrows(BizException.class, () -> service.hash("hello", "SHA512"));
        assertEquals("DEMO_002", e.getErrorCode());
    }

    @Test
    void should_throwDemo001_when_inputBlank() {
        assertThrows(BizException.class, () -> service.hash("", null));
    }

    @Test
    void should_sortAsc_when_orderAsc() {
        BubbleSortResult result = service.bubbleSort(List.of(5, 3, 8, 1), "ASC");
        assertEquals(List.of(1, 3, 5, 8), result.sorted());
        assertTrue(result.compareCount() > 0);
        assertTrue(result.swapCount() > 0);
    }

    @Test
    void should_sortDesc_when_orderDesc() {
        BubbleSortResult result = service.bubbleSort(List.of(5, 3, 8, 1), "DESC");
        assertEquals(List.of(8, 5, 3, 1), result.sorted());
    }

    @Test
    void should_exitEarly_when_alreadySorted() {
        BubbleSortResult result = service.bubbleSort(List.of(1, 2, 3, 4), null);
        assertEquals(List.of(1, 2, 3, 4), result.sorted());
        assertEquals(0, result.swapCount());
        assertEquals(3, result.compareCount());
    }

    @Test
    void should_throwDemo001_when_numbersEmpty() {
        assertThrows(BizException.class, () -> service.bubbleSort(List.of(), null));
    }
}
