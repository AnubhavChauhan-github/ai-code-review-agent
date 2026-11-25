package com.example.agent;

import java.util.List;

public record SafetyGateDecision(
		String status,
		List<String> reasons
		) {

}
