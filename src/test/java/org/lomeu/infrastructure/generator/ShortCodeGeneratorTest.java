package org.lomeu.infrastructure.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShortCodeGeneratorTest {

    @Test
    void shouldGenerateBase62() {
        ShortCodeGenerator generator = new ShortCodeGenerator();

        assertEquals("1", generator.generate(1));
        assertEquals("z", generator.generate(61));
        assertEquals("10", generator.generate(62));
    }

    @Test
    void shouldRejectNonPositiveIds() {
        ShortCodeGenerator generator = new ShortCodeGenerator();

        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
        assertThrows(IllegalArgumentException.class, () -> generator.generate(-1));
    }
}
