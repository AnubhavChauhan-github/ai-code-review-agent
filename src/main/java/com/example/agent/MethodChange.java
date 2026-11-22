package com.example.agent;

public record MethodChange(
        String method,
        String changeType,
        boolean allowed
) {
}
