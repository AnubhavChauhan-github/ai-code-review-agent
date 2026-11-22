
package com.example.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SuggestedFixValidator {

    private static final Pattern CODE_BLOCK =
            Pattern.compile(
                "```(?:java)?\\s*([\\s\\S]*?)```",
                Pattern.CASE_INSENSITIVE
            );

    private static final Pattern FIXED_POOL =
            Pattern.compile(
                "Executors\\s*\\.\\s*newFixedThreadPool\\s*\\("
            );

    private static final Pattern QUEUE_ADD =
            Pattern.compile(
                "\\bqueue\\s*\\.\\s*add\\s*\\("
            );

    public List<String> validate(
            String aiReview,
            List<CodeReviewFinding> reliabilityFindings) {

        List<String> warnings = new ArrayList<>();

        if (aiReview == null
                || aiReview.isBlank()
                || reliabilityFindings == null
                || reliabilityFindings.isEmpty()) {
            return warnings;
        }

        boolean threadRisk = reliabilityFindings.stream()
                .anyMatch(f -> f.problem()
                        .toLowerCase()
                        .contains("thread"));

        boolean queueRisk = reliabilityFindings.stream()
                .anyMatch(f -> f.problem()
                        .toLowerCase()
                        .contains("queue"));

        Matcher blocks = CODE_BLOCK.matcher(aiReview);

        boolean fixedPoolFound = false;
        boolean queueAddFound = false;

        while (blocks.find()) {

            String code = blocks.group(1);

            if (threadRisk && FIXED_POOL.matcher(code).find()) {
                fixedPoolFound = true;
            }

            if (queueRisk && QUEUE_ADD.matcher(code).find()) {
                queueAddFound = true;
            }
        }
        
        if (threadRisk
		        && FIXED_POOL.matcher(aiReview).find()) {
		    fixedPoolFound = true;
		}
		
		if (queueRisk
		        && QUEUE_ADD.matcher(aiReview).find()) {
		    queueAddFound = true;
		}

        if (fixedPoolFound) {
            warnings.add(
                "Suggested fix uses newFixedThreadPool(). "
                + "This bounds worker threads but leaves "
                + "the task queue unbounded. Review whether "
                + "a bounded ThreadPoolExecutor is needed."
            );
        }

        if (queueAddFound) {
            warnings.add(
                "Suggested queue.add() call may throw "
                + "when a bounded queue is full. "
                + "It does not provide waiting-based "
                + "backpressure. Review overload handling."
            );
        }

        return warnings;
    }
}
