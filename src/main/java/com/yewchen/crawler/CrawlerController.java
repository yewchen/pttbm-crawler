package com.yewchen.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CrawlerController {
	
	@Value("classpath:static/violation.txt")
    private Resource resource;

    @GetMapping("/diabloBM")
	public String hello( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
		 
		String retValue = "";
		
		/* get file */
		try {
			
			File file = resource.getFile();
			retValue = readFileByPath(file.getPath());
			System.out.println(retValue);
		} catch (IOException e) { e.printStackTrace(); }
		 
		/* set and return */
		model.addAttribute("name", retValue);
		return "diabloBM";
	        
	}
	
	private static String readFileByPath(String filePath) {
        String content = "";
        try {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        } catch (IOException e) { e.printStackTrace(); }
        return content;
    }
	
}