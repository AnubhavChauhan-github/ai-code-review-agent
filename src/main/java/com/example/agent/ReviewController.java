package com.example.agent;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api")
public class ReviewController {
	
	private final CodeReviewAgentService agentService;
	
	public ReviewController(CodeReviewAgentService agentService) {
		this.agentService = agentService;
	}
	
	@GetMapping("/hello")
	public String hello() {
		return "Java AI code Review Agent is running";
	}
	
	@PostMapping("/review")
	public String review(@RequestBody CodeReviewRequest request) {
		
		return "Received Java code for review: " + request.code();
	}
	
	@PostMapping("/file-review")
	public String reviewFile(@RequestBody FileReviewRequest request) {
		return agentService.reviewFile(request.fileName());
	}
	
	@PostMapping("/ai-review")
	public String aiReview(@RequestBody FileReviewRequest request) throws Exception {
		return agentService.aiReview(request.fileName());
	}
	
	public record CodeReviewRequest(String code) {}
	public record FileReviewRequest(String fileName) {}
	
	

}
