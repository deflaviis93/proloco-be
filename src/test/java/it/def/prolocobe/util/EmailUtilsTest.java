package it.def.prolocobe.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailUtilsTest {

    @Test
    void normalizza_convertiInMinuscoloERimuoveSpazi() {
        assertEquals("mario.rossi@example.com", EmailUtils.normalizza(" Mario.Rossi@Example.COM "));
    }

    @Test
    void normalizza_gestisceNull() {
        assertNull(EmailUtils.normalizza(null));
    }
}
