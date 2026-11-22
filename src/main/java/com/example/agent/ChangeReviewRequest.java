package com.example.agent;

import java.util.List;

public record ChangeReviewRequest(
        String originalFileName,
        String proposedFileName,
        List<String> allowedMethods
) {
}

