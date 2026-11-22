
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.List;

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
    

	@Test
	void detectsContradictionWithReliabilityFindings() {
	
	    var reliabilityFindings = List.of(
	        new CodeReviewFinding(
	            "MEDIUM",
	            "startWorkers",
	            "Cached thread pool may exhaust resources.",
	            "Executors.newCachedThreadPool()",
	            "Use a bounded executor.",
	            9
	        )
	    );
	
	    var warnings = validator.validateAgainstFindings(
	        "There are no potential issues detected.",
	        List.of(),
	        List.of(),
	        reliabilityFindings
	    );
	
	    assertEquals(1, warnings.size());
	}
	
	@Test
	void doesNotFlagReviewAcknowledgingRisk() {
	
	    var reliabilityFindings = List.of(
	        new CodeReviewFinding(
	            "MEDIUM",
	            "startWorkers",
	            "Unbounded queue may exhaust memory.",
	            "new LinkedBlockingQueue<String>()",
	            "Set an explicit capacity.",
	            12
	        )
	    );
	
	    var warnings = validator.validateAgainstFindings(
	        "The queue may grow without an explicit "
	        + "capacity and exhaust memory.",
	        List.of(),
	        List.of(),
	        reliabilityFindings
	    );
	
	    assertTrue(warnings.isEmpty());
	}
	
	@Test
	void doesNotFlagNoIssuesWhenNoFindingsExist() {
	
	    var warnings = validator.validateAgainstFindings(
	        "No issues detected.",
	        List.of(),
	        List.of(),
	        List.of()
	    );
	
	    assertTrue(warnings.isEmpty());
	}

}
