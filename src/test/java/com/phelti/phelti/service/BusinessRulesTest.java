package com.phelti.phelti.service;

import com.phelti.phelti.model.BusinessRules;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusinessRulesTest {
    @Test void amountMustFitPositiveTwoDecimalStorage() {
        for (String value : new String[]{"0.01", "3.00", "99999999.99"})
            assertTrue(BusinessRules.validAmount(new BigDecimal(value)), value);
        for (String value : new String[]{"0", "-1", "0.001", "100000000.00"})
            assertFalse(BusinessRules.validAmount(new BigDecimal(value)), value);
        assertFalse(BusinessRules.validAmount(null));
    }
}
