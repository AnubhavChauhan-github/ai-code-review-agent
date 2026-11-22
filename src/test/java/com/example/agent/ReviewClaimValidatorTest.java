
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReviewClaimValidatorTest {

    private final ReviewClaimValidator validator =
            new ReviewClaimValidator();

    @Test
    void detectsUnverifiedTestPassClaim() {

        var warnings = validator.validate(
            "All tests passed successfully.",
            "NOT_EXECUTED"
        );

        assertEquals(1, warnings.size());
    }

    @Test
    void detectsUnverifiedFixClaim() {

        var warnings = validator.validate(
            "This confirms that the bug has been fixed.",
            "NOT_EXECUTED"
        );

        assertEquals(1, warnings.size());
    }

    @Test
    void doesNotFlagOrdinaryBugExplanation() {

        var warnings = validator.validate(
            "Initializing max to zero fails for "
            + "negative-only arrays.",
            "NOT_EXECUTED"
        );

        assertTrue(warnings.isEmpty());
    }
}
