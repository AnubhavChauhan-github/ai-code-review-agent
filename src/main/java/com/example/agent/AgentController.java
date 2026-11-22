package com.example.agent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/agent")
public class AgentController {
	
	private final ToolCallingAgentService agentService;
	
	public AgentController(ToolCallingAgentService agentService) {
		this.agentService = agentService;
	}
 
	public record AgentRequest(String fileName) {}
	
	@PostMapping("/review")
	public ReviewResponse review(
	        @RequestBody AgentRequest request) throws Exception {

	    return agentService.review(request.fileName());
	}
}
