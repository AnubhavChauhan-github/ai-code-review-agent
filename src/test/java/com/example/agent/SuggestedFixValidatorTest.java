
package com.example.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class SuggestedFixValidatorTest {

    private final SuggestedFixValidator validator =
            new SuggestedFixValidator();

    private CodeReviewFinding reliabilityFinding(
            String problem) {

        return new CodeReviewFinding(
            "MEDIUM",
            "startWorkers",
            problem,
            "Example",
            "Review bounded resources",
            9
        );
    }

    @Test
    void flagsFixedThreadPoolSuggestion() {

        String review = """
            Suggested fix:
            ```java
            var pool = Executors.newFixedThreadPool(10);
            ```
            """;

        var warnings = validator.validate(
            review,
            List.of(reliabilityFinding(
                "Cached thread pool may create too many threads."
            ))
        );

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("unbounded"));
    }

    @Test
    void flagsQueueAddSuggestion() {

        String review = """
            Suggested fix:
            ```java
            var queue = new LinkedBlockingQueue<String>(100);
            queue.add("item");
            ```
            """;

        var warnings = validator.validate(
            review,
            List.of(reliabilityFinding(
                "LinkedBlockingQueue has no explicit capacity."
            ))
        );

        assertEquals(1, warnings.size());
    }

    @Test
    void acceptsBoundedExecutorExample() {

        String review = """
            ```java
            var executor = new ThreadPoolExecutor(
                5, 10, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100),
                new ThreadPoolExecutor.AbortPolicy()
            );
            ```
            """;

        var warnings = validator.validate(
            review,
            List.of(reliabilityFinding(
                "Potential thread exhaustion."
            ))
        );

        assertTrue(warnings.isEmpty());
    }
    

	@Test
	void flagsInlineFixedThreadPoolSuggestion() {
	
	    String review = """
	        Replace the cached executor with
	        Executors.newFixedThreadPool(10).
	        """;
	
	    var warnings = validator.validate(
	        review,
	        List.of(reliabilityFinding(
	            "Cached thread pool may create too many threads."
	        ))
	    );
	
	    assertEquals(1, warnings.size());
	}

}
