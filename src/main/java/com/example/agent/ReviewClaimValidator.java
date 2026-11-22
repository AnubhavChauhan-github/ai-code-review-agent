
package com.example.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class ReviewClaimValidator {

    private static final Pattern TEST_PASS_CLAIM =
            Pattern.compile(
                "\\b(?:all\\s+)?tests?\\s+"
                + "(?:have\\s+)?passed\\b"
                + "|\\bbuild\\s+(?:was\\s+)?successful\\b",
                Pattern.CASE_INSENSITIVE
            );

    private static final Pattern FIX_VERIFIED_CLAIM =
            Pattern.compile(
                "\\b(?:bug|fix|issue)\\s+"
                + "(?:has\\s+been|was|is)\\s+"
                + "(?:fixed|verified|resolved)\\b"
                + "|\\bverified\\s+successfully\\b"
                + "|\\bthis\\s+confirms\\s+that\\s+"
                + "the\\s+bug\\s+has\\s+been\\s+fixed\\b",
                Pattern.CASE_INSENSITIVE
            );

    public List<String> validate(
            String aiReview,
            String verificationStatus) {

        List<String> warnings = new ArrayList<>();

        if (aiReview == null || aiReview.isBlank()) {
            return warnings;
        }

        if (!"NOT_EXECUTED".equals(verificationStatus)) {
            return warnings;
        }

        if (TEST_PASS_CLAIM.matcher(aiReview).find()) {
            warnings.add(
                "AI review claims tests passed or the "
                + "build succeeded, but test execution "
                + "has not been recorded."
            );
        }

        if (FIX_VERIFIED_CLAIM.matcher(aiReview).find()) {
            warnings.add(
                "AI review claims a fix or bug resolution "
                + "was verified without execution evidence."
            );
        }

        return warnings;
    }
    

		public List<String> validateAgainstFindings(
		        String aiReview,
		        List<CodeReviewFinding> correctnessFindings,
		        List<CodeReviewFinding> riskFindings,
		        List<CodeReviewFinding> reliabilityFindings) {
		
		    List<String> warnings = new ArrayList<>();
		
		    if (aiReview == null || aiReview.isBlank()) {
		        return warnings;
		    }
		
		    int totalFindings =
		            correctnessFindings.size()
		            + riskFindings.size()
		            + reliabilityFindings.size();
		
		    if (totalFindings == 0) {
		        return warnings;
		    }
		
		    Pattern noIssuesClaim = Pattern.compile(
		        "\\b(?:there\\s+are\\s+)?no\\s+"
		        + "(?:potential\\s+)?issues\\s+"
		        + "(?:were\\s+)?detected\\b"
		        + "|\\bdoes\\s+not\\s+contain\\s+"
		        + "any\\s+(?:potential\\s+)?"
		        + "(?:issues|problems|errors)\\b"
		        + "|\\bno\\s+suggested\\s+fixes\\b"
		        + "|\\bdoes\\s+not\\s+require\\s+"
		        + "any\\s+modifications\\b",
		        Pattern.CASE_INSENSITIVE
		    );
		
		    if (noIssuesClaim.matcher(aiReview).find()) {
		        warnings.add(
		            "AI review may contradict deterministic findings. "
		            + totalFindings
		            + " finding(s) exist, but the explanation "
		            + "claims no issues or fixes are needed."
		        );
		    }
		
		    return warnings;
		}

}
