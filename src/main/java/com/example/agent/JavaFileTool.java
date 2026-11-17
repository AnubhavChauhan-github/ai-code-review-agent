package com.example.agent;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;


@Component
public class JavaFileTool {
	
	private final Path workspace = Path.of("workspace").toAbsolutePath().normalize();
	
	
	public String readFile(String fileName) {
		
		System.out.println("Received filename: [" + fileName + "]");
		
		
		try {
			if(fileName == null || !fileName.matches("[A-Za-z0-9]+[.]java")) {
				return "Invalid Java filename";
			}
			
			Path file = workspace.resolve(fileName).normalize();
			
			Path realWorkspace = workspace.toRealPath();
			Path realFile = file.toRealPath();
			
			if(!realFile.startsWith(realWorkspace) || !Files.isRegularFile(realFile) || Files.isSymbolicLink(file)) {
				return "File access denied";
			}
			
			if(Files.size(realFile) > 100_000) {
				return "File exceeds size limit";
			}
			
			return Files.readString(realFile);
		}
		catch(Exception e) {
			return "Unable to read requested file" + e.getClass().getSimpleName();
		}
	}
	

}
