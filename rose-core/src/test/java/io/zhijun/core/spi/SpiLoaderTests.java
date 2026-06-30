package io.zhijun.core.spi;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SpiLoaderTests {

    private final SpiLoader loader = SpiLoader.defaults();

    @Test
    void loadAllReturnsAllRegisteredImplementations() {
        List<TestSpi> result = loader.loadAll(TestSpi.class);
        assertEquals(3, result.size());
    }

    @Test
    void loadAllSortsByPriorityAscending() {
        List<TestSpi> result = loader.loadAll(TestSpi.class);
        assertInstanceOf(HighPriorityService.class, result.get(0));
        assertInstanceOf(LowPriorityService.class, result.get(1));
        assertInstanceOf(NoPriorityService.class, result.get(2));
    }

    @Test
    void loadAllReturnsEmptyListWhenNoServiceFile() {
        List<NonExistentSpi> result = loader.loadAll(NonExistentSpi.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void loadFirstReturnsHighestPriorityImplementation() {
        Optional<TestSpi> result = loader.loadFirst(TestSpi.class);
        assertTrue(result.isPresent());
        assertInstanceOf(HighPriorityService.class, result.get());
    }

    @Test
    void loadFirstReturnsEmptyWhenNoServiceFile() {
        Optional<NonExistentSpi> result = loader.loadFirst(NonExistentSpi.class);
        assertTrue(!result.isPresent());
    }
}
