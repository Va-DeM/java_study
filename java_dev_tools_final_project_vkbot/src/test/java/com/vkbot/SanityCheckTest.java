package com.vkbot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SanityCheckTest {

    @Test
    void junitAndMockitoAreWired() {
        Runnable mock = Mockito.mock(Runnable.class);
        mock.run();
        Mockito.verify(mock).run();

        assertEquals(4, 2 + 2);
    }
}
