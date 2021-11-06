package com.yewchen.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CrawlerController {
	
    @GetMapping("/diabloBM")
	public String hello( @RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) throws IOException {
		 
    	File initialFile = new File("src/main/resources/violation.txt");
	    InputStream in = null;
	    String res = "null";
	    try {
	    	in = new FileInputStream(initialFile);
	    	byte[] data = new byte[40960];
	    	in.read(data);
		    res = new String(data);
	    } catch ( Exception e ) {
	    	in.close();
	    	e.printStackTrace(); 
	    }
	    in.close();
		 
		/* set and return */
		model.addAttribute("name", res);
		return "diabloBM";
	        
	}
	
}