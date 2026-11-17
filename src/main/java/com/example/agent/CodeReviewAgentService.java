package com.example.agent;

import org.springframework.stereotype.Service;


@Service
public class CodeReviewAgentService {
	
	
	private final JavaFileTool fileTool;
	private final OllamaService huggingFaceService;
	
	public CodeReviewAgentService(JavaFileTool fileTool, OllamaService huggingFaceService) {
		this.fileTool = fileTool;
		this.huggingFaceService = huggingFaceService;
	}
	
	public String reviewFile(String fileName) {
		return fileTool.readFile(fileName);
	}
	
	public String aiReview(String fileName) throws Exception{
		
		String code = fileTool.readFile(fileName);
		
		if(code.startsWith("Invalid Java filename") 
				|| code.startsWith("File access denied") 
				|| code.startsWith("File exceeds")
				|| code.startsWith("Unable to read")) {
			throw new IllegalArgumentException(code);
		}
		
		return huggingFaceService.reviewCode(code);
			
	}
}
